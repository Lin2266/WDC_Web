<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<form action=<%=request.getContextPath()%>"/EmployeeServlet">
<p>
ID:<input type="text" id="id" name="id">
</p>
<p>
FirsName:<input type="text" id="FirsName" name="FirsName">
</p>
<p>
LastName:<input type="text" id="LastName" name="LastName">
</p>
<p>
BirthDate:<input type="date" id="BirthDate" name="BirthDate">
</p>
<p>
Salary:<input type="text" id="Salary" name="Salary">
</p>
</form>
</body>
</html>