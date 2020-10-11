<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<html>
<head>
<meta charset="UTF-8">
<title>JSTL x:parse Tags</title>
</head>
<body>
<h3>x:out select(Xpath正則表示式)需要使用jstl.jar - standard.jar - xml-apis1.4.jar ,需要注意版本</h3>
<h3>Books Info:</h3>
<c:import var="bookInfo" url="http://localhost:8980/WDC_Web/JSTL/books.xml"/>

<x:parse xml="${bookInfo}" var="output"/>
<b>The title of the first book is</b>: 
<x:out select="$output/books/book[1]/name" />
<br>
<b>The price of the second book</b>: 
<x:out select="$output/books/book[2]/price" />
	
	
</body>
</html>