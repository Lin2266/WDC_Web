package course.model;

import java.util.List;

public interface EmployeeDao {
	public void add(Employee e);
	public void update(Employee e);
	public void delete(int id);
	public List<Employee> getAllEmployees();

}
