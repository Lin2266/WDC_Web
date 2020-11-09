package course.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//web.xml有設定URL跟多個Filter，直接執行這個頁面
public class FilterOrderTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		PrintWriter out = response.getWriter();
		try {
			out.println("<html>");
			out.println("<head>");
			out.println("<title>FilterOrderTestServlet</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Test filter order.....</h1>");
			out.println("</body>");
			out.println("</html>");

		} finally {
			out.close();

		}
		
	}

}
