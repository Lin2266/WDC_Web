package com.test.commons.web.internal;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletRequest;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.exception.HttpHandleException;
import com.test.commons.util.StrUtil;

public class RequestParameterHelper {
    private final static Logger log = LoggerFactory.getLogger(RequestParameterHelper.class);
    private static final ThreadLocal<Matcher[]> _keyMatchersOfSecretValues = new ThreadLocal<Matcher[]>();

    /** 在一般遵循 HTML 4 標準的 AP Server 對 URI 字串所使用的編碼. 影響到 HttpServletRequest 之 getPathInfo(), getQueryString() 等 API 的編碼 */
    public static final String DEFAULT_APSERVER_URI_ENCODING = "ISO-8859-1";

    /** request 參數值預設欲使用的編碼. 如果在取參數值前未設定 ServletRequest.setCharacterEncoding() 者, 將採用此預設值 */
    public static final String DEFAULT_REQUEST_CHARACTER_ENCODING = "UTF-8";

    /** Part of HTTP content type header. */
    public static final String MULTIPART = "multipart/";
    /** HTTP content type header for multipart forms. */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    /** HTTP content type header for multiple uploads. */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    private HttpServletRequest request;
    private String uriEncoding; //AP Server 對 URI 字串所使用的編碼
    private String paramCharEncoding;
    private String uploadFileSaveDir; //檔案上傳後的存放目錄路徑
    private Long uploadFileMaxSize;  //單一檔案上傳大小的最大值 (單位: byte)
    private String uploadFileSavePrefix; //檔案上傳存檔後的檔名, 加前用的字串
    private String uploadFileSaveSuffix; //檔案上傳存檔後的檔名, 加後用的字串
    private String requestMethod; //GET, POST, PUT, DELETE, ...
    private String contentType;
    private Boolean isMultiPartRequest;

    private Map<String, String[]> queryStringMap; //cache
    private Map<String, List<String>> queryStringMap2; //cache
    private Map<String, String[]> parameterMap; //cache

    public RequestParameterHelper(HttpServletRequest request) {
        this.request = request;
    }

    public String getUriEncoding() {
		return (this.uriEncoding != null) ? this.uriEncoding : DEFAULT_APSERVER_URI_ENCODING;
	}

	public void setUriEncoding(String uriEncoding) {
		this.uriEncoding = uriEncoding;
	}

	public String getParamCharEncoding() {
		return (this.paramCharEncoding != null) ? this.paramCharEncoding :
			(this.request.getCharacterEncoding() != null) ? this.request.getCharacterEncoding() : DEFAULT_REQUEST_CHARACTER_ENCODING;
	}

	public void setParamCharEncoding(String paramCharEncoding) {
		this.paramCharEncoding = paramCharEncoding;
	}

	public String getUploadFileSaveDir() {
        return uploadFileSaveDir;
    }

    public void setUploadFileSaveDir(String uploadFileSaveDir) {
        this.uploadFileSaveDir = uploadFileSaveDir;
    }

    public Long getUploadFileMaxSize() {
        return uploadFileMaxSize;
    }

    public void setUploadFileMaxSize(Long uploadFileMaxSize) {
        this.uploadFileMaxSize = uploadFileMaxSize;
    }

    public String getUploadFileSavePrefix() {
        return uploadFileSavePrefix;
    }

    public void setUploadFileSavePrefix(String uploadFileSavePrefix) {
        this.uploadFileSavePrefix = uploadFileSavePrefix;
    }

    public String getUploadFileSaveSuffix() {
        return uploadFileSaveSuffix;
    }

    public void setUploadFileSaveSuffix(String uploadFileSaveSuffix) {
        this.uploadFileSaveSuffix = uploadFileSaveSuffix;
    }

    public String getRequestMethod() { //一律為大寫英文
    	if(this.requestMethod == null)
    		this.requestMethod = this.request.getMethod().toUpperCase();
    	return this.requestMethod;
    }

    public String getContentType() {
    	if(this.contentType == null)
    		this.contentType = this.request.getContentType();
    	return this.contentType;
    }

    /** server 收到的 request 是否為 multi-part 型態 */
    public boolean isMultiPartRequest() {
    	if(this.isMultiPartRequest == null) {
    		//boolean ismulti = ServletFileUpload.isMultipartContent(this.request); //Apache commons FileUpload.isMultipartContent(HttpServletRequest) 只承認 POST request
    		final String contentType = getContentType();
    		final boolean ismulti = ("POST".equals(getRequestMethod()) || "PUT".equals(getRequestMethod())) && //參考 FileUpload.isMultipartContent()
    				contentType != null && contentType.length() > MULTIPART.length() && contentType.regionMatches(true, 0, MULTIPART, 0, MULTIPART.length());
    		this.isMultiPartRequest = ismulti;
    	}
        return this.isMultiPartRequest;
    }

