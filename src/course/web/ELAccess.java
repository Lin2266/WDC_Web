package course.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.Address;
import course.model.Employee;


@WebServlet("/ELAccess")
public class ELAccess extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //test variable
        request.setAttribute("myNum",0);
        request.setAttribute("myStr","empl");
        
        //JAVA bean
        Employee empl = new Employee();
        empl.setId(1);
        empl.setName("Jim");
        Address addr = new Address();
        addr.setLocation("Taipei");
        empl.setAddress(addr);
        request.setAttribute("empl",empl);
        
        Employee empl2 = new Employee();
        empl2.setName("Bill");
        
        //MAP
        Map<String,Employee> myMap = new HashMap<>();
        myMap.put("empl",empl);
        myMap.put("empl2", empl2);
        request.setAttribute("myMap",myMap);
        
        //LIst
        List<Employee> myList = new ArrayList<>();
        myList.add(empl);
        myList.add(empl2);
        request.setAttribute("myList",myList);
        
        //Array
        Employee[] myArray = new Employee[2];
        myArray[0] = empl;
        myArray[1] = empl2;
        request.setAttribute("myArray",myArray);
        
        RequestDispatcher dr = request.getRequestDispatcher("/elAccess.jsp");
        dr.forward(request, response);
    }  

}
