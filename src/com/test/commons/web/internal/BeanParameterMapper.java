package com.test.commons.web.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.test.commons.annotation.Action;
import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.DefaultAction;
import com.test.commons.annotation.Input;
import com.test.commons.annotation.Payload;
import com.test.commons.annotation.Rest;
import com.test.commons.util.DateUtil;
import com.test.commons.util.JSONArray;
import com.test.commons.util.JSONObject;
import com.test.commons.util.MsgUtil;
import com.test.commons.util.StrUtil;
import com.test.commons.util.internal.SpringClassUtil;

/**
 * HttpServlet request 參數, 對應至 backing-bean 屬性及 action method 參數的工具.
 * <ol>
 *   <li>populate() method (將 request 參數值結合至 bean 屬性規則):
 *     <ul>
 *       <li>與 request 參數對應的 bean 屬性, 要標注以 &#64;Input
 *       <li>&#64;Input 可標注於 bean field, getter 或 setter method 之一
 *       <li>即使 &#64;Input 標注於 bean field, 仍須透過 setter 來替 bean 屬性設值(因是對 backing-bean 增強過的 proxy 物件操作設值取值之故)
 *       <li>request 參數名與 bean 屬性名不同者, 可利用 &#64;Input(name="REQUEST_PARAM_NAME") 明確指明 HTML 輸入欄位的 name 屬性值
 *     </ul>
 *   <li>invokeAction() method (將 request 參數結傳入並呼叫 action method)
 *     <ul>
 *       <li>action method 標注以 &#64;Action, &#64;AjaxAction, &#64;Rest 等才能被 invokeAction() 呼叫
 *       <li>action method 參數, 標注以 &#64;Input(name="REQUEST_PARAM_NAME") 才能收到對應的參數值 (NOTE: Java method 參數名稱在 compile 成 bytecode 後即換為內部代號, 故需以 annotation 註明參數名稱)
 *       <li>標注 &#64;Rest 的 action method, 有一參數標注以 &#64;Payload 者, request payload 內容將透過該參數傳入. 
 *           但此前 polulate() 及 invokeAction() 只能取來自 request query string 的參數值, 否則會把 payload 內容消費完, 使帶 &#64;Payload 的參數無以得到 payload 內容
 *       <li>帶 &#64;Payload 的 action method 參數, 其型態若為 byte[] 或 File 者, 將會得到完整 payload 內容, 其他型態則各自比照 bean 屬性/action method 參數綁定值的規則
 *     </ul>
 *   <li> (將 bean 屬性設至 request attribute 規則): TODO
 *   <li>所有 bean 屬性的 getter method 都將被呼叫以取值(除去被標注以 &#64;Action, &#64;AjaxAction, &#64;Rest 的 method)
 * </ol>
 */
public final class BeanParameterMapper {
	private static final Logger log = LoggerFactory.getLogger(BeanParameterMapper.class);

	private static final int TYPE_UNKNOWN = -1;
	private static final int TYPE_STRING = 1; //String
	private static final int TYPE_STRINGS = 2; //String[]
	private static final int TYPE_BYTE = 3; //byte
	private static final int TYPE_BYTES = 4; //byte[]
	private static final int TYPE_BYTE_OBJ = 5; //Byte
	private static final int TYPE_BYTE_OBJS = 6; //Byte[]
	private static final int TYPE_SHORT = 7; //short
	private static final int TYPE_SHORTS = 8; //short[]
	private static final int TYPE_SHORT_OBJ = 9; //Short
	private static final int TYPE_SHORT_OBJS = 10; //Short[]
	private static final int TYPE_INT = 11; //int
	private static final int TYPE_INTS = 12; //int[]
	private static final int TYPE_INT_OBJ = 13; //Integer
	private static final int TYPE_INT_OBJS = 14; //Integer[]
	private static final int TYPE_LONG = 15; //long
	private static final int TYPE_LONGS = 16; //long[]
	private static final int TYPE_LONG_OBJ = 17; //Long
	private static final int TYPE_LONG_OBJS = 18; //Long[]
	private static final int TYPE_FLOAT = 19; //float
	private static final int TYPE_FLOATS = 20; //float[]
	private static final int TYPE_FLOAT_OBJ = 21; //Float
	private static final int TYPE_FLOAT_OBJS = 22; //Float[]
	private static final int TYPE_DOUBLE = 23; //double
	private static final int TYPE_DOUBLES = 24; //double[]
	private static final int TYPE_DOUBLE_OBJ = 25; //Double
	private static final int TYPE_DOUBLE_OBJS = 26; //Double[]
	private static final int TYPE_BOOLEAN = 27; //boolean
	private static final int TYPE_BOOLEANS = 28; //boolean[]
	private static final int TYPE_BOOLEAN_OBJ = 29; //Boolean
	private static final int TYPE_BOOLEAN_OBJS = 30; //Boolean[]
	private static final int TYPE_CHAR = 31; //char
	private static final int TYPE_CHARS = 32; //char[]
	private static final int TYPE_CHAR_OBJ = 33; //Character
	private static final int TYPE_CHAR_OBJS = 34; //Character[]
	private static final int TYPE_DATE = 35; //java.util.Date
	private static final int TYPE_DATES = 36; //java.util.Date[]
	private static final int TYPE_FILE = 37; //File
	private static final int TYPE_FILES = 38; //File[]
	private static final int TYPE_JSONOBJECT = 39; //com.tatung.commons.util.JSONObject
	private static final int TYPE_JSONARRAY = 40; //com.tatung.commons.util.JSONArray
	private static final int TYPE_HTTP_SERVLET_REQUEST = 100; //javax.servlet.http.HttpServletRequest.   ***自此以下不以 typeCode(Class<?>) 判斷型態碼***
	private static final int TYPE_HTTP_SERVLET_RESPONSE = 101; //javax.servlet.http.HttpServletResponse
	private static final int TYPE_HTTP_SESSION = 102; //javax.servlet.http.HttpSession
	private static final int TYPE_OUTPUT_STREAM = 110; //(暫無用)
	private static final int TYPE_VALUE_HOLDER_STRING = 111; //(暫無用)
	private static final int TYPE_VALUE_HOLDER_LONG = 112; //(暫無用)
	private static final int TYPE_VALUE_INPUT_COMMAND = 113; //專用以對應 request parameters 用的 Java Bean (SpringMVC 文件裡稱作 command object)
	
	/** 前端 request 參數中, 多值參數欲與 backing bean 的 null 陣列屬性值對應者, 應傳送 "一個" 該參數且值為本常數值(EMPTY_ARRAY_VALUE 所代表的值) */
	public static final String EMPTY_ARRAY_PARAMETER_VALUE = "_%20_"; //unicode " "
	
