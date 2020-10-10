package course.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ELCookieSCParam")
public class ELCookieSCParam extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addCookie(new Cookie("UserCookie","Tomcat User"));
		
		RequestDispatcher rd = getServletContext().getRequestDispatcher("/EL/elCookiesSCParam.jsp");
		rd.forward(request, response);
		
	}

}
