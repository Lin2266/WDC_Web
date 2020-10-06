<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<jsp:useBean id="myBean" scope="request" class="course.model.CustomerBean"/>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<html>
<head>
<meta charset="UTF-8">
<title>${pageTitle}</title>
</head>
<%
    Map<String, String> users = new HashMap<String, String>();
    users.put("Mike", "18");
    users.put("Tom", "40");
    users.put("Jim", "30");
    application.setAttribute("users", users);
%>
<body>
	
<%-- 	<c:set var="varName" value="value" scope="page | request | session | application"/> --%>
<!-- 	或 -->
<%-- 	<c:set var="varName" scope="page | request | session | application"> --%>
<!-- 		value in body -->
<%-- 	</c:set> --%>
	
<!-- 	1.var:變數名稱。 -->
<!-- 	2.value:變數值，可以用標籤屬性或標籤本體(body)指定。 -->
<!-- 	3.scope:變數值儲存範圍，非必要。 -->
	
	<c:set var="pageTitle">My Title</c:set>
	${pageTitle}
	
	<p>set也可更新javaBean欄位值或map的物件實例名稱</p>
	1.target:javaBean或map的物件實例名稱
	2.property:javaBean的欄位名稱或map物件的名稱
	3.value:要設定的值

	<%--
    不設定scope時，則會以page、request、session、application的範圍尋找屬性名稱，如果在某個範圍找到屬性名稱，則在該範圍設定屬性。
    如果在所有範圍都沒有找到屬性名稱，則會在page範圍中新增屬性。
    --%>
    <c:set var="city" value="BeiJing" scope="session"></c:set>
    ${sessionScope.city}<br>

    <%-- value值也可以是一個物件 --%>
    <c:set var="user" value="${users}" scope="session"></c:set>
    ${sessionScope.user}<br>

    <%-- 刪除一個attribute --%>
    <c:remove var="users" scope="application"></c:remove>
    ${applicationScope.users}<br>

    <%--
     <c:set>也可以用來設定JavaBean的屬性或Map物件的鍵/值，要設定JavaBean或Map物件，必須使用target屬性進行設定。
     如果${sessionScope.user}運算出來的結果是個JavaBean，則呼叫setName()並將請求引數name的值傳入。
     如果${sessionScope.user}運算出來的結果是個Map，則以property屬性作為鍵，而value屬性作為值，來呼叫Map物件的put()方法。
     --%>
    <c:set target="${sessionScope.user}" property="Mike" value="31"></c:set>
    ${sessionScope.user}
	
	
</body>
</html>