	/** 日期格式錯誤的訊息(應納為 i18n resource boundle 的 key) */
	public static final String MSG_ERROR_DATE_FORMAT = "date string format error";

	public static final int INVOKE_AS_ACTION = 1;
	public static final int INVOKE_AS_AJAX_ACTION = 2;
	public static final int INVOKE_AS_REST = 4;
	public static final int INVOKE_AS_DEFAULT_ACTION = 5;
	private static final Map<String, ClassDescriptor> _beanDescCache = new ConcurrentHashMap<String, ClassDescriptor>(); //key: backing bean class name (可能是 enhanced 的)
	//NOTE: 並未使用 commons-beanutils PropertyUtils 處理 bean properties 值
	
	/** 清除內含暫存的 bean property 資料 */
	public static void clear() {
		_beanDescCache.clear();
	}
	
	/**
	 * 由 parameters 取值, 而對被 &#64;Input 修飾的 bean field/property 設值.<br>
	 * 順序: property 先於 field, 有數個被同一 &#64;Input 修飾的 field/property param 者, 第二個 field/property 將被忽略.
	 */
	public static void populate(final Object bean, final Map<String, String[]> parameters, final ClassDescriptor classDesc) {
		final StringBuilder debugMsg = log.isDebugEnabled() ? new StringBuilder() : null;
		populate(bean, parameters, classDesc, debugMsg);
		if(debugMsg != null && debugMsg.length() != 0)
			log.debug("populate {}: {}", bean.getClass().getSimpleName(), debugMsg);
	}
	
	private static void populate(final Object bean, final Map<String, String[]> parameters, final ClassDescriptor classDesc, final StringBuilder debugMsg) {
		try {
			//bean class 可能因為裡面被 cglib 作 enhance 處理, 須取得未 enhance 前的 class (TODO: 恐怕也有可能是 JDK1.5 以後的動態 proxy class)
			//Class<?> clazz = bean.getClass();
			//Class<?> clazz0 = ClassUtil.getUnenhancedClass(clazz);
	
			for(int i = 0; i < classDesc.propDescs.length; i++) { //對 bean property 設值
				final PropDescriptor pd = classDesc.propDescs[i];
				if(pd.parameterName != null &&  //有標注 @Input 的 bean property 者才有 parameterName 值
						pd.propType != TYPE_HTTP_SERVLET_REQUEST && pd.propType != TYPE_HTTP_SERVLET_RESPONSE && pd.propType != TYPE_HTTP_SESSION && //不讓 request/response/session 物件綁在 bean properties 上以免形成記憶空間漏洞 
						parameters.containsKey(pd.parameterName)) {
					setValue(bean, pd, parameters.get(pd.parameterName), debugMsg);
				}
			}
		} catch(Throwable t) {
			throw new RuntimeException(t.getMessage(), t);
		}
	}
	
	/**
	 * invoke backing bean action method
	 * @param invokeType INVOKE_AS_ACTION/INVOKE_AS_AJAX_ACTION/INVOKE_AS_DOWNLOAD_ACTION
	 * @return action method return value
	 */
	public static Object invokeAction(final HttpServletRequest request, final HttpServletResponse response, final Object bean, 
			final ClassDescriptor classDesc, final String methodName, final Map<String, String[]> parameterValues, 
			final OutputWrapper outputWrapper, final int invokeType) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException, InstantiationException {
		if(methodName == null || methodName.length() == 0) { //未指定 action method 時
			if(invokeType == INVOKE_AS_ACTION) { //目前允許執行 default action 的呼叫法
				for(int i = 0, ii = classDesc.methodDescs.length; i < ii; i++) { //試執行 default action
					final ActionMethodDescriptor md = classDesc.methodDescs[i];
					if(md.invokeType == INVOKE_AS_DEFAULT_ACTION) {
						invokeAction(request, response, bean, classDesc, md, parameterValues, outputWrapper, INVOKE_AS_DEFAULT_ACTION);
						return null; //default action method 不令回傳任何東西
					}
				}
			}
            return null;
		}
		for(int i = 0, ii = classDesc.methodDescs.length; i < ii; i++) { //找 action method
			final ActionMethodDescriptor md = classDesc.methodDescs[i];
			if(md.methodName.equals(methodName))
				return invokeAction(request, response, bean, classDesc, md, parameterValues, outputWrapper, invokeType);
		}
		throw new NoSuchMethodException("No such action method " + classDesc.nonEnhancedClass.getName() + "." + methodName + "(...)");
	}
	
