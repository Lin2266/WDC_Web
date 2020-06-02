/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package course.web;

import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import course.model.Address;
import course.model.Employee;


@WebServlet(name = "ELScope", urlPatterns = {"/ELScope"})
public class ELScope extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //page、request、session、application (Map型態)
        Employee emp1 = new Employee();
        emp1.setName("Bill");
        request.setAttribute("emp1", emp1);
        
        //test Object
        Employee emp2 = new Employee();
        emp2.setId(1);
        emp2.setName("Jim");
        Address addr = new Address();
        addr.setLocation("Taipei");
        emp2.setAddress(addr);
        
        //test session
        HttpSession session = request.getSession();
        session.setAttribute("emp2",emp2);
        RequestDispatcher rd = getServletContext().getRequestDispatcher("/elScope.jsp");
        rd.forward(request, response);
       
    }

}
