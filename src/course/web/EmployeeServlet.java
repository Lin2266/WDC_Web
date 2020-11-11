package course.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import course.model.Employee;


@WebServlet("/EmployeeServlet")
public class EmployeeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EmployeeServlet service = new EmployeeServlet();
		Employee emp = new Employee();
		emp.setId(Integer.valueOf(request.getParameter("ID")));
		emp.setFirstName(request.getParameter("FirstName"));
		emp.setLastName(request.getParameter("LastName"));
		emp.setBirthDate(new Date());
		emp.setSalary(Float.valueOf(request.getParameter("Salary")));
		//request.setAttribute("emps",service.adddThenFindAll(emp));
		
		RequestDispatcher rd = request.getRequestDispatcher("/showEmps.jsp");
		rd.forward(request, response);
	}




}
