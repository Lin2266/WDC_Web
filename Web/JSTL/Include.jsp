<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>include</title>
</head>
<body>
	<h3>include:包含/合併指定的JSP頁面</h3>
	<h3>param:在進行前途include 和 forward作業時傳遞參數</h3>
	<h3>before...</h3>
	<hr>
	<p>在URL自己加user的值，http://localhost:8980/WDC_Web/JSTL/Include.jsp?user=jim</p>
	<jsp:include page="/WEB-INF/view/banner.jsp">
		<jsp:param value="${parm.user}" name="p1"/>
	</jsp:include>
	<hr>
	<h3>after...</h3>
	
	<jsp:include page="/WEB-INF/view/banner.jsp"></jsp:include>
	同於JAVA語法:
	<%	
 		//會跳到第1行
		String page1 = "/WEB-INF/view/banner.jsp";
		RequestDispatcher segment = request.getRequestDispatcher(page1);
		segment.include(request, response);
	%>
</body>
</html>