	/**
	 * invoke backing bean action method (非 default action method)
	 * @param invokeType INVOKE_AS_ACTION/INVOKE_AS_AJAX_ACTION/INVOKE_AS_DOWNLOAD_ACTION/INVOKE_AS_REST
	 * @return action method return value
	 */
	public static Object invokeAction(final HttpServletRequest request, final HttpServletResponse response, final Object bean,
            final ClassDescriptor classDesc, final ActionMethodDescriptor methodDesc, final Map<String, String[]> parameterValues,
            final OutputWrapper outputWrapper, final int invokeType) 
    		throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, SecurityException, IntrospectionException {
		if(methodDesc == null)
			throw new NoSuchMethodError("unknown action method for backing bean " + classDesc.nonEnhancedClass.getName());
		if(methodDesc.invokeType != invokeType && !(methodDesc.invokeType == INVOKE_AS_DEFAULT_ACTION && invokeType == INVOKE_AS_ACTION))
			throw new NoSuchMethodException("No such action method " + classDesc.nonEnhancedClass.getName() + "." + methodDesc.methodName + "(...) annotated with @" + invokeTypeAnnotationName(invokeType));
		if(methodDesc.usePayload && invokeType != INVOKE_AS_REST) //NOTE: 把 request payload 整個傳進來的方式, 目前只讓用在 RESTFul 呼叫
			throw new NoSuchMethodException(classDesc.nonEnhancedClass.getName() + "." + methodDesc.methodName + "(...): parameter for passing request payload can only exist in the action method annotated with @" + Rest.class.getSimpleName());
		
		final StringBuilder debugMsg = !log.isDebugEnabled() ? null : new StringBuilder().append("invoke: ").append(classDesc.nonEnhancedClass.getSimpleName()).append(".").append(methodDesc.methodName).append("(");
        final Object[] args = new Object[methodDesc.params.length]; //action method 的參數. 把 request/response/parameterValues 的值配給 args 成員
        
        //把 request 參數值配給 action method 參數
        //NOTE: 如果 method 參數中含有一接收 request payload 的參數, 其他 method 參數就只能接經由 request query string 傳遞而來的參數值了,
        //      此時 parameterValues 須為來自 request query string
        for(int i = 0, ii = args.length, iii = ii - 1; i < ii; i++) { //for each action method parameter
        	final ActionMethodParamDescriptor paramDesc = methodDesc.params[i];
        	if(paramDesc == null) { //沒標注 annotation, 非 HttpServletRequest/HttpServletResponse/HttpSession type 的參數
        		if(debugMsg != null) debugMsg.append("?").append((i == iii) ? "" : ", ");
        		continue;
        	}
            
        	//先針對 action method parameter 型態為 HttpServletRequest/HttpServletResponse/HttpSession 型態者(不需以 @Input 標註)
        	if(paramDesc.typeCode == TYPE_HTTP_SERVLET_REQUEST) {
                args[i] = request;
                if(debugMsg != null) debugMsg.append("(HttpServletRequest)").append((i == iii) ? "" : ", ");
            } else if(paramDesc.typeCode == TYPE_HTTP_SERVLET_RESPONSE) {
                args[i] = response;
                if(debugMsg != null) debugMsg.append("(HttpServletResponse)").append((i == iii) ? "" : ", ");
            } else if(paramDesc.typeCode == TYPE_HTTP_SESSION) {
                args[i] = request.getSession();
                if(debugMsg != null) debugMsg.append("(HttpSession)").append((i == iii) ? "" : ", ");
                
        	//其他以 @Input/@Payload 標註的 action method parameter
            } else {
            	if(paramDesc.usePayload) { //NOTE: 只要有一 action method 參數使用 payload, 本 function 的呼叫者就必須把 request query string 內的參數傳給 parameterValues
            		if(paramDesc.typeCode == TYPE_BYTES) {
                		args[i] = RequestPayloadHelper.getRequestPayloadAsBytes(request); //此後 payload 內含的參數就再也取不出了(以下亦同)
                		if(debugMsg != null) debugMsg.append("(bytes)").append((i == iii) ? "" : ", ");
            		} else if(paramDesc.typeCode == TYPE_FILE) {
                		args[i] = RequestPayloadHelper.getRequestPayloadAsFile(request);
                		if(debugMsg != null) debugMsg.append(((File)args[i]).getAbsolutePath()).append((i == iii) ? "" : ", ");
            		} else {
            			args[i] = convertType(RequestPayloadHelper.getRequestPayload(request), paramDesc.typeCode);
            			if(debugMsg != null) debugMsg.append("--- payload start ---\n").append((String)args[i]).append("\n--- end of payload ---").append((i == iii) ? "" : ", ");
            		}
            		
            	} else if(paramDesc.typeCode == TYPE_VALUE_INPUT_COMMAND) { //command object, 裝載 request 參數值
            		final ClassDescriptor desc = getBeanDescriptor(paramDesc.commandObjectType, false);
            		final Object cmd = paramDesc.commandObjectType.newInstance();
            		if(debugMsg != null) debugMsg.append("(").append(paramDesc.commandObjectType.getSimpleName()).append(":");
            		populate(cmd, parameterValues, desc, debugMsg);
            		args[i] = cmd;
            		if(debugMsg != null) debugMsg.append((i == iii) ? ")" : "), ");
            		
            	} else { //@Input
            		final String[] paramValue = parameterValues.get(paramDesc.paramName); //由 request 傳來的參數值
            		final boolean secretParam = log.isDebugEnabled() && RequestParameterHelper.matchSecretKeys(paramDesc.paramName);
            		
            		if(!paramDesc.isArray) { //參數非 array
                        final Object value2 = convertType((paramValue == null || paramValue.length == 0 || "".equals(paramValue[0])) ? (String)null :
                                EMPTY_ARRAY_PARAMETER_VALUE.equals(paramValue[0])? null : paramValue[0], paramDesc.typeCode);
                        args[i] = value2;
                        if(debugMsg != null) debugMsg.append(secretParam ? "***" : value2).append((i == iii) ? "" : ", ");
                    } else { //參數為 array
                    	if(paramValue != null && paramValue.length != 0 && !(paramValue.length == 1 && EMPTY_ARRAY_PARAMETER_VALUE.equals(paramValue[0]))) {
                            args[i] = convertTypes(paramValue, paramDesc.typeCode, secretParam, debugMsg);
	                        if(debugMsg != null) debugMsg.append((i == iii) ? "" : ", ");
                    	} else {
                    		if(debugMsg != null) debugMsg.append("null").append((i == iii) ? "" : ", ");
                    	}
                    }
            	}
            }
        } //end of: for each action method parameter
        
        if(debugMsg != null && debugMsg.length() != 0 && log.isDebugEnabled())
            log.debug(debugMsg.append(")").toString());
		//執行 action method
        return methodDesc.method.invoke(bean, args);
	}
	
	/** 
	 * 取 backing bean 之 property 或 action method 資訊 (for BackingBean special)
	 * @param clazz (可為為 enhanced)
	 */
	public static ClassDescriptor getBeanDescriptor(final Class<?> clazz) 
			throws IntrospectionException, NoSuchMethodException, SecurityException {
		return getBeanDescriptor(clazz, true);
	}
	
