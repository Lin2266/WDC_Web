<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	
	
   <c:url value="/test_param2.jsp" var="myURL">
   <c:param name="trackingId" value="1234"/>
   <c:param name="reportType" value="summary"/>
	</c:url>
	<c:import url="http://localhost:8980/${myURL}"/>
</body>
</html>