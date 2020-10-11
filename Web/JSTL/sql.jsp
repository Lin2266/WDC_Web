<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>
<html>
<head>
<meta charset="UTF-8">
<title>資料庫存取</title>
</head>
<body>
	<sql:setDataSource 
		var="dataSource"
		driver="com.mysql.jdbc.Driver"
		url="jdbc:mysql://localhost:3306/company?zeroDateTimeBehavior=convertToNull&characterEncoding=utf-8"
		user="root"
		password="123456"
	/>
	
	<c:set var="empId" value="3"/>
	
	Try to update employee(id=<c:out value="${empId}"/>) set Salary = 99...<br>
	<sql:update dataSource="${dataSource}" var="count">
		update Employee set Salary = 99 where id = ?
		<sql:param value="${empId}"/>
	</sql:update>
	
	Update finished!
	<sql:query dataSource="${dataSource}" var="result">
		select * from employee where id<=10
	</sql:query>
	
	<table border="1" width="100%">
	<tr>
		<th>ID</th>
		<th>First Name</th>
		<th>Last Name</th>
		<th>BirthDate</th>
		<th>Salary</th>
	</tr>
	<c:forEach var="row" items="${result.rows}">
	<tr>
		<th><c:out value="${row.ID}"/></th>
		<th><c:out value="${row.FirstName}"/></th>
		<th><c:out value="${row.LastName}"/></th>
		<th><c:out value="${row.BirthDate}"/></th>
		<th><c:out value="${row.Salary}"/></th>	
	</tr>		
	</c:forEach>
	</table>
</body>
</html>