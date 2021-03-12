package course.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


import com.mysql.jdbc.Statement;

public class EmployeeDaoJdbcImpl implements EmployeeDao{
	private DataSource dataSource;
	//called by TestDataSourceFromScAction
	public EmployeeDaoJdbcImpl(DataSource dataSource2) {
		this.dataSource = dataSource2;
	}


	private DataSource getDataSource() {
		DataSource ds = null;
		try {
			Context ctx = new InitialContext();
			if(ctx == null) {
				throw new RuntimeException("JNDI not found");
			}
			ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/MyLocalDB");
			if(ds == null) {
				throw new RuntimeException("DataSource not found");
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ds;
				
	}

	@Override
	public void add(Employee e) {
		String sql = "insert into employee(id,firstname,lastname,birthdate,salary)"+
					"values(?,?,?,?,?)";
		try(Connection con = this.dataSource.getConnection();
			PreparedStatement pstmt = con.prepareStatement(sql);){
			pstmt.setLong(1,e.getId());
			pstmt.setString(2,e.getFirstName());
			pstmt.setString(3,e.getLastName());
			pstmt.setDate(4,new Date(e.getBirthDate().getTime()));
			pstmt.setFloat(5,e.getSalary());
			pstmt.executeUpdate();
		}catch(SQLException sqle) {
			sqle.printStackTrace();
		}
		
		
	}

	@Override
	public void update(Employee e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Employee> getAllEmployees() {
		List<Employee> emps = new ArrayList<Employee>();
		String sql = "select * from employee order by id desc";
		try(Connection con = this.dataSource.getConnection();
			Statement stmt = (Statement)con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);){
		
		while(rs.next()) {
			Employee e = new Employee();
			e.setId(rs.getLong("id"));
			e.setFirstName(rs.getString("firstname"));
			e.setLastName(rs.getString("lastname"));
			e.setSalary(rs.getFloat("salary"));
			e.setBirthDate(rs.getDate("birthdate"));
			emps.add(e);
		}
		}catch(SQLException e) {
			e.printStackTrace();
		}
		return emps;
	}
}
