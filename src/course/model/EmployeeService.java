package course.model;

import java.util.List;

import javax.activation.DataSource;

public class EmployeeService {
	private EmployeeDao dao;
	
	//called by TestDataSourceFromScAtion
	public EmployeeService(DataSource dataSource) {
		this.dao = new EmployeeDaoJdbcImpl(dataSource);
	}
	
	public List<Employee> addThenFindAll(Employee e){
		dao.add(e);
		List<Employee> emps = dao.getAllEmployess();
		return emps;
	}

}
