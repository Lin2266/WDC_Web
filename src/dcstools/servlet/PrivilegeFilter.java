package dcstools.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import dcstools.Account;
import tools.ExceptionUtil;
import tools.StrUtil;

/**
 * 根據畫面網址判斷使用者有無使用該畫面的權限.<br>
 * 針對 URI 樣式例: *.jsp, /servlet/*
 */
public class PrivilegeFilter implements Filter {
	private static final Log log = LogFactory.getLog(PrivilegeFilter.class);
	private static final String RESPONSE_CONTENT_TYPE = "text/html; charset=UTF-8";
    private static final String WARNING_NO_PRIVILEGE = "使用者無此作業權限";
    private static final String JSP_EXCEPTION_KEY = "javax.servlet.jsp.jspException";
    private static final String ERROR_MESSAGE_KEY = "legacy_servlet_invoker_error_message";
    private static final String ERROR_PAGE = "/ErrPage.jsp";
    private static final String ACTION_OBJECT_URI_SUFFIX = ".do"; //for 較新型式 action object URI: /*.do
    private static final String ACTION_OBJECT_AJAX_URI_SUFFIX = ".ajax"; //for URI: /*.ajax
    
	public void init(final FilterConfig filterConfig) throws ServletException {
		log.info("PrivilegeFilter ready.");
	}
	