	private static ClassDescriptor getBeanDescriptor(final Class<?> clazz, final boolean isBackingBean) 
			throws IntrospectionException, NoSuchMethodException, SecurityException {
		final String className = clazz.getName();
		
		//先自 cache 找
		ClassDescriptor ret = _beanDescCache.get(className);
		
		if(ret == null) {
			final Class<?> nonEnhancedClass = SpringClassUtil.getUnenhancedClass(clazz);
			final Map<String, PropDescriptor> propDescriptors = new HashMap<String, PropDescriptor>(); //key: backing bean property name, or action method name
			final BackingBean beanAnnotation = nonEnhancedClass.getAnnotation(BackingBean.class);
			
	        if(beanAnnotation == null && isBackingBean)
	        	throw new IllegalArgumentException("backing bean " + nonEnhancedClass.getName() + " not annotated with @" + BackingBean.class.getSimpleName());
			
			//(1)scan bean properties(即 gettXXX()/setXXX(), 包括母類別的屬性). ** 注意 **: nonEnhancedClass 若為經 CGLIB 增強過的 class, 則無法循正常繼承體系向上尋得被標注以 annotation 的 method
	        for(PropertyDescriptor desc : Introspector.getBeanInfo(nonEnhancedClass).getPropertyDescriptors()) {
	            String propName = desc.getName();
	            if("class".equals(propName))
	                continue;
	            final Method setter = desc.getWriteMethod(); //public (<= not enhanced class)
	            final Method getter = desc.getReadMethod(); //public (<= not enhanced class)
	            if(setter == null) //強制只能透過 property setter 來設值
	            	continue;
	            
	            //略去形似屬性但不被用來與 @Input 搭配的 method (method 有可能被取名為 getXXX() 或 setXXX() 的型式)
	            //略去標注以 @Action, @AjaxAction, @DefaultAction, @Rest, @Value 等之 getXXX()/setXXX() method
	            if(setter.getAnnotation(Action.class) != null || setter.getAnnotation(AjaxAction.class) != null || setter.getAnnotation(DefaultAction.class) != null || 
	            		setter.getAnnotation(Rest.class) != null || setter.getAnnotation(Value.class) != null || 
	            		(getter != null && (
                    		getter.getAnnotation(Action.class) != null || getter.getAnnotation(AjaxAction.class) != null || getter.getAnnotation(DefaultAction.class) != null ||
                    		getter.getAnnotation(Rest.class) != null || getter.getAnnotation(Value.class) != null
                		))) {
	                continue;
	            }

	            //當前 setter/getter 有被標注 @Input 者, 取用其 name 或 value 屬性值作為 request 參數名
	            Input an = null;
	            String tmp, param; //URL 參數
	            if((an = setter.getAnnotation(Input.class)) != null ||
	                    (getter != null && (an = getter.getAnnotation(Input.class)) != null)) {
	            	
	                param = ((tmp = an.name()).length() != 0) ? tmp : //以 name 屬性值為優先
	                	((tmp = an.value()).length() != 0) ? tmp : propName;
	            } else { //沒有標注 @Input 的屬性, 不拿來把 request parameters 結合至 bean properties, 但還是要 cache 起來 (下面的 scan field 還有找尋 @Input 的機會)
	                param = null;
	            }
	            
	            //property type
	            final Class<?> type = setter.getParameterTypes()[0];
	            
	            final PropDescriptor propDesc = new PropDescriptor(type, an, param, propName, setter, getter);
	            propDescriptors.put(propName, propDesc);
			}
	        
			//(2)scan bean fields (field 和 setter/getter 同時被標註 @Input 者, field 上的 param 設定 將蓋過原本由 setter/getter 得來的設定值)
	        //注意: Fusion Soft 的 annotation 工具不能對 field 的 annotation 起作用, 仍得自行用 Java Bean API 找尋
	        for(Field field : nonEnhancedClass.getDeclaredFields()) {
	        	final Input an = field.getAnnotation(Input.class);
	            if(an == null)
	                continue;
	            field.setAccessible(true);
	            
	            String param = an.name(); //URL 上所帶之參數(以 name 屬性值為優先)
	            if(param.length() == 0)
	            	param = an.value();
	            
	            final String fieldName = field.getName();
	            if(param.length() == 0) //表示畫面欄位名與 bean field 名相同
	                param = fieldName;
	            
	            //強制只能透過 setter method 來設值
	            final PropDescriptor propDesc = propDescriptors.get(fieldName);
	            if(propDesc == null) 
	                throw new RuntimeException("Backing bean '" + nonEnhancedClass.getName() + "': field '" + fieldName + "' with @Input annotation has no corresponding setter method.");
	            propDesc.parameterName = param;
	            propDesc.propAnnotation = an;
	            
	            //直接對 field 設值 (如有與 field 同名的 property, 只有 field 設值有作用)
	            /*
	            PropDescriptor propDesc = propDescriptors.get(fieldName);
	            if(propDesc != null) {
	                propDesc.parameterName = param; //@Input 標在 field 的優先性 > 標在 setter/getter
	                propDesc.field = field2;
	            } else { //有被 @Input 標注, 但無對應 setter/getter 的 field
	                propDesc = new PropDescriptor(an, param, fieldName, field, null, null);
	                propDescriptors.put(fieldName, propDesc); //如有與 field 同名的 property, 蓋掉 property 設值用的 PropDescriptor 物件
	            }
	            */
	        }
	        
	        //(3)scan action methods 之含被 @Input 標注的參數者, 或參數為 HttpServletRequest/HttpServletResponse/HttpSession 型態者
	        final List<ActionMethodDescriptor> methodDescriptors = new ArrayList<ActionMethodDescriptor>();
        	final Set<String> methodNames = new HashSet<String>(); //for 確保唯一 method name
        	final Map<String, String> restNameMethodPairs = new HashMap<String, String>(); //for 確保 @Rest 之 name 和 method 兩屬性值組合在同一 bean 中為唯一
        	boolean hasWebAction = false; //此 backing bean 是否含任何非 @Rest 的 method
        	boolean hasRESTAction = false; //此 backing bean 是否含 @Rest method -> 規定一般網頁 request 和 REST request 不共用同一 backing bean
        	boolean hasDefaultAction = false; //規定一 backing bean 最多只能有一 @DefaultAction 所標注的 action method
        	
	        for(Method method : nonEnhancedClass.getMethods()) { //每個 public methods (含屬於被繼承 class/interface 內的)
	        	final String methodName = method.getName();
	        	Annotation actionAnnotation = null;
	        	int invokeType = 0; //依照標注在 method 的 annotation 來分類
	        	boolean hasActionAnnotation = false;

	        	for(Annotation ann : method.getAnnotations()) { //檢查標注在 method 上之所有 annotation
	        		final Class<? extends Annotation> annType = ann.annotationType();
	        		
	        		if(annType.isAnnotationPresent(Action.class)) { //標注 @Action, @AjaxAction, @DefaultAction, @Rest... 等都算是被 @Action based annotation 標注的 method
	        			if(hasActionAnnotation)
	        				throw new IllegalArgumentException("action method " + nonEnhancedClass.getSimpleName() + "." + methodName + "() can be annotated with only one \""  + Action.class.getName() + "\" based annotation");
	        			hasActionAnnotation = true;
	        			actionAnnotation = ann;
	        			
	        			if(annType.equals(DefaultAction.class)) {
	        				if(hasDefaultAction)
	        					throw new IllegalArgumentException(nonEnhancedClass.getSimpleName() + " has more than one \"default action\" method (annotated with @" + DefaultAction.class.getSimpleName() + "). This is not allowed.");
	        				invokeType = INVOKE_AS_DEFAULT_ACTION;
	        				hasDefaultAction = true;
	        			} else if(annType.equals(Action.class)) {
	        				invokeType = INVOKE_AS_ACTION;
	        				hasWebAction = true;
	        			} else if(annType.equals(AjaxAction.class)) {
	        				invokeType = INVOKE_AS_AJAX_ACTION;
	        				hasWebAction = true;
	        			} else if(annType.equals(Rest.class)) {
	        				invokeType = INVOKE_AS_REST;
	        				hasRESTAction = true;
	        				
	        				//@Rest 之 name 和 method 兩屬性值組合在同一 bean 中須為唯一(也要考慮 method=Rest.METHOD_ANY 的情形)
	        				final Rest rest = (Rest)ann;
	        				String m = restNameMethodPairs.get(rest.name());
	        				if(m != null && (m.equals(rest.method()) || Rest.METHOD_ANY.equals(m) || Rest.METHOD_ANY.equals(rest.method())))
	        					throw new IllegalArgumentException("method annotation @Rest with attribute name=" + rest.name() + ", method=" + rest.method() + " ambiguous or duplicated");
	        				restNameMethodPairs.put(rest.name(), rest.method());
	        			} else {
	        				throw new IllegalArgumentException("unknown action method annotation type: " + annType);
	        			}
	        		}
	        	}
	        	
                if(actionAnnotation != null) { //檢查 action method 之 method parameters
                	//限制 action method 名稱不能重複
	                if(methodNames.contains(methodName))
	                    throw new IllegalArgumentException("there are more than one public action method named '" + methodName + "' in backing bean '" + nonEnhancedClass.getName() + "'");
	                methodNames.add(methodName);
	                
	                //取 action method parameter 資訊
	                final Class<?>[] paramClasses = method.getParameterTypes(); //正常情況下的 method parameters 名稱在 compile 後不存在了(所以一定要靠 annotation 註明參數名稱)
	                final ActionMethodParamDescriptor[] paramDescs = new ActionMethodParamDescriptor[paramClasses.length]; //與 paramClasses 成員對應
	                final Annotation[][] paramAnns = method.getParameterAnnotations(); //與 paramClasses 成員一一對應
	                boolean usePayload = false; //是否宜接取用 request payload
	                
	                for(int i = 0; i < paramClasses.length; i++) { //每個 method 參數
	                	if(HttpServletRequest.class.equals(paramClasses[i])) { //HttpServletRequest 參數不需標注 annotation
	                		paramDescs[i] = new ActionMethodParamDescriptor(null, TYPE_HTTP_SERVLET_REQUEST, false, false, null);
	                	} else if(HttpServletResponse.class.equals(paramClasses[i])) { //HttpServletResponse參數不需標注 annotation
	                		paramDescs[i] = new ActionMethodParamDescriptor(null, TYPE_HTTP_SERVLET_RESPONSE, false, false, null);
	                	} else if(HttpSession.class.equals(paramClasses[i])) { //HttpSession 參數不需標注 annotation
	                		paramDescs[i] = new ActionMethodParamDescriptor(null, TYPE_HTTP_SESSION, false, false, null);
	                	} else { //其他有標注 annotation 的 method parameter
		                	for(Annotation ann : paramAnns[i]) { //同一 parameter 的所有 annotation. (透過 @Input 取 parameter name, 用與 request parameter name 對應)
		                		final Class<? extends Annotation> annType = ann.annotationType();
		                		
		                		if(Input.class.equals(annType)) { //以 @Input 標注的 method 參數
		                			final Input input = (Input)ann;
		                			String paramName = (input.name().length() != 0) ? input.name() : //以 name 屬性值為優先
		                				(input.value().length() != 0) ? input.value() : null;
		                				
		                			final int typeCode = typeCode(paramClasses[i]);
		                			final boolean isParamArray = paramClasses[i].isArray();
		                			if(typeCode == TYPE_UNKNOWN) { //當作此參數是 command object (專門裝載 request parameter 的 JavaBean)
		                				if(paramName != null)
		                					throw new IllegalArgumentException("a parameter in " + nonEnhancedClass.getName() + "." + method.getName() + "() is annotated with @" + Input.class.getSimpleName() + "(\"" + paramName + "\") but which type is unsupported");
		                				if(isParamArray)
		                					throw new IllegalArgumentException("a array-type parameter in " + nonEnhancedClass.getName() + "." + method.getName() + "() annotated with @" + Input.class.getSimpleName() + " without specifying parameter name, obviously is neither a request parameter nor a command object");
		                				paramDescs[i] = new ActionMethodParamDescriptor(null, TYPE_VALUE_INPUT_COMMAND, false, false, paramClasses[i]); //TODO: 萬一也不是 command object, 如此仍無法拋 exception 示警
		                			} else {
		                				if(paramName == null) //有標注 @Input 的參數一定要指定參數名稱(無法依賴參數變數名本身)
		                					throw new IllegalArgumentException("a parameter in " + nonEnhancedClass.getName() + "." + method.getName() + "() is annotated with @" + Input.class.getSimpleName() + " but no parameter name specified via @" + Input.class.getSimpleName());
		                				paramDescs[i] = new ActionMethodParamDescriptor(paramName, typeCode, isParamArray, false, null);
		                			}
	                				break; //每個 method parameter 只取第一個對應到的 annotation.
	                				
		                		} else if(Payload.class.equals(annType)) { //以 @Payload 標注的 method 參數
		                			paramDescs[i] = new ActionMethodParamDescriptor(null, typeCode(paramClasses[i]), paramClasses[i].isArray(), true, null);
		                			usePayload = true;
		                			break; //每個 method parameter 只取第一個對應到的 annotation.
		                		}
		                	}
	                	}
	                }
	                methodDescriptors.add(new ActionMethodDescriptor(methodName, method, actionAnnotation, paramDescs, method.getReturnType(), invokeType, usePayload));
		        }
	        }
	        
	        if(hasWebAction && hasRESTAction) //限制: backing bean for 一般網頁呼叫 或 REST 呼叫, 只能二者擇一
	        	throw new IllegalArgumentException("Backing bean " + nonEnhancedClass.getName() + " can be only for either REST or non-REST requests, but not both");
	        
	        final PropDescriptor[] propDescs = propDescriptors.values().toArray(new PropDescriptor[propDescriptors.size()]);
	        final ActionMethodDescriptor[] methodDescs = methodDescriptors.toArray(new ActionMethodDescriptor[methodDescriptors.size()]);
	        ret = new ClassDescriptor(clazz, nonEnhancedClass, beanAnnotation, propDescs, methodDescs);
	        _beanDescCache.put(className, ret);
		}
		return ret;
	}
	
