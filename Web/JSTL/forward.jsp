<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<html>
<head>
<meta charset="UTF-8">
<title>forward</title>
</head>
<body>
	<h3>將請求轉發給指定的JSP頁面</h3>
	<p>在URL自己加user的值，http://localhost:8980/WDC_Web/JSTL/forward.jsp?user=jim</p>
	<jsp:forward page="/WEB-INF/view/banner.jsp">
	<jsp:param value="${parm.user}" name="p1"/>
	</jsp:forward>		
	
</body>
</html>