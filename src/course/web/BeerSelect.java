package course.web;

import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.BeerExpert;

public class BeerSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;

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
