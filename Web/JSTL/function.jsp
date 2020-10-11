<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<html>
<head>
<meta charset="UTF-8">
<title>函式標籤</title>
</head>
<body>
	<h3>多數用於字串處理</h3>
	<%
		pageContext.setAttribute("s1","this is JSTL functions tags test");
		pageContext.setAttribute("s2"," jim ");
	%>
<!-- 	test是判斷式 -->
	<c:if test="${fn:containsIgnoreCase(s1,'jstl')}"><!--判斷s1字串裡面有沒有jstl，不分大小寫-->
		'jstl' found!
	</c:if><br><br>
	
	<c:if test="${fn:contains(s1,'test')}"><!--判斷s1字串裡面有沒有TEST-->
		'test' found!
	</c:if><br><br>
	
	<c:if test="${fn:startsWith(s1,'this')}"><!--判斷s1字串裡面開題是不是this-->
		's1 starts with this'
	</c:if><br><br>
	
	length of s2(${s2}) is: ${fn:length(s2)}<br><br><!--包含空白-->
	
	<c:set var="s3" value="${fn:trim(s2)}"/><!--去掉空白-->
	length of s3(${s3}) is: ${fn:length(s3)}
</body>
</html>