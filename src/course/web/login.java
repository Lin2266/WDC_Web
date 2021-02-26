package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

/**
 * Servlet implementation class login
 */
@WebServlet("/login.do")
public class login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String account = request.getParameter("id");
		String password = request.getParameter("pwd");
		
		HashMap userData = new HashMap();
		userData.put("account", account);
		userData.put("password", password);
		
		JSONObject responJsonObject = new JSONObject(userData);
		PrintWriter out = response.getWriter();
		out.println(responJsonObject);
	}

}
