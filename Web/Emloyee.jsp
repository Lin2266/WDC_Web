<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>員工資料</title>
</head>
<body>
<form method="POST" action="<%=request.getContextPath()%>/EmployeeServlet">
<p>
<label for="id">ID:</label>
<input type="text" id="id" name="id" value="<%=request.getParameter("id")==null?"":request.getParameter("id")%>">
</p>
<p>
<label for="FirsName">FirsName:</label>
<input type="text" id="FirsName" name="FirsName" value="<%=request.getParameter("FirsName")==null?"":request.getParameter("FirsName")%>">
</p>
<p>
<label for="LastName">LastName:</label>
<input type="text" id="LastName" name="LastName" value="<%=request.getParameter("LastName")==null?"":request.getParameter("LastName")%>">
</p>
<p>
<label for="BirthDate">BirthDate:</label>
<input type="date" id="BirthDate" name="BirthDate" value="<%=request.getParameter("BirthDate")==null?"":request.getParameter("BirthDate")%>">
</p>
<p>
<label for="Salary">Salary:</label>
<input type="text" id="Salary" name="Salary" value="<%=request.getParameter("Salary")==null?"":request.getParameter("Salary")%>">
</p>
<p>
<input type="submit" name="submit" value="送出">
</p>
</form>
</body>
</html>