<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.11.3.min.js"></script> 
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<title>Insert title here</title>
</head>
<body>
Encoded
<p>
	<c:out value="${js}"/>
</p>
Not Encoded:
<p>
	${js}
</p>
</body>
</html>