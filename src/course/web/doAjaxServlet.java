package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


@WebServlet(urlPatterns= {"/doAjaxServlet.do"})
public class doAjaxServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		 
        //對Post中文參數進行解碼
 
        request.setCharacterEncoding("UTF-8");
 
        //取得Ajax傳入的參數
 
        String userName = request.getParameter("userName");
 
        String[] arrayUserInterest = request.getParameterValues("userInterest");
 
        //建構要回傳JSON物件
 
        HashMap userInfoMap = new HashMap();
 
        userInfoMap.put("userName", userName);
 
        
 
        ArrayList userInterestList = new ArrayList();
 
        userInterestList.addAll(Arrays.asList(arrayUserInterest));
 
        userInfoMap.put("userInterest", userInterestList);
 
        
 
        JSONObject responseJSONObject = new JSONObject(userInfoMap);
 
        
 
        PrintWriter out = response.getWriter();
 
        out.println(responseJSONObject);
	}
	
	public String getServletInfo() {
		// TODO Auto-generated method stub
		return "Short description"; 
	}


}
