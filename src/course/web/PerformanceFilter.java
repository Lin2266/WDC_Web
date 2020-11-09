package course.web;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;

//用來攔截所有Servlet的request和response以計算Servlet執行時間(效能)

//此設定也可在web.xml做設定
//@WebFilter(filterName = "/PerformanceFilter",//設定名稱
//			urlPatterns = "/*",				 //設定需要過濾的URL資源型式，複數時使用陣列
//			dispatcherTypes = {				 //設定觸發時機，複數時使用陣列
//			DispatcherType.FORWARD,		 
//			DispatcherType.ERROR,
//			DispatcherType.REQUEST,
//			DispatcherType.INCLUDE},
//			initParams = {					 //設定初始參數，複數時使用陣列，參數的名稱和值如以下:
//			@WebInitParam(name = "Log Entry Prefix", value="Performance: ")
//			})
public class PerformanceFilter implements Filter {
	private FilterConfig config;
	
	//當容器建立Filter後會呼叫1次，該方法由容器傳入FilterConfig物件，可取得Filter初始參數
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}
	
	//每次request被攔截時都會執行一次，等同Servlet的service()
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		//before next filter or Serlvet
		long begin = System.currentTimeMillis();
		//to next Filter or Servlet，繼續下一個Filter or Servlet
		chain.doFilter(request, response);
		//after next Filter or Servlet
		long end = System.currentTimeMillis();
		//prepare log message
		StringBuffer logMessage = new StringBuffer();
		if(request instanceof HttpServletRequest) {
			//取得request的URL作為日記記錄的一部分
			logMessage = ((HttpServletRequest) request).getRequestURL();
		}
		logMessage.append(": ");
		logMessage.append(end-begin);
		logMessage.append(" ms");
		//對應@WebInitParam(name = "Log Entry Prefix")，會取得value="Performance: ", 當log的字首
		String logPrefix = config.getInitParameter("Log Entry Prefix");
		if(logPrefix !=null) {
			logMessage.insert(0, logPrefix);
		}
		System.out.println(logMessage.toString());
	}

	public void destroy() {
		config = null;
	}



}