    /**
     * 仿製 ServletRequest#getParameterMap().
     * <ul>
     *     <li>URI encoding: 按 HTTP 4 標準, 應預設為 ISO-8859-1, 但不保證每家 AP Server 皆是如此處理
     *     <li>GET request: 依 URI encoding 設定而定, 但不保證每家 AP Server 皆是如此處理
     *     <li>至 Servlet Spec 3.0 為止, method=非POST 的 ServletRequest.getParameterMap() 只對透過 query-string 傳遞的參數有作用
     *     <li>理應只有 POST request 可傳遞 content-type=multipart/form-data 內容<br/>
     *         參考 https://issues.apache.org/jira/browse/FILEUPLOAD-197 <br/><br/>
     *
     *         "PUT means the sent representation is the replacement value for the target resource.
     *         A server could certainly support that functionality using any container format,
     *         it wouldn't be "normal" to use a MIME multipart, nor is it expected to be supported
     *         by the file upload functionality defined for browsers in RFC1867." <br/><br/>
     *
     *         "If you want to PUT a package, I suggest defining a resource that can be represented
     *         by an efficient packaging format (like ZIP) and then using PUT on that resource
     *         to have the side-effect of updating the values of its subsidiary resources."<br/><br/>
     *
     *         by Roy T. Fielding (06/Mar/13 21:25)
     *     <li>本工具處理方式:
     *         <ul>
     *         	   <li>經過本工具包裝處理後的 request 物件, request body (payload) 內的參數均已消費完畢, request.getParameterMap() 之類的函數將取不到
     *             <li>非 multipart request (假設為 content-type=application/x-www-form-urlencoded):
     *                 <ul>
     *                     <li>不呼叫 ServletRequest.getParameterMap() 取參數
     *                     <li>無論 GET/POST/PUT/DELETE/... request, 字元編碼皆受 ServletRequest.setCharacterEncoding() 設定的控制
     *                     <li>自 request payload 取參數, 並完成轉碼的動作
     *                     <li>自 query string 取參數, 並完成轉碼的動作
     *                 </ul>
     *             <li>multipart request:
     *                 <ul>
     *                     <li>HTML form 需具備 enctype="multipart/form-data" 屬性, 或者送出的 request 的 content-type 開頭為 multipart/
     *                     <li>需以 POST(或 PUT) 型態傳送 request
     *                     <li>以具備 type="file" 屬性的 input 欄位代表上傳的檔案
     *                     <li>如果同一欄位需可同時多選上傳檔案, 可在 input 欄位加 multiple="multiple" 屬性
     *                     <li>先自 query string 取參數, 並完成轉碼的動作
     *                     <li>使用 file upload 元件取 form entity 內所有欄位的值(return 的物件內, 存放的是已上傳檔案的實體路徑)
     *                 </ul>
     *         </ul>
     * </ul>
     */
	public Map<String, String[]> getParameterMap() {
    	try {
	        if(this.parameterMap != null)
	            return this.parameterMap;

	        //workaround: 部分 ap server (如 Oracle 早期以 Orion 為基礎的 AP Server)在某些情況下, 不預先呼叫 ServletRequest.getParameter(),
	        //直接呼叫 ServletRequest.getParameterMap() 或 Apache ServletFileUpload 會取不到值或取錯值
	        //this.request.getParameter("foo");

	        //一般非 multi-part request
	        if(!isMultiPartRequest())
	        	return (this.parameterMap = getNonMultipartParameterMap());

	        //multi-part request, 取 payload (entity) 及 query string 內的參數:
	        //multi-part 裡的 type=file 欄位資料必須存為實體檔案 (理應只有 POST 可傳送 multi-part request(見本函數註釋之 Roy T. Fielding 大老的文字))
	        return (this.parameterMap = getMultipartParameterMap());
    	} catch(Throwable t) {
            throw new HttpHandleException(t.getMessage(), t);
        }
    }

    public Map<String, String[]> getParameterMapFromQueryString() {
		if(this.queryStringMap != null)
			return this.queryStringMap;
		return (this.queryStringMap = ensureGetParameterMap(getParameterMapFromQueryString2()));
    }

