package course.web;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;

import course.model.Employee;



@WebServlet("/TestDataSourceFromScAtion")
public class TestDataSourceFromScAtion extends HttpServlet {


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get connection pool
		Object o = request.getServletContext().getAttribute("WeStartListener.DB");
		DataSource ds = (DataSource)o;
		//pass connection pool to DAO, through service
		EmployeeServlet service = new EmployeeServlet(ds);
		//call service
		//List<Employee> emps = service.addThenFindAll(createEmp());
		//dispatch request
		//request.setAttribute("emps",emps);
		request.setAttribute("subject","from Service Context");
		RequestDispatcher rd = request.getRequestDispatcher("showEmps.java");
		rd.forward(request, response);
		
	}
	
	private Employee createEmp() {
		Employee emp = new Employee();
		emp.setId(new Date().getTime());
		emp.setFirstName("test");
		emp.setLastName("test");
		emp.setBirthDate(new Date());
		emp.setSalary(10000.1f);
		return emp;
	}

	
}
