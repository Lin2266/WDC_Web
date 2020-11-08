package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/LifeCycleTestServlet")
public class LifeCycleTestServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/*
	 * 類別方法標註這些annotation的用處和生命週期方法init()及destroy()相似，被標註的方法本身不需要是public，
	 * 不限定名稱，但必須是:
	 * 1.沒有參數
	 * 2.宣告為void
	 * 3.沒有拋出checked Exception
	 * 4.非final
	 * 
	 * 容器呼叫標註@PostConstruct的方法時若發生錯誤，該物件將無法使用，會在以下時機點被容器呼叫，
	 * 1.依賴注入後
	 * 2.呼叫init()方法前
	 */
	//容器調用順序為:
	//1.
	@PostConstruct
	void myInit() {
		System.out.println("myInit() is called by container");
	}
	String myParam;
	
	//2.
	@Override
	public void init() throws ServletException {
		this.myParam = super.getServletConfig().getInitParameter("myParam");
		System.out.println("init() hooks!");
	}
	
	//3.用eclipse的server頁籤的stop按鍵正常停止tomcat，才會被調用，console的Terminate按鍵將直接停止JVM, 
	//等同暴力停止tomcat，此方法將不會被觸發。
	@Override
	public void destroy() {
		System.out.println("destroy() is called by container");
	}
	
	//4.用eclipse的server頁籤的stop按鍵正常停止tomcat，才會被調用，console的Terminate按鍵將直接停止JVM, 
	//等同暴力停止tomcat，此方法將不會被觸發。
	@PreDestroy
	public void myDestroy() {
		System.out.println("myDestroy() is called by container");
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html);charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			out.println("<h1>myParam =" + myParam + "</h1>");
		}finally {
			out.close();
		}
	}


}
