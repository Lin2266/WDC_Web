<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<title>直接跳至他頁</title>
<script>
function postToNextPage() {
	document.form1.method = "post";
	document.form1.action = "${pageContext.request.contextPath}/demo00101101.jsp";
	document.form1.submit();
}
function getToNextPage() {
	document.form1.method = "get";
	document.form1.action = "${pageContext.request.contextPath}/demo00101101.jsp";
	document.form1.submit();
}
</script>
</head>
<body>

<form name="form1" id="form1">
	<input type="hidden" name="_action" value="init"/><!-- 呼叫 backing bean -->
	值一: <input type="text" name="value1" value="測試"/>
	<br/><br/>
	
	<button onclick="postToNextPage(); return false;">以 HTTP POST 直接至下一頁</button>
	&nbsp; &nbsp;
	<button onclick="getToNextPage(); return false;">以 HTTP GET 直接至下一頁</button>
</form>

<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo001011.jsp.txt" target="_blank">demo001011.jsp</a>
</div>
</body>
</html>