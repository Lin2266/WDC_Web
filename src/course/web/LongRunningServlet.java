package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.RunUtility;


@WebServlet(urlPatterns= {"/LongRunningServlet"})
public class LongRunningServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("Request Thread 1:");
		System.out.println("Name:" + Thread.currentThread().getName());
		System.out.println("Time:" + new Date());
		System.out.println("-----------------------------------");
		//執行時耗的工作(5秒)
		RunUtility.run(5);
		response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

		out.println("Response Thread:</br>");
		out.println("Name:" + Thread.currentThread().getName() + "</br>");
		out.println("Time:" + new Date());
		
		//在網頁執行執行http://localhost:100/WDC_Web/LongRunningServlet
		//可以發現處理request和產生response的都是同一thread，且時間相隔5秒
		
		//導向結果至view
		RequestDispatcher rd = request.getRequestDispatcher("../../result1.jsp");
		rd.forward(request, response);
		
		
	}

//	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		// TODO Auto-generated method stub
//		doGet(request, response);
//	}

}
