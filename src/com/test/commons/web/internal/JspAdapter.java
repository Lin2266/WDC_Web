package com.test.commons.web.internal;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.test.commons.annotation.BackingBean;
import com.test.commons.util.JspUtil;
import com.test.commons.util.MsgUtil;
import com.test.commons.util.StrUtil;
import com.test.commons.util.internal.SpringClassUtil;
import com.test.commons.web.FlashScope;

//
//page flow:
//                                   redirect (next URI's stage0(),stage1())
//        前一頁---------------+   +----------------------------------------> [page_C](without backing bean)
//                            |   |
//                    stage0()|   |
//  direct/redirect (stage2())|   | forward (stage3())
//                            ↓   |
//                      [page_A]/[bean_A]
//                       |   ↑      | | |
//                       |   |      | | +---------------------------------+
//                       |   |      | +--------------------------+        |
//                       |   |      |          forward (stage3())|        |
//                       |   +------+                            |        |
//                       |     forward (stage3())                |        |
//                       |                                       |        |redirect (next URI's stage0(),stage2())
//                       |                                       |        |
//                       |                                       ↓        ↓
//                       |                                    [page_B]/[bean_B]
//                       |                                                ↑
//                       |                                                |
//                       +------------------------------------------------+
//                         direct link/form submit (next URI's stage0(),stage2())
//

public class JspAdapter {
	private static final Logger log = LoggerFactory.getLogger(JspAdapter.class);
	
    private ApplicationContext applicationContext; //Spring application context
    private long fileUploadMaxSizeBytes; //max file upload size (byte)
    private String actionParamName; //在 URL 中用來代表要呼叫 backing bean action 的參數
    private String forwardPrefixIndicator; //backing bean 欲 forward 時, 傳回值字串之前應加上的標示字串
    private String redirectPrefixIndicator; //backing bean 欲 redirect 時, 傳回值字串之前應加上的標示字串
    private String uriCharEncoding; //ap server 對網址所使用的字元編碼
    private String requestCharacterEncoding;  //request parameter encoding
    
    public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public long getFileUploadMaxSizeBytes() {
		return fileUploadMaxSizeBytes;
	}

	public void setFileUploadMaxSizeBytes(long fileUploadMaxSizeBytes) {
		this.fileUploadMaxSizeBytes = fileUploadMaxSizeBytes;
	}

	public String getActionParamName() {
		return actionParamName;
	}

	public void setActionParamName(String actionParamName) {
		this.actionParamName = actionParamName;
	}

	public String getForwardPrefixIndicator() {
		return forwardPrefixIndicator;
	}

	public void setForwardPrefixIndicator(String forwardPrefixIndicator) {
		this.forwardPrefixIndicator = forwardPrefixIndicator;
	}

	public String getRedirectPrefixIndicator() {
		return redirectPrefixIndicator;
	}

	public void setRedirectPrefixIndicator(String redirectPrefixIndicator) {
		this.redirectPrefixIndicator = redirectPrefixIndicator;
	}

	public String getUriCharEncoding() {
		return uriCharEncoding;
	}

	public void setUriCharEncoding(String uriCharEncoding) {
		this.uriCharEncoding = uriCharEncoding;
	}

	public String getRequestCharacterEncoding() {
		return requestCharacterEncoding;
	}

	public void setRequestCharacterEncoding(String requestCharacterEncoding) {
		this.requestCharacterEncoding = requestCharacterEncoding;
	}