	private static void setValue(final Object bean, final PropDescriptor paramDescriptor, final String[] values, final StringBuilder debugMsg) {
		try {
			if(debugMsg != null)
				debugMsg.append("[").append(paramDescriptor.parameterName).append("=>").append(paramDescriptor.propertyName).append("=");
	        
	        //for field => 暫作廢, 改由 property setter 值, 因直接對 proxy 物件之  field 設值, 無法再以對應的 getter method 取值
			/*
			if(paramDescriptor.isField()) {
				Field field = paramDescriptor.field;
				Class<?> type = field.getType();
				if(!type.isArray()) {
					if(debugMsg != null) debugMsg.append("=").append((values == null) ? "" : values[0]);
					field.set(bean, convertType((values == null || "".equals(values[0])) ? null : values[0], paramDescriptor.type));
				} else { //array
					if(debugMsg != null) debugMsg.append("=").append((values == null) ? "" : StrUtil.join(values, ", "));
					if(values != null && values.length == 1 && EMPTY_ARRAY_PARAMETER_VALUE.equals(values[0]))
						field.set(bean, null);
					else
						field.set(bean, convertTypes(values, paramDescriptor.type, debugMsg));
				}
				if(debugMsg != null) debugMsg.append("]");
					return;
			}
			*/
	        
			//set property value via setter
			final Method setter = paramDescriptor.propSetter;
			final boolean hideSecretValues = debugMsg != null && RequestParameterHelper.matchSecretKeys(paramDescriptor.propertyName);
			
			if(!paramDescriptor.isArrayProp) {
				final Object value2 = convertType("".equals(values[0]) ? (String)null : 
						EMPTY_ARRAY_PARAMETER_VALUE.equals(values[0])? null : values[0], 
					paramDescriptor.propType);
				if(debugMsg != null) {
					debugMsg.append(hideSecretValues ? "***" : 
						(value2 == null) ? "" : value2);
				}
				setter.invoke(bean, value2);
			} else { //array
				if(values != null && values.length == 1 && EMPTY_ARRAY_PARAMETER_VALUE.equals(values[0])) { //畫面上的多值欄位(如 multi-select, checkbox 等)對於 "無值" 情形(瀏覽器預設不傳送該欄位變數), JavaScript 工具發送 request 仍須傳送一標示性字串以便把 server 端 backing bean 對應屬性值設為 null
					setter.invoke(bean, new Object[] { null });
				} else {
					setter.invoke(bean, new Object[] { convertTypes(values, paramDescriptor.propType, hideSecretValues, debugMsg) });
				}
			}
			if(debugMsg != null) debugMsg.append("]");
		} catch(Throwable t) {
			throw new IllegalArgumentException("error setting bean (" + bean.getClass().getName() + ") property '" + paramDescriptor.propertyName + 
					"' for request parameter='" + paramDescriptor.parameterName + "', value=[" + StrUtil.join(",", values) + "]", t);
		}
    }

