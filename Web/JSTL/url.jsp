<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta charset="UTF-8">
<title>jstl_url</title>
</head>
<body>
<%-- 	<c:url value="value" var="varName" scope="page | request | session | application"/> --%>
	<p>1.value:需要套用URL-Rewriting的URL</p>
	<p>2.var:儲存前述URL-Rewriting後的URL</p>
	<p>3.scope:前途變數var的儲存範圍</p>
	
	<p>URL可以是「相對」或「絕對」路徑，將有不同結果</p>
	
	<p><a href='<c:url value="/hello.jsp"/>'>絕對路徑</a></p>	
	<p>檢視原始碼:href='<c:url value="/WDC_Web/hello.jsp"/>'</p>
	
	<font color="red">hello.jsp放跟url.jsp同層才會是相對路徑，放在web底下就要用絕對路徑/hello.jsp</font>
	<form action='<c:url value="hello.jsp"/>' method="GET">相對路徑
	<p>檢視原始碼:form action='<c:url value="hello.jsp"/>' method="GET"</p>
	<input type="submit">
	</form>
	
</body>
</html>