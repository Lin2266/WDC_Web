<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
<meta charset="UTF-8">
<title>多國語言</title>
</head>
<body>
	<h3>先在Java Resource建立properties，值要用當地語言</h3><br>
	<fmt:bundle basename="zh_TW">
		<fmt:message key="count.one" />
		<fmt:message key="count.two" />
		<fmt:message key="count.three" />	
	</fmt:bundle>
	
	<br><br>
	
	<fmt:setLocale value="en_US"/>
	<fmt:bundle basename="en_US">
		<fmt:message key="count.one" />
		<fmt:message key="count.two" />
		<fmt:message key="count.three" />	
	</fmt:bundle>
	
	<br><br>
	
	<fmt:setLocale value="ru_RU"/>
	<fmt:bundle basename="ru_RU">
		<fmt:message key="count.one" />
		<fmt:message key="count.two" />
		<fmt:message key="count.three" />	
	</fmt:bundle>
</body>
</html>