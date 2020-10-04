<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<jsp:useBean id="myBean" scope="request" class="course.model.CustomerBean"/>
<head>
<meta charset="UTF-8">
<title>JSP標準標籤</title>
</head>
<body>
	<%  myBean.setName(request.getParameter("name"));
		myBean.setEmail(request.getParameter("email"));
		myBean.setPhone(request.getParameter("phone"));
	%>
	<p>jsp:useBean id="" scope=""(page | request | session | application)
				class=""</p>
	<p>1.標籤屬性id:bean元件名稱</p>
	<p>2.標籤屬性scope:bean元件的儲存範圍</p>
	<p>3.標籤屬性class:bean元件的類別元整名稱(含package)</p>
	<form action="TestUseBen1.jsp">
	<p>name:<input type="text" id="name" name="name"></p>
	<p>email:<input type="text" id="email" name="email"></p>
	<p>phone:<input type="text" id="phone" name="phone"></p>
	<input type="submit">
	</form>
	
	 
	<p>myBean:${myBean}</p>
	myBean.name:${myBean.name}
	
	<jsp:useBean id="myBean1" scope="request" class="course.model.CustomerBean">
		<%  myBean1.setName(request.getParameter("name"));
			myBean1.setEmail(request.getParameter("email"));
			myBean1.setPhone(request.getParameter("phone"));
		%>		
	</jsp:useBean>
	<p>myBean1:${myBean1}</p>
	myBean1.name:${myBean1.name}
	
</body>
</html>