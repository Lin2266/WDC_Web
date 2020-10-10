<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<meta charset="UTF-8">
<title>重新導向redirect</title>
</head>
<body>
	<h3>c:redirect 用於頁面的重新導向，相當於response.sendRedirect()的作用。<br>
		重導向後瀏覽器會重送URL導致所有內容都消失。</h3>
	<c:redirect url="/test_redirect.jsp">
		<c:param name="id" value="Hello"/>
	</c:redirect>
	
</body>
</html>