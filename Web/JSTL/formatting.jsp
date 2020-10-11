<%@page import="java.util.Date"%>
<%@page import="org.apache.naming.java.javaURLContextFactory"%>
<%@page import="javax.xml.crypto.Data"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Formatting</title>
</head>
<body>
	<% int i = 99; %>

	<p>格式化涉及「多國語言」與「時區」的JavaSE主題，「localization」、「Date Time API」</p>
	
	<h3>fmt:setLocale, fmt:bundle 設定多國語言</h3>
	<fmt:setLocale value="ru_RU"/>
	<fmt:bundle basename="ru_RU"><%--取得main.properties--%>
 		<fmt:message key="menu6"> <%--取得main.properties的key然後輸出值--%>
			<fmt:param value="99"/>
		</fmt:message>
	</fmt:bundle><br><br>
	
	<h3>fmt:setLocale, fmt:bundle 設定多國語言</h3>
	<fmt:setLocale value="zh_TW"/>
	<fmt:setBundle basename="zh_TW" var="money"/><%--取得main.properties--%>
 		<fmt:message key="menu6" bundle="${money}"> <%--取得main.properties的key然後輸出值--%>
			<fmt:param value="99"/><%--對應main.properties值裡面的{0}--%>
		</fmt:message><br><br>
	
	<h3>fmt:parseDate 解析日期格式</h3>
	<c:set var="now" value="20-10-2020"/>
	<fmt:parseDate value="${now}" var="parsedEmpDate" pattern="dd-mm-yyyy"/>
	Parsed Date : <c:out value="${parsedEmpDate}"/><br><br>
	
	<h3>fmt:parseNumber 設定數字格式</h3>
	<c:set var="balance" value="1250003.350"/>
<!-- 	type:NUMBER數字，CURRENCY貨幣，PERCENT百分比 -->
	<fmt:parseNumber var="i1" type="number" value="${balance}"/>
	Parsed Number (1) : <c:out value="${i1}"/> 小數點第2位<br>
	
	<fmt:parseNumber var="i2" integerOnly="true" type="number" value="${balance}"/>
	Parsed Number (2) : <c:out value="${i2}"/> 整數(integerOnly是否只解析整數)<br>
	Parsed Number (3) : <fmt:parseNumber value="15%"/><br><br>
	
	<h3>fmt:formatDate 設定日期格式</h3>
	<c:set var="now" value="<%=new java.util.Date()%>"/>
	Formatted Date (1) :<%--上午 12:24:36 --%>
 	<fmt:formatDate type="time" value="${now}"/><br>
	Formatted Date (2) :<%--2020/10/11 --%>
	<fmt:formatDate type="date" value="${now}"/><br>
	Formatted Date (3) :<%--2020/10/11 上午 12:24:36--%>
	<fmt:formatDate type="both" value="${now}"/><br>
	Formatted Date (4) :<%--2020/10/11 上午 12:24--%>
	<fmt:formatDate type="both" dateStyle="short" timeStyle="short" value="${now}"/><br>
	Formatted Date (5) :<%--2020/10/11 上午 12:24:36--%>
	<fmt:formatDate type="both" dateStyle="medium" timeStyle="medium" value="${now}"/><br>
	Formatted Date (6) :<%--2020年10月11日 上午12時24分36秒--%>
	<fmt:formatDate type="both" dateStyle="long" timeStyle="long" value="${now}"/><br>
	Formatted Date (7) :<%--2020-27-11--%>
	<fmt:formatDate pattern="yyyy-mm-dd" value="${now}"/><br><br>
	
	<h3>fmt:formatNumber 設定數字格式</h3>
	<c:set var="balance" value="120010.2309"/>
	Formatted Number (1) :<%--NT$120,010.23--%>
	<fmt:formatNumber value="${balance}" type="currency"/><br>
	Formatted Number (2) :<%--010.231 , 整數最大位數，只到整數第3位，小數點4捨五入--%>
	<fmt:formatNumber type="number" maxIntegerDigits="3" value="${balance}"/><br>
	Formatted Number (3) :<%--120,010.231 , 小數點後最大位數，小數只到第3位--%>
	<fmt:formatNumber type="number" maxFractionDigits="3" value="${balance}"/><br>
	Formatted Number (4) :<%--120010.231 , 是否對數字分組--%>
	<fmt:formatNumber type="number" groupingUsed="false" value="${balance}"/><br>
	Formatted Number (5) :<%--023%--%>
	<fmt:formatNumber type="percent" maxIntegerDigits="3" value="${balance}"/><br>
	Formatted Number (6) :<%--12,001,023.0900000000% , 小數點後最小位數--%>
	<fmt:formatNumber type="percent" minFractionDigits="10" value="${balance}"/><br>
	Formatted Number (7) :<%--12,001,023% , 小數點後最小位數--%>
	<fmt:formatNumber type="percent" minIntegerDigits="3" value="${balance}" /><br>
	Formatted Number (8) :<%--120.01E3--%>
	<fmt:formatNumber type="number" pattern="###.###E0" value="${balance}" /><br>
	
	Currency in USA:<%--$120,010.23--%>
	<fmt:setLocale value="en_US"/>
	<fmt:formatNumber value="${balance}" type="currency"/>
		
	
</body>
</html>