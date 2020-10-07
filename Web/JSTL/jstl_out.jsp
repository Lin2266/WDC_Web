<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<meta charset="UTF-8">
<title>jstl_out</title>
</head>
<body>
	<p>c:out value="value" excapeXml="true | false" default="defaultValue"</p>
	或
	<p>c:out value="value" excapeXml="true | false"</p>
		 <p>default defaultValue</p>
		<p>/c:out</p>
	<p>1.value:JAVA運算式</p>
	<p>2.default:若前述value結果為null，就以本值取代</p>
	<p>3.excapeXml:預設true，若為true，則某些字元，如 < 、> 、& 、'、"將在輸出時被編碼，如 < 變成 &lt;</p>
	
	<c:out value="${param.email}" default="no email provided"/>
</body>
</html>