	//@param beanPath 不含 ".jsp"
	public void execute(final ServletRequest req, final ServletResponse res, final FilterChain chain, 
			final String beanPath) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest)req; //for Servlet 環境 (not Portlet)
        final HttpServletResponse response = (HttpServletResponse)res;
        final String dest = beanPath + BackingBeanHelper.DEFAULT_JSP_URI_SUFIX; //目的畫面 URI. 取當前畫面 URI, 如果 filter 結束前之前出問題, 就導回前一畫面
        String dest2 = null; //由 backing bean 之 action method 所 return 的下一畫面的 URI 指示字串(optional)
        BeanParameterMapper.ClassDescriptor classDesc = null;

        try {
            request.setCharacterEncoding(this.requestCharacterEncoding);
            final boolean hasBackingBean = this.applicationContext.containsBean(beanPath);
            RequestParameterHelper paramHelper = null;
            String actionName = null; //取 backing bean 上欲執行的 meghod name
            
            if(log.isDebugEnabled()) {
            	final StringBuilder msg = new StringBuilder("[").append(request.getMethod())
            			.append(isMultiPartRequest(request) ? " MULTI-PART" : " ")
            			.append(req.getRemoteAddr())
            			.append(" ").append(MsgUtil.retrieveLocaleString(request.getLocale()));
            	final String queryString;
            	if(!hasBackingBean) { //無 backing bean 就不要消費 request parameters
            		queryString = request.getQueryString();
            		msg.append(" no-backing-bean");
            	} else {
            		paramHelper = getRequestParameterHelper(request, this.fileUploadMaxSizeBytes);
                	actionName = paramHelper.getParameter(this.actionParamName);
                	queryString = paramHelper.toQueryStringForDebugging(true);
        			msg.append(" action=").append(StrUtil.print(actionName));
            	}
            	msg.append("] ").append(request.getContextPath()).append(request.getServletPath()); //即使網址未指定 index.jsp 也能顯示出來
            	if(queryString != null && queryString.length() != 0)
        			msg.append("?").append(queryString);
            	log.debug(msg.toString());
            }

            //處理 request 初期階段
            stage0(request, dest);

            //目的畫面有配置 backing bean 者, 將 request 參數結合至 bean 屬性, 再呼叫 action method
            if(hasBackingBean) {
            	if(paramHelper == null)
            		paramHelper = getRequestParameterHelper(request, this.fileUploadMaxSizeBytes);
            	if(actionName == null)
            		actionName = paramHelper.getParameter(this.actionParamName);

                //取得 backing bean
                final Object bean = this.applicationContext.getBean(beanPath); //依據 alias name 來取 bean, 如果 bean scope=session 者, 此時 bean 應已在 session 中了
                classDesc = BeanParameterMapper.getBeanDescriptor(bean.getClass());
                //Spring 自動在 request/session 置入指定 scope 的 backing bean, 其 id 為 @BackingBean 的 id 屬性值
                
                //特別處理 backing bean 有關 scope 相關議題
                stage2(request, bean, classDesc);
                
                //populate request 參數並執行 action
                final Map<String, String[]> parameterMap = paramHelper.getParameterMap();
                final Object ret = populateAndExecuteBackingBean(request, response, bean, classDesc, actionName, parameterMap);
                
                if(ret instanceof String) //非 null 代表要導向別的 JSP 畫面
                    dest2 = StrUtil.trimLeft((String)ret);
            } else {
            	//特別處理當前頁無配置 backing bean 的 scope 議題
            	stage1(request);
            }
        } catch(Throwable t) {
            handleException(request, t);
        }
        
        //導向其他畫面(forward 或 redirect) (要把當前 bean 值帶入下一頁者, 應使用 flashScope)
        if(dest2 != null) { //在此, classDesc 必不為 null
            if(StrUtil.startWithIgnoreCase(dest2, this.forwardPrefixIndicator)) { //forward:
                dest2 = StrUtil.trim(dest2.substring(this.forwardPrefixIndicator.length())); //去掉開頭標示 forward 的字串
                int i = 0;
                final String path2 = ((i = dest2.indexOf('?')) != -1) ? dest2.substring(0, i) : dest2; //截去 query string
                
                if(!path2.equals(dest)) { //確定要 forward 至別的 JSP 畫面 (若 forward 前後是同一畫面者, 不必導頁, 不必處理 scope 了)(如果 path2 帶 URL rewriting 字串者, 這裡就不會等於當前網址 dest)
                	stage3(request, path2); //特別處理有關 scope 相關議題(forward 到下一頁並不經過 BackingBeanFilter)
	                request.getRequestDispatcher(dest2).forward(request, response); //如果 dest2 含 URL rewriting(非 query string) 字串者, 就無法順利前往了
                	log.debug("Forward to {}", dest2);
                } else {
                	dest2 = null;
                }
            } else if(StrUtil.startWithIgnoreCase(dest2, this.redirectPrefixIndicator)) { //redirect:
            	//不需在此記下當前 JSP 畫面 URI, redirect 後自會再度經過此 filter, 屆時 dest2 == null
            	dest2 = StrUtil.trimLeft(dest2.substring(this.redirectPrefixIndicator.length())); //去掉開頭標示 redirect 的字串
            	if(dest2.startsWith("/")) //同 AP 內換頁, 使用絕對路徑
            		dest2 = request.getContextPath() + dest2;
            	
            	JspUtil.internalCopyMssageInRequestToSession(); //把訊息交給 session 以便導頁後可取出訊息
                response.sendRedirect(dest2);
            	log.debug("Redirect to {}", dest2);
            } else { //backing bean 的 return 字串有誤者
                final String msg = "Backing Bean [" + classDesc.nonEnhancedClass.getName() + "]: for forwarding/redirecting to the next page, returning string must be prefixed with either 'forward:' or 'redirect:'";
                log.error(msg);
                JspUtil.setMessage(msg);
                dest2 = null;
            }
        }
        
        //因此處已在 forward/redirect 之後, 在此印出 debug 訊息的同時, 下一畫面可能也同時正在 render 中...
        //log.debug("\nKEY_CURRENT_BACKING_BEAN_NAME value in session: " + (String)request.getSession().getAttribute(BackingBeanHelper.KEY_CURRENT_BACKING_BEAN_NAME) +
        //		"\nKEY_VIEW_BEAN_NAME value in session: " + (String)request.getSession().getAttribute(KEY_VIEW_BEAN_NAME) +
        //		"\nKEY_VIEW_BEAN_NAME_ALIAS value in session: " + (String)request.getSession().getAttribute(KEY_VIEW_BEAN_NAME_ALIAS));
	}

	@Override
	public String toString() {
		return "JspAdapter [applicationContext=" + applicationContext + ", fileUploadMaxSizeBytes=" + fileUploadMaxSizeBytes + ", actionParamName=" +
				actionParamName + ", forwardPrefixIndicator=" + forwardPrefixIndicator + ", redirectPrefixIndicator=" + redirectPrefixIndicator +
				", uriCharEncoding=" + uriCharEncoding + ", requestCharacterEncoding=" + requestCharacterEncoding + "]";
	}

	private String getBeanPath(String uri) {
    	if(uri.endsWith(BackingBeanHelper.DEFAULT_JSP_URI_SUFIX))
    		return uri.substring(0, uri.length() - BackingBeanHelper.DEFAULT_JSP_URI_SUFIX.length());
    	return uri;
    }
	
    private Object populateAndExecuteBackingBean(final HttpServletRequest request, final HttpServletResponse response, final Object bean,
    		final BeanParameterMapper.ClassDescriptor classDesc, final String actionName, final Map<String, String[]> parameterMap) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException, InstantiationException {
    	//將 request 參數值 populate 到 backing bean 的屬性
    	//if(parameterMap.size() != 0) //改統一透過 action method argument 傳遞, 不透過 bean 屬性
        //	BeanParameterMapper.populate(bean, parameterMap, classDesc);
    	
    	//執行 action
        return BeanParameterMapper.invokeAction(request, response, bean, classDesc, actionName, parameterMap, (OutputWrapper)null, BeanParameterMapper.INVOKE_AS_ACTION);
    }

    private String handleException(final HttpServletRequest request, final Throwable e) {
        //找出 exception 根源, 並將 SQL 訊息翻成中文訊息
        Throwable t = e, t2 = e;
        while((t = t.getCause()) != null)
            t2 = t;
        log.error("{}\n==> Root exception: {}\nStacktrace:\n", e, t2, e);

        String msg = t2.getMessage();
        if(msg == null || msg.length() == 0 || "null".equals(msg))
            msg = t2.toString();
        JspUtil.setMessage(msg);

        return msg;
    }

    //開始處理 request 初期階段
    private void stage0(final HttpServletRequest request, final String currentPageURI) {
    	//記下畫面實際 URI
    	markCurrentJspURI(request, currentPageURI);
    	//處理 flashScope 的資料(direct link 或 redirect 至同一畫面都會視為換頁)
        preCheckFlashScopeObject(request);
    }
    
    //用於當前頁無配置 backing bean 的情況(=> 無 forward/redirect)
    private void stage1(final HttpServletRequest request) {
    	markCurrentBackingBeanName(request, null);
    }
    
    //在替 backing bean 屬性設值且執行 action method 之前執行(由上頁 JSP/Backing bean redirect 或 direct link 或 form submit 至當前頁時)
    private void stage2(final HttpServletRequest request, final Object bean, final BeanParameterMapper.ClassDescriptor classDesc) throws Exception {
        //每進入新頁就將當前頁之 backing bean name 予以記錄(在此與 Spring 無關, 但為了 Ajax 連至當前頁以外的 backing bean 操作而設)
    	markCurrentBackingBeanName(request, StrUtil.selectNotEmpty(classDesc.annotation.path(), classDesc.annotation.value()));
    }
    
    //處理 scope 的資料(由 backing bean forward 至 JSP(不與當前 JSP 畫面相同) 階段)
    private void stage3(final HttpServletRequest request, final String nextPageURI) {
    	if(nextPageURI == null) //代表 forward 至當前頁, 不必處理了
    		return;
    	
    	//記下畫面實際 URI
    	markCurrentJspURI(request, nextPageURI);
    	
    	//flashScope
    	postCheckFlashScopeObject(request);

		//下一頁 JSP 對應的 backing bean
		final String nextBeanPath = getBeanPath(nextPageURI);
		if(this.applicationContext.containsBean(nextBeanPath)) { //forward 後的畫面有配置 backing bean 者
			final Class<?> nextBeanClass = SpringClassUtil.getUnenhancedClass(this.applicationContext.getType(nextBeanPath));
			final BackingBean nextBeanAnnotation = nextBeanClass.getAnnotation(BackingBean.class);
	        if(nextBeanAnnotation == null)
	        	throw new RuntimeException("the backing bean '" + nextBeanClass.getName() + "' not annotated with '@BackingBean'");
			
			//預先起下一頁 JSP 對應的 backing bean
        	this.applicationContext.getBean(nextBeanPath); //預先起下一頁的 session scope backing bean 的 session(但尚不需結合 request 參數, 也不需執行 action)

			markCurrentBackingBeanName(request, StrUtil.selectNotEmpty(nextBeanAnnotation.path(), nextBeanAnnotation.value()));
		} else {
			stage1(request);
		}
    }
    
    //將當前 JSP 畫面 URI 記錄於 session 中
    private void markCurrentJspURI(final HttpServletRequest request, final String uri) {
    	request.getSession().setAttribute(BackingBeanHelper.KEY_CURRENT_JSP_URI, request.getContextPath() + uri);
    }
    
    //將當前 JSP 配對的 backing bean name 記錄於 session 中
    private void markCurrentBackingBeanName(final HttpServletRequest request, final String beanName) {
    	request.getSession().setAttribute(BackingBeanHelper.KEY_CURRENT_BACKING_BEAN_NAME, beanName);
    }

    //導頁前處理 flash scope 的資料
    private void preCheckFlashScopeObject(final HttpServletRequest request) {
    	final FlashScope flashScope = (FlashScope)request.getSession().getAttribute(FlashScope.KEY_FLASH_SCOPE_IN_SESSION);
    	if(flashScope != null)
    		flashScope.doPrePhaseActions(request);
    }
    
    //導頁後處理 flash scope 的資料
    private void postCheckFlashScopeObject(final HttpServletRequest request) {
    	final FlashScope flashScope = (FlashScope)request.getSession().getAttribute(FlashScope.KEY_FLASH_SCOPE_IN_SESSION);
    	if(flashScope != null)
    		flashScope.doPostPhaseActions(request);
    }
    
    //參考 RequestParameterHelper.isMultiPartRequest()
    private boolean isMultiPartRequest(final HttpServletRequest request) {
    	final String contentType = request.getContentType();
    	final String method = request.getMethod();
    	return ("POST".equals(method) || "PUT".equals(method)) && //參考 FileUpload.isMultipartContent()
    			contentType != null && contentType.length() > RequestParameterHelper.MULTIPART.length() && 
    			contentType.regionMatches(true, 0, RequestParameterHelper.MULTIPART, 0, RequestParameterHelper.MULTIPART.length());
    }
    
    private RequestParameterHelper getRequestParameterHelper(final HttpServletRequest request, final long fileUploadMaxSizeBytes) {
    	final RequestParameterHelper ret = new RequestParameterHelper(request);
    	ret.setUriEncoding(this.uriCharEncoding);
    	ret.setParamCharEncoding(this.requestCharacterEncoding);
    	if(ret.isMultiPartRequest()) {
            ret.setUploadFileMaxSize(fileUploadMaxSizeBytes);
            ret.setUploadFileSaveDir(JspUtil.createUniqueTempRealPath());
        }
    	return ret;
    }
}