    public Map<String, List<String>> getParameterMapFromQueryString2() {
    	try {
    		if(this.queryStringMap2 != null)
    			return this.queryStringMap2;

    		final Map<String, List<String>> ret = new HashMap<String, List<String>>();
    		rawQueryStringToMap(ret, this.request.getQueryString(), getUriEncoding(), getParamCharEncoding());
    		return (this.queryStringMap2 = ret);
    	} catch(Throwable t) {
            throw new HttpHandleException(t.getMessage(), t);
        }
    }

    /**
     * 仿照 ServletRequest#getParameterValues(String)
     * @param parameterName
     * @return
     */
    public String[] getParameterValues(final String parameterName) {
        return getParameterMap().get(parameterName);
    }

    /**
     * 仿照 ServletRequest#getParameter(String)
     * @param parameterName
     * @return
     */
    public String getParameter(final String parameterName) {
        final String[] v = getParameterValues(parameterName);
        if(v != null && v.length != 0)
            return v[0];
        return null;
    }

    /**
     * 無論參數來自 query string 或 payload, 一律組合成可置於 request URI 後作為參數的字串的型式.
     */
    public String toQueryStringForDebugging(final boolean forLog) {
    	final Map<String, String[]> paramMap = getParameterMap();
    	final StringBuilder sb = new StringBuilder();
    	for(Map.Entry<String, String[]> entry : paramMap.entrySet()) {
    		final String k = entry.getKey();
    		final String[] v = entry.getValue();
    		final boolean hideSecretValues = forLog && matchSecretKeys(k);
    		for(int i = 0; i < v.length; i++) {
    			sb.append(k).append("=").append(hideSecretValues ? "***" : v[i]).append("&");
    		}
    	}
    	if(sb.length() > 0)
    		sb.deleteCharAt(sb.length() - 1);
    	return sb.toString();
    }

    ///**
    // * 把 request query string 型式的字串化為 Map 集合.<br/>
    // * 如: name1=value1&name2=value2&name3=value3... 型式的字串,<br/>
    // * 化為如 { "name1":["value1", ... ], "name2":["value2", ...], "name3":["value3", ...] } 型式的 Map
    // */
    //public static Map<String, String[]> queryStringToMap(final String str, final String charEncoding) {
    //	final Map<String, List<String>> result = queryStringToMap2(str, charEncoding);
    //	if(result.size() == 0)
    //		return new HashMap<String, String[]>(0);
    //	Map<String, String[]> ret = new HashMap<String, String[]>((int)(result.size() / 0.75 + 1), 0.75F);
    //	for(String k : result.keySet()) {
    //		List<String> v = result.get(k);
    //		ret.put(k, (v == null) ? null : v.toArray(new String[v.size()]));
    //	}
    //	return ret;
    //}

    /**
     * 把 request query string 型式的字串化為 Map 集合.<br/>
     * 如: name1=value1&name2=value2&name3=value3... 型式的字串,<br/>
     * 化為如 { "name1":["value1", ... ], "name2":["value2", ...], "name3":["value3", ...] } 型式的 Map
     */
    public static Map<String, List<String>> queryStringToMap2(final String str, final String charEncoding) {
    	try {
    		if(isEmpty(str))
    			return new HashMap<String, List<String>>(0);

	    	final Map<String, List<String>> ret = new HashMap<String, List<String>>();
	    	urlencodedQueryStringToMap(ret, str, charEncoding);
	    	return ret;
    	} catch(Throwable t) {
    		throw new HttpHandleException(t.getMessage(), t);
    	}
    }

    /**
	 * 取 request body 內容(即 payload).<br>
	 * 注意: request.getParameterMap() 後, 本 method 就取不到內容了, 反之亦然
	 * @param request 在沒有使用 HttpServletRequestWrapper 包裹的情況下, 本 method 取出 payload 後, request 物件將無法再次取得 request 資料
	 * @param charEncoding
	 * @see #DEFAULT_REQUEST_CHARACTER_ENCODING
	 */
    public static String getRequestPayload(final ServletRequest request, final String charEncoding) {
		Reader in = null;

    	try {
    		final StringBuilder ret = new StringBuilder();
    		final char[] buff = new char[128];
    		in = new BufferedReader(new InputStreamReader(request.getInputStream(), charEncoding));
    		for(int n = 0; (n = in.read(buff)) != -1; )
    			ret.append(buff, 0, n);
    		in.close();
    		in = null;
    		return ret.toString();
    	} catch(Throwable t) {
    		throw new HttpHandleException(t.getMessage(), t);
    	} finally {
    		if(in != null) try { in.close(); } catch(Throwable t) {}
    	}
    }
    