	//for other tools
	public static Object convertType(final String value, final Class<?> type) {
		return convertType(value, typeCode(type));
	}
	
	//convert value to reference type value & JSONObject/JSONArray & Map<String, Object>/List<Object> (forJSON)
	//primary type: byte=>Byte, short=>Short, int=>Integer, long=>Long, float=>Float, double=>Double, boolean=>Boolean, char=>Character
	private static Object convertType(final String value, final int type) {
		try {
			switch(type) { //數字字串可能含 "," 字元, 轉型為數字型態前要移除之(但 byte 型態之值不會超過 1000, 不需考慮)
				case TYPE_STRING: return value;
				case TYPE_BYTE: return (value == null) ? new Byte((byte)0) : Byte.valueOf(value);
				case TYPE_BYTE_OBJ: return (value == null) ? (Byte)null : Byte.valueOf(value);
				case TYPE_SHORT: return (value == null) ? new Short((short)0) : Short.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_SHORT_OBJ: return (value == null) ? (Short)null : Short.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_INT: return (value == null) ? new Integer(0) : Integer.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_INT_OBJ: return (value == null) ? (Integer)null : Integer.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_LONG: return (value == null) ? new Long(0L) : Long.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_LONG_OBJ: return (value == null) ? (Long)null : Long.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_FLOAT: return (value == null) ? new Float(0F) : Float.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_FLOAT_OBJ: return (value == null) ? (Float)null : Float.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_DOUBLE: return (value == null) ? new Double(0D) : Double.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_DOUBLE_OBJ: return (value == null) ? (Double)null : Double.valueOf(StrUtil.replaceAll(value, ",", ""));
				case TYPE_BOOLEAN: return (value == null) ? Boolean.FALSE : Boolean.valueOf(value);
				case TYPE_BOOLEAN_OBJ: return (value == null) ? (Boolean)null : Boolean.valueOf(value);
				case TYPE_CHAR: return (value == null) ? new Character(Character.MIN_VALUE) : new Character(value.charAt(0));
				case TYPE_CHAR_OBJ: return (value == null) ? (Character)null : new Character(value.charAt(0));
				case TYPE_DATE: return convertDate(value);
				case TYPE_FILE: return (value == null || value.length() == 0) ? (File)null : new File(value);
				case TYPE_JSONOBJECT: return (value == null || value.length() == 0) ? (JSONObject)null : new JSONObject(value);
				case TYPE_JSONARRAY: return (value == null || value.length() == 0) ? (JSONArray)null : new JSONArray(value);
				default: 
					if(value == null)
						return null;
					throw new IllegalArgumentException(MsgUtil.message("Cannot convert request parameter value") + ": " + value);
			}
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(MsgUtil.message("Request parameter number format error") + ": " + value);
		}
	}

