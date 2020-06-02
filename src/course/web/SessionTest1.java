
package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.html.HTML;

@WebServlet(name = "SessionTest1", urlPatterns = {"/SessionTest1"})
public class SessionTest1 extends HttpServlet {
   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String prarm = request.getParameter("prarm");
        HttpSession session = request.getSession();
        session.setAttribute("msg1", "this is msg1 saved in SerssionTest1:"+ prarm + "</br>");
                
                
       
    }

}
