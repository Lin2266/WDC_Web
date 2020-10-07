<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>getProperty</title>
</head>
<body>
<jsp:useBean id="mybean" scope="request" class="course.model.CustomerBean"/>
<jsp:setProperty property="email" name="mybean" value="123@yahoo"/>
<jsp:getProperty property="email" name="mybean"/>
轉譯後為:out.print(mybean.getEmail());

</body>
</html>