package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.BeerExpert;
import jdk.nashorn.internal.ir.RuntimeNode.Request;

//@WebServlet(name = "BeerSelect", urlPatterns = {"/BeerSelect.do"})
public class BeerSelect extends HttpServlet {

    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		String color = request.getParameter("color");
		BeerExpert be = new BeerExpert();
		List result = be.getBrands("color");
		
		request.setAttribute("styles",result);
		RequestDispatcher rd = request.getRequestDispatcher("result.jsp");
		rd.forward(request, response);
		
//		PrintWriter out = response.getWriter();
//		out.println("Beer Selection Advice<br>");		
//		
//		
//		Iterator it = result.iterator();
//		while (it.hasNext()) {
//			out.println("<br>try:" + it.next());
//			
//		}
	}

}
