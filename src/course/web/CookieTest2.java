package course.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/CookieTest2")
public class CookieTest2 extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cookie[] allCookies = request.getCookies();
        if(allCookies != null){
            for(int i = 0;i<allCookies.length;i++){
                Cookie c = allCookies[i];
                PrintWriter out = response.getWriter();
                out.append("Cookie userKey:"+ c.getName() +
                        "\tCookie userValue:" + c.getValue());
            }
        }else{
            response.getWriter().append("No cookie found");
    }

}
}