    public static boolean matchSecretKeys(final String key) {
    	Matcher[] mm = _keyMatchersOfSecretValues.get();
    	if(mm == null) {
    		mm = new Matcher[] { //敏感內容儘量別露出於 log 
    				Pattern.compile("password", Pattern.CASE_INSENSITIVE).matcher(key), 
    				Pattern.compile("passwd", Pattern.CASE_INSENSITIVE).matcher(key)
				};
    		_keyMatchersOfSecretValues.set(mm);
    		
    		for(int i = 0; i < mm.length; i++) {
    			if(mm[i].find())
    				return true;
    		}
    	} else {
    		for(int i = 0; i < mm.length; i++) {
    			if(mm[i].reset(key).find())
    				return true;
    		}
    	}
    	return false;
    }

    private Map<String, String[]> getNonMultipartParameterMap() throws IOException {
		final Map<String, List<String>> result = new HashMap<String, List<String>>();

		//from request body (payload)
		if(!"GET".equals(getRequestMethod()))
			urlencodedQueryStringToMap(result, getRequestPayload(this.request, getParamCharEncoding()), getParamCharEncoding());
		//from query string
		rawQueryStringToMap(result, this.request.getQueryString(), getUriEncoding(), getParamCharEncoding());

		return ensureGetParameterMap(result);
	}

    private Map<String, String[]> getMultipartParameterMap() throws Exception {
    	//multi-part 裡的 type=file 欄位資料必須存為實體檔案 (理應只有 POST 可傳送 multi-part request(見本函數註釋之 Roy T. Fielding 大老的文字))
        if(getUploadFileSaveDir() == null)
            throw new IllegalArgumentException("uploadFileSaveDir property not set");

        final long fileUploadMaxSize = (getUploadFileMaxSize() == null) ? BackingBeanHelper.DEFAULT_FILE_UPLOAD_MAX_SIZE_BYTES : getUploadFileMaxSize();
        final File saveDir = new File(getUploadFileSaveDir());
        if(!saveDir.isDirectory())
            saveDir.mkdirs();

        final Map<String, List<String>> result = new HashMap<String, List<String>>(); //放所有欄位
        final Map<String, List<String>> params1 = new HashMap<String, List<String>>(); //專門用來放置 multi-part request 的 type=file 的欄位值
        final DiskFileItemFactory factory = new DiskFileItemFactory(1024 * 1024, saveDir);
        final ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding(getParamCharEncoding());

        for(FileItem item : (List<FileItem>)upload.parseRequest(this.request)) {
            if(item == null)
                continue;
            
            final String parameterName = item.getFieldName(); //request 參數名

            if(item.isFormField()) { //form 裡一般欄位值,  但 type="file" 的欄位值也可能出現在這裡
                ensureGetParameterValues(result, parameterName).add(item.getString(getParamCharEncoding())); //不指定 encoding 中文就會有問題
            } else { //type="file"
            	//取原始檔名
                String fileName = item.getName().trim(); //可能含 client 端的路徑字串
                if("".equals(fileName))
                    continue;
                
                //限制檔案大小
                if(item.getSize() > fileUploadMaxSize)
                	throw new IllegalArgumentException("uploaded file \"" + fileName + "\" size (" + item.getSize() + " bytes) exceeds size limit (" + fileUploadMaxSize + " bytes)");

                //workaround: server 為 Unix, client 為 Windows 時, 仍會把 local 路徑當成檔名的一部分
                fileName = URLDecoder.decode(new File(fileName).getName(), getParamCharEncoding()); //檔名有可能也是 urlencoded 過的
                int i;
                if((i = fileName.lastIndexOf("\\")) != -1 && fileName.length() != ++i) //非以 "\" 結尾
                	fileName = fileName.substring(i); //取最後的 "\" 之後的字串

                //加前加後成新檔名
                if(!StrUtil.isEmpty(getUploadFileSavePrefix()))
                    fileName = getUploadFileSavePrefix() + fileName;
                if(!StrUtil.isEmpty(getUploadFileSaveSuffix()))
                    fileName += getUploadFileSaveSuffix();

				//欲存為實體檔案的 File 物件, 如果已存在同檔名的檔案, 在最後的副檔名前加 .n
                File savedFile = new File(saveDir, fileName);
                if(savedFile.isFile()) { //同檔名檔案已存在者, 在副檔名前加序號, 如 abc.xyz, abc.1.xyz, abc.2.xyz, ...
                	String fileNameFrontPart = null, fileNameBackPart = null;
                    int dot = fileName.lastIndexOf('.');
                    if(dot > 0 && dot < (fileName.length() - 1)) {
                    	fileNameFrontPart = fileName.substring(0, dot);
                    	fileNameBackPart = fileName.substring(dot + 1); //副檔名
                    } else {
                    	fileNameFrontPart = fileName;
                    	fileNameBackPart = null;
                    }
	                for(int m = 1; savedFile.isFile();  m++) {
	                	String fileName2 = fileNameFrontPart + "." + m;
	                	if(fileNameBackPart != null)
	                		fileName2 += "." + fileNameBackPart;
	                    savedFile = new File(saveDir, fileName2);
	                }
                }

				//寫入 disk 檔案
                item.write(savedFile);
                if(log.isDebugEnabled())
                	log.debug("file uploaded: " + savedFile.getAbsolutePath());

                //欄位名->[實際檔案實體路徑] 列為 request 參數
                ensureGetParameterValues(params1, parameterName).add(savedFile.getAbsolutePath());
            }
        }
        result.putAll(params1); //如此 type=file 的欄位值不會因也被視同一般欄位而重覆地被置於 params 中

        //parameters from query string
  		rawQueryStringToMap(result, this.request.getQueryString(), getUriEncoding(), getParamCharEncoding());

  		return ensureGetParameterMap(result);
    }

