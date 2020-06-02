<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<p>heade跟param是複數型態的「陣列」，只有一個也必須用[0]取出第一個成員，
	只能取單一物件，若原本就是集合物件，也只能取得第一個值，小心不要造成bug</p>
	heade:<br>
	1.${header["cookie"]}<br><br>
	
	headerVaules:<br>
	1.${headerValues["cookie"]}<br>
	2.${headerValues["cookie"][0]}<br>
	3.${headerValues["cookie"][1]}<br><br>
	
	param:<br>取參數的name
	1.${param}<br>
	2.${param.name}<br>
	3.${param.languages}<br><br>
	
	
	paramValues:<br>取多選的值
	1.${paramValues}<br>
	2.${paramValues.name}<br>
	3.${paramValues.name["0"]}<br>
	4.${paramValues.languages}<br>
	5.${paramValues.languages[0]}<br>
	6.${paramValues.languages["1"]}	<br><br>
</body>
</html>