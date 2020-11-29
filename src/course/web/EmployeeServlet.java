package course.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.jdbc.pool.DataSource;

import course.model.Employee;


@WebServlet(name="EmployeeServlet",urlPatterns="/EmployeeServlet")
public class EmployeeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataSource dataSource;
	
	public EmployeeServlet() {
			
	}
	public EmployeeServlet(DataSource dataSource) {
		this.dataSource = dataSource;		
	}

	public Employee addThenFindAll(Employee emp) {
		return emp;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		EmployeeServlet service = new EmployeeServlet();
		Employee emp = new Employee();
		emp.setId(Integer.valueOf(request.getParameter("id")));//接收數字參數
		emp.setSid(request.getParameter("sid"));//接收字串參數
		emp.setFirstName(request.getParameter("FirsName"));
		emp.setLastName(request.getParameter("LastName"));
		emp.setBirthDate(new Date());
		emp.setSalary(Float.valueOf(request.getParameter("Salary")));
		request.setAttribute("emps",service.addThenFindAll(emp));
		//request.setAttribute("emps",emp);
		RequestDispatcher rd = request.getRequestDispatcher("/showEmps.jsp");
		rd.forward(request, response);
	}




}
