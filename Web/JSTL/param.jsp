<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<meta charset="UTF-8">
<title>jstl & el _param</title>
</head>
<body>
	<h3>新增參數到request中，可用於 c:import 跟 c:url 跟 c:redirect 等標籤。<br>
	param:在進行前途jsp:include 和 forward作業時傳遞參數</h3>
<!-- 	因為value沒值，要在URL自己加user的值，http://localhost:8980/WDC_Web/JSTL/Include.jsp?user=jim
		有值就不用 -->
	
	<h2>1.jsp:include:</h2>	
	<jsp:include page="/WEB-INF/view/banner.jsp">
		<jsp:param value="john" name="user"/>
	</jsp:include><br>
	
	
	<h2>2.jsp:forward:</h2>	因轉頁需單獨測試
<!-- forward完會轉到新頁面，要單獨測試	 -->
<!-- 	http://localhost:8980/WDC_Web/JSTL/forward.jsp?user=jim -->
<%-- 	<jsp:forward page="/WEB-INF/view/banner.jsp"> --%>
<%-- 	<jsp:param value="${parm.user}" name="p1"/> --%>
<%-- 	</jsp:forward>	 --%>
	
	
	<br><br>
	<h2>3.c:c:url:</h2>param value不能用EL變數
	<c:url value="/test_param.jsp" var="myURL">
<%-- 		<c:param name="urlName" value="${parm.user }"/> --%>		
   		<c:param name="trackingId" value="1234"/>
   		<c:param name="reportType" value="summary"/>
	</c:url>
	<br><br>
	
	<h2>4.c:import:</h2>import路徑要以HTTP開頭
	<c:import url="http://localhost:8980/${myURL}">
		<c:param name="urlName" value="/test_param.jsp"/>
	</c:import>
	<br><br>
	
	<h2>5.c:redirect:</h2>因轉頁需單獨測試
	<h3>c:redirect 用於頁面的重新導向，相當於response.sendRedirect()的作用。<br>
		重導向後瀏覽器會重送URL導致所有內容都消失。</h3>
<%-- 	<c:redirect url="/test_redirect.jsp"> --%>
<%-- 		<c:param name="id" value="Hello"/> --%>
<%-- 	</c:redirect> --%>
</body>
</html>