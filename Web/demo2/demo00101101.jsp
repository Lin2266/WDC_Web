<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	final String v1 = request.getParameter("value1");
	final String v2 = (String)request.getAttribute("value1");
%>
<html>
<head>
<title>直接跳至他頁(續)</title>
</head>
<body>

取由前頁傳來的 value1 參數:
<ul>
	<li>在本頁以 request.getParameter("value1") 取值 =&gt; <br/>
		value1 = <%= v1 %>
		<br/>
	<li>在本頁 backing bean action method 內以 request.setAttribute("value1", ...) 傳值 =&gt; <br/>
		value1 = <%= v2 %>
</ul>

說明:
<ol>
	<li>以 HTTP POST 至指定 JSP 頁面時, 在處理 backing bean 階段已將 request parameter 消費殆盡,<br/>
		故新 JSP 頁面將無法直接以 request.getParameter() 的 API 取 request parameter.<br/>
		須在 backing bean 的 action method 中將 request parameter 值改透過 request.setAttribute() API 傳值
	<li>以 HTTP GET 至指定 JSP 頁面時, 雖不會發生 request parameter 提前消費完畢的狀況,<br/>
		但參數會顯示在畫面網址列上, 又可能有中文編碼的問題
</ol>

<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo00101101.jsp.txt" target="_blank">demo00101101.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo00101101.java.txt" target="_blank">Demo00101101.java</a>
</div>
</body>
</html>