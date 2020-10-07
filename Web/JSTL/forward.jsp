<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:forward page="WEB-INF/view/banner.jsp">
	<jsp:param value="${parm.user}" name="p1"/>
</jsp:forward>	

<html>
<head>
<meta charset="UTF-8">
<title>forward</title>
</head>
<body>
	<h3>將請求轉發給指定的JSP頁面</h3>
		
	
</body>
</html>