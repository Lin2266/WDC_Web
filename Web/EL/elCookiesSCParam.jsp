<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ELCookieSCParam</title>
</head>
<body>
	cookie:<br>
	1.${cookie}<br>
	2.${cookie.UserCookie.value}<br><br>
	
	ServletContext Param:<br>
	1.${initParam}<br>
	2.${initParam.ServletContextParam}<br>
</body>
</html>