    //convert value[] to reference type value[]
    private static Object convertTypes(final String[] values, final int types, final boolean hideSecretValues, final StringBuilder debugMsg) {
        if(values == null)
            return null;
        if(values.length == 0)
            return new Object[0];
        final StringBuilder msg = (debugMsg == null) ? null : new StringBuilder();
        Object ret = null;
        
        switch(types) {
        	case TYPE_STRINGS: {
        		if(msg != null && values != null) {
	        		msg.append(StrUtil.concatenate(",", values, new StrUtil.ToStringHandler() {
						@Override public String toString(Object s) {
							return hideSecretValues ? "***" :
								(s == null) ? "" : s.toString();
						}
					}));
        		}
                ret = values;
                break;
    		}
        	case TYPE_BYTES: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final byte[] values2 = new byte[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Byte.valueOf(values[i]) : (byte)0;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_BYTE_OBJS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final Byte[] values2 = new Byte[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Byte.valueOf(values[i]) : (Byte)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_SHORTS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final short[] values2 = new short[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Short.valueOf(StrUtil.replaceAll(values[i], ",", "")) : (short)0;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_SHORT_OBJS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final Short[] values2 = new Short[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Short.valueOf(StrUtil.replaceAll(values[i], ",", "")) : (Short)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_INTS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final int[] values2 = new int[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null) ? Integer.valueOf(StrUtil.replaceAll(values[i], ",", "")) : 0;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_INT_OBJS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final Integer[] values2 = new Integer[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null) ? Integer.valueOf(StrUtil.replaceAll(values[i], ",", "")) : (Integer)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
    		}
        	case TYPE_LONGS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final long[] values2 = new long[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null) ? Long.valueOf(StrUtil.replaceAll(values[i], ",", "")) : 0L;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
    		}
        	case TYPE_LONG_OBJS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final Long[] values2 = new Long[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null) ? Long.valueOf(StrUtil.replaceAll(values[i], ",", "")) : (Long)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
    		}
        	case TYPE_FLOATS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final float[] values2 = new float[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Float.valueOf(StrUtil.replaceAll(values[i], ",", "")) : 0F;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_FLOAT_OBJS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final Float[] values2 = new Float[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Float.valueOf(values[i]) : (Float)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_DOUBLES: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final double[] values2 = new double[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Double.valueOf(StrUtil.replaceAll(values[i], ",", "")) : 0D;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_DOUBLE_OBJS: {
        		if(!checkEmptyValueForNumberArrayType(values))
        			break;
        		final Double[] values2 = new Double[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() > 0) ? Double.valueOf(StrUtil.replaceAll(values[i], ",", "")) : (Double)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_BOOLEANS: {
        		final boolean[] values2 = new boolean[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() != 0) ? Boolean.valueOf(values[i]) : false;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_BOOLEAN_OBJS: {
        		final Boolean[] values2 = new Boolean[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() != 0) ? Boolean.valueOf(values[i]) : (Boolean)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_CHARS: {
        		final char[] values2 = new char[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() != 0) ? Character.valueOf(values[i].charAt(0)) : Character.MIN_VALUE;
                    if(msg != null) msg.append(hideSecretValues ? "***" : values2[i]).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_CHAR_OBJS: {
        		final Character[] values2 = new Character[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = (values[i] != null && values[i].length() != 0) ? Character.valueOf(values[i].charAt(0)) : (Character)null;
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
        	}
        	case TYPE_DATES: {
        		final java.util.Date[] values2 = new java.util.Date[values.length];
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                    values2[i] = convertDate(values[i]);
                    if(msg != null) msg.append(hideSecretValues ? "***" : ((values2[i] != null) ? values2[i] : "")).append((i == iii) ? "" : ",");
                }
                ret = values2;
                break;
    		}
        	case TYPE_FILES: { //已上傳存檔的檔案
        		StringBuilder debugMsg2 = null;
            	File[] values2 = (File[])null;
                for(int i = 0, ii = values.length, iii = ii - 1; i < ii; i++) {
                	if(values[i] == null || values[i].length() == 0) { //不處理 File[] 中含 null 值的狀況
                		values2 = (File[])null;
                		debugMsg2 = null;
                		break;
                	}
                	if(values2 == null) //lazy initialization
                		values2 = new File[values.length];
            		values2[i] = new File(values[i]);
            		if(msg != null) { 
                        if(debugMsg2 == null)
                            debugMsg2 = new StringBuilder();
                        debugMsg2.append(values[i]).append((i == iii) ? "" : ",");
                    }
                }
                if(msg != null) { if(debugMsg2 != null) { msg.append(debugMsg2); }}
                ret = values2;
                break;
        	}
        	default: throw new IllegalArgumentException(MsgUtil.message("Cannot convert request parameter values") + "[" + StrUtil.join(",", values) + "]");
        }
        if(msg != null)
    		debugMsg.append("[").append((ret != null) ? msg : null).append("]");
        return ret;
    }
    
    private static boolean checkEmptyValueForNumberArrayType(final String[] values) {
    	if(values.length != 1 || values[0] == null)
    		return true;
    	return values[0].length() != 0; //client 端一個空字串來, 遇 array of number 型態的 bean property => 把此 property 設為 null
    }

    //將符合特定格式的日期字串 ==> java.util.Date 物件
    private static java.util.Date convertDate(final String dateString) {
        if(dateString == null)
            return null;
    
		java.util.Date ret = null;    
        int len = dateString.length();
        if(len == 7 || len == 8 || len == 9) { //民國年 "YYYMMDD" 或 "(Y)YY MM DD" (不限分隔字元)
            ret = DateUtil.twDateToDate(dateString);
        } else {
            //如果秒數之後帶毫秒值
            int i1 = dateString.indexOf('.');
            if(i1 != -1) {
                int i2 = dateString.lastIndexOf('.');
                if(i1 == i2 && (len - i2 - 1) < 3) //很可能帶毫秒(最多三位). 不等者, 即把 "." 視為時刻分隔字元
                    len = i2; //到小數點為止的長度
            }
            
            if(len == 16 || len == 17 || len == 18) //民國年 "YYYMMDD hh mm ss" 或 "(Y)YY MM DD hh mm ss" (不限分隔字元)
                ret = DateUtil.twDateToDate(dateString);
			else
           		ret = DateUtil.toDate(dateString);
        }
        
		if(ret == null)
			throw new IllegalArgumentException(MsgUtil.message(MSG_ERROR_DATE_FORMAT) + ": " + dateString);
		return ret;
    }
    
    //將受支援的 bean property type 以代碼表示
    private static int typeCode(final Class<?> type) {
    	if(String.class.equals(type))
			return TYPE_STRING;
		if(String[].class.equals(type))
			return TYPE_STRINGS;
		if(byte.class.equals(type))
			return TYPE_BYTE;
		if(byte[].class.equals(type))
			return TYPE_BYTES;
		if(Byte.class.equals(type))
			return TYPE_BYTE_OBJ;
		if(Byte[].class.equals(type))
			return TYPE_BYTE_OBJS;
		if(short.class.equals(type))
			return TYPE_SHORT;
		if(short[].class.equals(type))
			return TYPE_SHORTS;
		if(Short.class.equals(type))
			return TYPE_SHORT_OBJ;
		if(Short[].class.equals(type))
			return TYPE_SHORT_OBJS;
		if(int.class.equals(type))
			return TYPE_INT;
		if(int[].class.equals(type))
			return TYPE_INTS;
		if(Integer.class.equals(type))
			return TYPE_INT_OBJ;
		if(Integer[].class.equals(type))
			return TYPE_INT_OBJS;
		if(long.class.equals(type))
			return TYPE_LONG;
		if(long[].class.equals(type))
			return TYPE_LONGS;
		if(Long.class.equals(type))
			return TYPE_LONG_OBJ;
		if(Long[].class.equals(type))
			return TYPE_LONG_OBJS;
		if(float.class.equals(type))
			return TYPE_FLOAT;
		if(float[].class.equals(type))
			return TYPE_FLOATS;
		if(Float.class.equals(type))
			return TYPE_FLOAT_OBJ;
		if(Float[].class.equals(type))
			return TYPE_FLOAT_OBJS;
		if(double.class.equals(type))
			return TYPE_DOUBLE;
		if(double[].class.equals(type))
			return TYPE_DOUBLES;
		if(Double.class.equals(type))
			return TYPE_DOUBLE_OBJ;
		if(Double[].class.equals(type))
			return TYPE_DOUBLE_OBJS;
		if(boolean.class.equals(type))
			return TYPE_BOOLEAN;
		if(boolean[].class.equals(type))
			return TYPE_BOOLEANS;
		if(Boolean.class.equals(type))
			return TYPE_BOOLEAN_OBJ;
		if(Boolean[].class.equals(type))
			return TYPE_BOOLEAN_OBJS;
		if(char.class.equals(type))
			return TYPE_CHAR;
		if(char[].class.equals(type))
			return TYPE_CHARS;
		if(Character.class.equals(type))
			return TYPE_CHAR_OBJ;
		if(Character[].class.equals(type))
			return TYPE_CHAR_OBJS;
		if(java.util.Date.class.equals(type))
			return TYPE_DATE;
		if(java.util.Date[].class.equals(type))
			return TYPE_DATES;
		if(File.class.equals(type))
			return TYPE_FILE;
		if(File[].class.equals(type))
			return TYPE_FILES;
		if(JSONObject.class.equals(type))
			return TYPE_JSONOBJECT;
		if(JSONArray.class.equals(type))
			return TYPE_JSONARRAY;
		return TYPE_UNKNOWN; //不能在此抛 exception, 不然遇到被注以 @Resource 或 @Inject 或 @AutoWired 等的 property 就會過早地抛錯了
    }

    private static String invokeTypeAnnotationName(final int invokeType) {
    	switch(invokeType) {
    		case INVOKE_AS_ACTION: return Action.class.getSimpleName();
    		case INVOKE_AS_AJAX_ACTION: return AjaxAction.class.getSimpleName();
    		case INVOKE_AS_DEFAULT_ACTION: return DefaultAction.class.getSimpleName();
    		case INVOKE_AS_REST: return Rest.class.getSimpleName();
    		default: return "unknown";
    	}
    }
    
    public static class ClassDescriptor {
    	public final Class<?> clazz;
    	public final Class<?> nonEnhancedClass;
    	public final BackingBean annotation;
    	public final PropDescriptor[] propDescs;
    	public final ActionMethodDescriptor[] methodDescs;
    	
    	public ClassDescriptor(Class<?> clazz, Class<?> nonEnhancedClass, BackingBean annotation, 
    			PropDescriptor[] propDescs, ActionMethodDescriptor[] methodDescs) {
    		this.clazz = clazz;
    		this.nonEnhancedClass = nonEnhancedClass;
    		this.annotation = annotation;
    		this.propDescs = propDescs;
    		this.methodDescs = methodDescs;
    	}

		@Override
		public String toString() {
			return "ClassDescriptor [clazz=" + clazz + ", nonEnhancedClass=" + nonEnhancedClass + ", annotation=" + annotation +
					", propDescs=" + Arrays.toString(propDescs) + ", methodDescs=" + Arrays.toString(methodDescs) + "]";
		}
    }
    
	//backing bean 屬性資訊
	public static class PropDescriptor {
		public final int propType; //property type code (常數: TYPE_XXX)
		public Input propAnnotation;
		public String parameterName; //bean property 所配的 @Input 之 value 值, null 宜表示本 property 未與 ＠Input 對應
		public final String propertyName; //bean 之 field/property name
		//public final Field field;
		public final Method propSetter;
		public final Method propGetter;
		public final boolean isArrayProp; //此屬性型態是否為陣列
	    
		public PropDescriptor(Class<?> propType, Input propAnnotation, String parameterName, String propertyName, 
				Method propSetter, Method propGetter) {
			this.propType = typeCode(propType);
			this.propAnnotation = propAnnotation;
			this.parameterName = parameterName;
			this.propertyName = propertyName;
			//this.field = field;
			this.propSetter = propSetter;
			this.propGetter = propGetter;
			this.isArrayProp = (propType == null) ? false : propType.isArray();
		}

		@Override
		public String toString() {
			return "PropDescriptor [propType=" + propType + ", propAnnotation=" + propAnnotation + ", parameterName=" + parameterName +
					", propertyName=" + propertyName + ", propSetter=" + propSetter + ", propGetter=" + propGetter + ", isArrayProp=" + isArrayProp +
					"]";
		}
	}
	
	public static class ActionMethodDescriptor {
		final public String methodName;
		final public Method method;
		final public Annotation actionAnnotation;
		final public ActionMethodParamDescriptor[] params;
		final public Class<?> returnType;
		final public int invokeType; //INVOKE_AS_ACTION, INVOKE_AS_AJAX_ACTION, INVOKE_AS_DOWNLOAD_ACTION
		final public boolean usePayload; //此 method 是否直接取用 request payload 內容
		
		final public boolean isReturnJSON;
		
		public ActionMethodDescriptor(String methodName, Method method, Annotation actionAnnotation, ActionMethodParamDescriptor[] params, 
				Class<?> returnType, int invokeType, boolean usePayload) {
			this.methodName = methodName;
			this.method = method;
			this.actionAnnotation = actionAnnotation;
			this.params = params;
			this.returnType = returnType;
			this.invokeType = invokeType;
			this.usePayload = usePayload;
			
			this.isReturnJSON = returnType != null && (returnType.equals(JSONObject.class) || returnType.equals(JSONArray.class));
		}

		@Override
		public String toString() {
			return "ActionMethodDescriptor [methodName=" + methodName + ", method=" + method + ", actionAnnotation=" + actionAnnotation +
					", params=" + Arrays.toString(params) + ", returnType=" + returnType + ", invokeType=" + invokeType + ", usePayload=" +
					usePayload + "]";
		}
	}
	
	private static class ActionMethodParamDescriptor {
		final public String paramName;
		final public int typeCode;
		final public boolean isArray;
		final public boolean usePayload; //此參數是否用於傳入 request payload
		final public Class<?> commandObjectType; //當 typeCode=TYPE_VALUE_INPUT_COMMAND 時, 此 action method parameter 的型態 
		
		public ActionMethodParamDescriptor(String paramName, int typeCode, boolean isArray, boolean usePayload, Class<?> commandObjectType) {
			this.paramName = paramName;
			this.typeCode = typeCode;
			this.isArray = isArray;
			this.usePayload = usePayload;
			this.commandObjectType = commandObjectType;
		}

		@Override
		public String toString() {
			return "ActionMethodParamDescriptor [paramName=" + paramName + ", typeCode=" + typeCode + ", isArray=" + isArray + ", usePayload=" +
					usePayload + ", commandObjectType=" + commandObjectType + "]";
		}
	}
}
