package course.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

/**
 * Servlet implementation class ValueGetController
 */
@WebServlet(urlPatterns= {"/ValueGetController.do"})
public class ValueGetController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		String action = request.getParameter("action");
		System.out.println("action" + action);
		if (action.equals("GetFirstFloorValue")) {
		String[] str = GetFirstFloorValue();
		JSONArray ja = JSONArray.fromObject(str);
		String json = ja.toString();
		out.print(json);
		System.out.println(json);
		out.flush();
		out.close();
		return;
		}
		out.flush();
		out.close();
		}
		private String[] GetFirstFloorValue() {
		String[] str = new String[4];
		str[0] = "test1";
		str[1] = "test2";
		str[2] = "test3";
		str[3] = "test4";
		return str;
	}

}