    private static void rawQueryStringToMap(final Map<String, List<String>> out, final String rawQueryString, final String encFrom, final String encTo)
    		throws IOException {
		if(out != null && !isEmpty(rawQueryString)) {
			final String queryString = isEqual(encFrom, encTo) ? rawQueryString : new String(rawQueryString.getBytes(encFrom), encTo); //此時的 query string 仍可能是 URLEncode 編碼後的字串
			urlencodedQueryStringToMap(out, queryString, encTo);
		}
    }

    private static void urlencodedQueryStringToMap(final Map<String, List<String>> out, final String urlencodedQueryString, final String charEncoding) throws IOException {
    	if(out != null && !isEmpty(urlencodedQueryString)) {
	    	final List<String> params = split(urlencodedQueryString, "&");

	    	//參考 Spring org.springframework.http.converter.FormHttpMessageConverter 之 source
	    	for(String param : params) {
	    		String name = null, value = null;
	    		final int idx = param.indexOf('=');
	    		if(idx == -1) {
	    			name = URLDecoder.decode(param, charEncoding);
	    		} else {
	    			name = URLDecoder.decode(param.substring(0, idx), charEncoding);
	    			value = URLDecoder.decode(param.substring(idx + 1), charEncoding);
	    		}

	    		ensureGetParameterValues(out, name).add(value);
	    	}
    	}
    }

    //自 params 取參數值(type List<String>), 無對應參數者, 補一個空的 List<String> 物件進去
    private static List<String> ensureGetParameterValues(final Map<String, List<String>> params, final String parameterName) {
        List<String> parameterValues = params.get(parameterName); //request 參數值
        if(parameterValues == null) {
            parameterValues = new ArrayList<String>(2); //假設一般欄位以單值為占大多數情形
            params.put(parameterName, parameterValues);
        }
        return parameterValues;
    }

    static Map<String, String[]> ensureGetParameterMap(final Map<String, List<String>> params) {
    	if(params == null || params.size() == 0)
    		return new HashMap<String, String[]>(0);

    	final Map<String, String[]> ret = new HashMap<String, String[]>((int)(params.size() / 0.75 + 1), 0.75F);
    	for(Map.Entry<String, List<String>> entry : params.entrySet()) {
    		final String k = entry.getKey();
    		final List<String> v = entry.getValue();
    		ret.put(k, (v == null) ? null : v.toArray(new String[v.size()]));
    	}
    	return ret;
    }

    static List<String> split(final String s, final String delimiters) {
    	if(s == null)
    		return new ArrayList<String>(0);

    	final StringTokenizer st = new StringTokenizer(s, delimiters);
    	final List<String> tokens = new ArrayList<String>();
    	while(st.hasMoreTokens()) {
    		String token = st.nextToken();
    		if(token.length() > 0)
    			tokens.add(token);
    	}
    	return tokens;
    }

    private static boolean isEqual(final String s1, final String s2) {
    	return s1 != null && s2 != null && s1.length() == s2.length() && s1.equals(s2);
    }

    private static boolean isEmpty(final String s) {
    	return s == null || s.length() == 0;
    }
}