	@Override
	public void destroy() {}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest)req;
		final HttpServletResponse response = (HttpServletResponse)res;
		final String path = getPath(request); //不含 contextPath
		
		try {
			final String path2 = path.toLowerCase(); //全小寫
			
			if(path2.length() > 4) { //略過可能的 default page
				//URI(不含 contextPath) 非 "/servlet/jdcs..." 及 "/jdcs....jsp", "/jdcs....do", "/jdcs....ajax" 型式的, 暫不檢查權限
				String[] programName = null;
				if(path2.startsWith("/servlet/jdcs")) { //check: /servet/jdcsXXX.jdcsXXX -> 第一個 jdcsXXX programName, 第二個 jdcsXXX subProgramName
					programName = getProgramNameFromServletPath(path2);
				} else if(path2.endsWith(ACTION_OBJECT_URI_SUFFIX) || path2.endsWith(ACTION_OBJECT_AJAX_URI_SUFFIX)) { //check: /jdcsXXX.jdcsXXX.do -> 第一個 jdcsXXX programName, 第二個 jdcsXXX subProgramName
					programName = getProgramNameFromActionPath(path2);
				}
				//TODO: 舊 jdcs 完全不把關 JSP 畫面, 所以這裡也暫不管制
				//else if(path2.endsWith(".jsp") && path2.startsWith("/jdcs")) { //check: /jdcsXXX/jdcsXXX.jsp -> 第一個 jdcsXXX programName, 第二個 jdcsXXX subProgramName
				//	programName = getProgramNameFromJSPPath(path2);
				//}
				
				if(programName != null) {
					final String ftype = request.getParameter("ftype");
					final String[] procNames = getProcName(programName[0], programName[1], ftype);
					if(procNames != null && procNames.length > 0) {
						if(procNames.length == 1) {
							if(!Account.hasPrivilege(request, procNames[0])) {
								log.error("no privilege for: " + path + " (check procName '" + procNames[0] + "' from '" + programName[0] + "', '" + programName[1] + "', ftype=" + ftype + ")");
								throw new Exception(WARNING_NO_PRIVILEGE);
							}
						} else {
							boolean hasPrivilege = false;
							for(int i = 0; i < procNames.length; i++) {
								if(Account.hasPrivilege(request, procNames[i])) {
									hasPrivilege = true;
									break;
								}
							}
							
							if(!hasPrivilege) {
								log.error("no privilege for: " + path + " (check procName [" + StrUtil.join(", ", procNames) + "] from '" + programName[0] + "', '" + programName[1] + "', ftype=" + ftype + ")");
								throw new Exception(WARNING_NO_PRIVILEGE);
							}
						}
					}
				}
			}
			
			chain.doFilter(req, res);
		} catch(Throwable t) {
			if(path.endsWith(ACTION_OBJECT_AJAX_URI_SUFFIX) ||
					"true".equals(request.getParameter("AJAX"))) { //WORKAROUND: 用額外的 "AJAX" request parameter 判斷傳統 servlet 網址是否屬 AJAX 呼叫
				response.setContentType(RESPONSE_CONTENT_TYPE);
	    		response.getWriter().write(ExceptionUtil.getRootMessage(t));
			} else {
				handleException(request, response, t); //到 error page
			}
		}
	}
	
	private String getPath(final HttpServletRequest request) {
		final String uri = request.getRequestURI(); //含 context path, 不含 query string. default page 省略時將無法被顯示在 uri 字串中
		final String contextPath = request.getContextPath();
		final String ret = (uri.length() > contextPath.length()) ? uri.substring(contextPath.length()) : uri;
		return ret;
	}
	
	//pttern: /jdcsXXX/jdcsYYY.jsp -> [ jdcsXXX, jdcsYYY ]
	private String[] getProgramNameFromJSPPath(final String path) {
		final int i2 = path.indexOf('/', 1); //第二個 '/' 的 index
		if(i2 < 0)
			return null;
		final int i3 = path.length() - 4; //結尾 ".jsp" 的 index
		return new String[] { path.substring(1, i2), path.substring(i2 + 1, i3) };
	}
	
	//pattern: /servlet/jdcsXXX.jdcsYYY  -> [ jdcsXXX, jdcsYYY ]
	private String[] getProgramNameFromServletPath(final String path) {
		final int i2 = path.indexOf('/', 1); //第二個 '/' 的 index
		final int i3 = path.indexOf('.');
		if(i3 < 0)
			return null;
		return new String[] { path.substring(i2 + 1, i3), path.substring(i3 + 1) };
	}
	
	//pattern: /jdcsXXX.jdcsYYY.do  -> [ jdcsXXX, jdcsYYY ]
	//pattern: /jdcsXXX.jdcsYYY.ajax  -> [ jdcsXXX, jdcsYYY ]
	private String[] getProgramNameFromActionPath(final String path) {
		final int i2 = path.indexOf('.');
		final int i3 = path.indexOf('.', i2 + 1);
		if(i3 < 0)
			return null;
		return new String[] { path.substring(1, i2), path.substring(i2 + 1, i3) };
	}

	//由畫面網址截取可能的 程式代碼.
	//根據使用者進入的畫面的網址之路徑, 子路徑(例: /jdcs11100/jdcs1110001.jsp => "jdcs11100", "jdcs1110001"; 或 /jdcs11100.jdcs11100_01T => "jdcs11100", "jdcs11100_01T")轉成對應的程式代碼
	//(1) 取由網址之 name/subName 及 ftype/preferredProcName 參數 所對對應的受檢用的 程式代碼 (ftype: 文案卷別. 0:文卷 1:案卷 2:中心案卷)
	//(2) 特許放行的網址則傳回 null.
	//private String getCheckProgramName(final String name, final String subName, final String ftype, final String preferredProcName) {
	private String[] getProcName(final String name, final String subName, final String ftype) {
		switch(name) { //注意: name 已先全轉為小寫, 以下 case 項內容 也要寫成小寫
			case "jdcs915": //for 其他系統 HTTP 連線
			case "jdcs915a": //for 審判系統介接
			case "jdcs3p000": //原 jdcs3p000* 內部 ConnSession 以 jdcs00000 作為受檢查的程式代碼
			case "jdcs3u000": //原 jdcs3p000* 內部 ConnSession 以 jdcs00000 作為受檢查的程式代碼
			case "jdcs3z200": //for client 端文稿製作軟體
			case "jdcsa7200": //公告
			case "jdcsa7210": //公告
			case "jdcsa7300": //公告
			case "jdcsa7400": return null; //表示 無條件放水
			
			case "jdcs11100":
			case "jdcs11110":
			case "jdcs71400": {
				switch(name) {//加入權限-收文資料查詢(11120)
					case "jdcs11100": return new String[] { name, "jdcs11120" };
					case "jdcs11110": return new String[] { name, "jdcs11120" };
					case "jdcs71400": return new String[] { name, "jdcs11120" };
				}
			}
			
			case "jdcs11400": return ("jdcs11400_01t".equals(subName) || "jdcs11400_03t".equals(subName)) ? new String[] { "jdcs11100", "jdcs11400" } : new String[] { "jdcs11400" };
			case "jdcs30400":
			case "jdcs30500":
			case "jdcs31000":
			case "jdcs32000":
			case "jdcs33000":
			case "jdcs34000":
			case "jdcs36100":
			case "jdcs36c00":
			case "jdcs37000":
			case "jdcs38100":
			case "jdcs38200":
			case "jdcs38300":
			case "jdcs38400":
			case "jdcs38500":
			case "jdcs38600":
			case "jdcs38700":
			case "jdcs39000":
			case "jdcs39100":
			case "jdcs39400":
			case "jdcs3a000":
			case "jdcs3b000":
			case "jdcs3c000":
			case "jdcs3d000":
			case "jdcs3e000":
			case "jdcs3f000":
			case "jdcs3h000":
			case "jdcs3i000":
			case "jdcs3k000":
			case "jdcs3l000":
			case "jdcs3m000":
			case "jdcs3n000":
			case "jdcs3r000":
			case "jdcs3s000":
			case "jdcs3t000":
			case "jdcs3x000":
			case "jdcs3y000":
			case "jdcs3z000":
			case "jdcs3z100":
			case "jdcs5y000":
			case "jdcs7y000":
			case "jdcs1z000": return new String[] { "jdcs30000" }; //依原該支 servlet 內 ConnSession 所使用的受檢查程式代碼
			case "jdcs3v000": return "jdcs3v000_00t".equals(subName) ? new String[] { "jdcs3v000" } : new String[] { "jdcs3v000", "jdcs30000" };
			case "jdcs593i0": {
				if(subName != null) {
					switch(subName) {
						case "jdcs593i0_02t": return new String[] { "jdcs30000" };
						case "jdcs593i0_01t": return new String[] { "jdcs59300" };
						case "jdcs593i0_00t": return new String[] { "0".equals(ftype) ? "jdcs59300_0" :
								"1".equals(ftype) ? "jdcs59300_1" : 
								"2".equals(ftype) ? "jdcs59300_2" : "jdcs59300"
							};
					}
				}
				return new String[] { "jdcs30000" };
			}
			case "jdcs21000": return new String[] { "1".equals(ftype) ? "jdcs74100" : "jdcs21000" };
			case "jdcs24000": return new String[] { "1".equals(ftype) ? "jdcs74300" : "jdcs24000" };
			case "jdcs29000": return new String[] { "1".equals(ftype) ? "jdcs74400" : "jdcs29000" };
			case "jdcs2c000b": return new String[] { "1".equals(ftype) ? "jdcs74200" : "jdcs2c000" };
			case "jdcs55c00": return new String[] { !"jdcs55c00_00t".equals(subName) ? "jdcs55c00" :
				"0".equals(ftype) ? "jdcs55c00_0" :
				"1".equals(ftype) ? "jdcs55c00_1" : 
				"2".equals(ftype) ? "jdcs55c00_2" : "jdcs55c00"
			};
			case "jdcs55e00": return new String[] { !"jdcs55e00_00t".equals(subName) ? "jdcs55e00" :
				"0".equals(ftype) ? "jdcs55e00_0" : 
				"1".equals(ftype) ? "jdcs55e00_1" : 
				"2".equals(ftype) ? "jdcs55e00_2" : "jdcs55e00"
			};
			case "jdcs55f00": return new String[] { !"jdcs55f00_00t".equals(subName) ? "jdcs55f00" :
				"0".equals(ftype) ? "jdcs55f00_0" :
				"1".equals(ftype) ? "jdcs55f00_1" : 
				"2".equals(ftype) ? "jdcs55f00_2" : "jdcs55f00"
			};
			case "jdcs58100": return new String[] { "0".equals(ftype) ? "jdcs58100_0" :
				"1".equals(ftype) ? "jdcs58100_1" : 
				"2".equals(ftype) ? "jdcs58100_2" : "jdcs58100"
			};
			case "jdcs59000": return new String[] { !"jdcs59000_00t".equals(subName) ? "jdcs59000" :
				"0".equals(ftype) ? "jdcs59000_0" :
				"1".equals(ftype) ? "jdcs59000_1" : 
				"2".equals(ftype) ? "jdcs59000_2" : "jdcs59000"
			};
			case "jdcs59200": return new String[] { !"jdcs59200_00t".equals(subName) ? "jdcs59200" :
				"0".equals(ftype) ? "jdcs59200_0" :
				"1".equals(ftype) ? "jdcs59200_1" : 
				"2".equals(ftype) ? "jdcs59200_2" : "jdcs59200"
			};
			case "jdcs59300": return new String[] { !"jdcs59300_00t".equals(subName) ? "jdcs59300" :
				"0".equals(ftype) ? "jdcs59300_0" : 
				"1".equals(ftype) ? "jdcs59300_1" : 
				"2".equals(ftype) ? "jdcs59300_2" : "jdcs59300"
			};
			case "jdcs59500": return new String[] { !"jdcs59500_00t".equals(subName) ? "jdcs59500" :
				"0".equals(ftype) ? "jdcs59500_0" :
				"1".equals(ftype) ? "jdcs59500_1" : 
				"2".equals(ftype) ? "jdcs59500_2" : "jdcs59500"
			};
			case "jdcs5ac00": return new String[] { !"jdcs5ac00_00t".equals(subName) ? "jdcs5ac00" :
				"0".equals(ftype) ? "jdcs5ac00_0" :
				"1".equals(ftype) ? "jdcs5ac00_1" : 
				"2".equals(ftype) ? "jdcs5ac00_2" : "jdcs5ac00"
			};
			case "jdcs5ae00": return new String[] { !"jdcs5ae00_00t".equals(subName) ? "jdcs5ae00":
				"0".equals(ftype) ? "jdcs5ac00_0" :
				"1".equals(ftype) ? "jdcs5ac00_1" : 
				"2".equals(ftype) ? "jdcs5ac00_2" : "jdcs5ae00"
			};
			case "jdcs5ah00": return new String[] { !"jdcs5ah00_00t".equals(subName) ? "jdcs5ah00" :
				"0".equals(ftype) ? "jdcs5ah00_0" :
				"1".equals(ftype) ? "jdcs5ah00_1" : 
				"2".equals(ftype) ? "jdcs5ah00_2" : "jdcs5ah00"
			};
			case "jdcs5aj00": return new String[] { !"jdcs5aj00_00t".equals(subName) ? "jdcs5aj00" :
				"0".equals(ftype) ? "jdcs5aj00_0" :
				"1".equals(ftype) ? "jdcs5aj00_1" : 
				"2".equals(ftype) ? "jdcs5aj00_2" : "jdcs5aj00"
			};
			case "jdcs5aw00": return new String[] { !"jdcs5aw00_00t".equals(subName) ? "jdcs5aw00" :
				"0".equals(ftype) ? "jdcs5aw00_0" :
				"1".equals(ftype) ? "jdcs5aw00_1" : 
				"2".equals(ftype) ? "jdcs5aw00_2" : "jdcs5aw00"
			};
			case "jdcs5k000": return "jdcs5k000_00t".equals(subName) ? null : new String[] { "jdcs5k000" }; //jdcs5k000_00t: 來文機關詞庫查詢
			case "jdcs5z700": return new String[] { !"jdcs5z700_00t".equals(subName) ? "jdcs5z700" :
				"0".equals(ftype) ? "jdcs5z700_0" :
				"1".equals(ftype) ? "jdcs5z700_1" : 
				"2".equals(ftype) ? "jdcs5z700_2" : "jdcs5z700"
			};
			//case "jdcsa7300": return ("jdcsa7300_04t".equals(subName) || procName == null || procName.length() == 0) ? "jdcsa7300" : procName.toLowerCase();
		}
		return new String[] { name }; //扣除以上 switch-case 裡的特殊狀況, 一般畫面憑網址裡截出的程式代碼, 來判斷有無進入該畫面的權限
	}
	
	private void handleException(final HttpServletRequest request, final HttpServletResponse response, final Throwable t) throws ServletException, IOException {
    	log.error(t.getMessage(), t);
    	request.removeAttribute(JSP_EXCEPTION_KEY); //照一般塞 "javax.servlet.jsp.jspException" 的方式秀不出 error page 畫面(只會丟出 HTTP 500 error)
		request.setAttribute(ERROR_MESSAGE_KEY, ExceptionUtil.getRootMessage(t));
		request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
    }
}
