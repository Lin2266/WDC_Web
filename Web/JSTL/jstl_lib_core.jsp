<%@page import="org.apache.jasper.tagplugins.jstl.core.Param"%>
<%@page import="org.apache.jasper.tagplugins.jstl.core.ForEach"%>
<%@page import="org.apache.catalina.Session"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<meta charset="UTF-8">
<title>jstl_core 函式庫標籤</title>
<%	pageContext.setAttribute("jiml","<strong>jiml</strong>",PageContext.SESSION_SCOPE);
	pageContext.setAttribute("arr",new Integer[5],PageContext.SESSION_SCOPE);
	pageContext.setAttribute("x",11,PageContext.SESSION_SCOPE);
%>
</head>
<body>
	<h3>1.c:out 顯示運算結果或數值。</h3>
	<font color="red">escapeXml(flase):<c:out value="${sessionScope.jiml}" escapeXml="false"/></font><br>
	<font color="red">escapeXml(true>:<c:out value="${sessionScope.jiml}" escapeXml="true"/></font>
	<p><font color="red">屬性default:<c:out value="${sessionScope.jim0}" default="no value"/></font></p><br>
	
	<h3>2.c:set 設定變數或屬性的值，可指定儲存範圍。</h3>
	<c:set value="this is jim" var="oneString"/>
	<p><font color="red">var:${oneString}</font></p><br>
		
	<h3>3.c:remove 刪除指定範圍內的變數。</p>
	<p><font color="red">Befor removed:${sessionScope.jiml}</font></h3>
	<c:remove var="jiml" scope="session"/>
	<font color="red"><p>After removed:${sessionScope.jiml}</p></font><br>
	
	<h3>4.c:catch 在JSP頁面中捕捉拋出的例外(java.lang.Throwable或其子類別)。</h3>
	<c:catch var="exception">
		<%
			int i =5;
			int j =0;
			int k =i/j;
		%>
	</c:catch>
	<font color="red">例外:<c:out value="${exception}"/></font><br><br>
	
	<h3>5.c:if 評估運算式結果，若為true將執行標籤本體,empty為空值，not empty不為空值，arry為KEY值,var存test的布林值。</h3>
	<c:if test="${not empty arr}" var="visits">
		<font color="red">\${arr}:${arr} - is not empty</font>
	</c:if>
		<p><font color="red">visits:${visits}</font></p><br>
		
	
	<font color="green"><h3>6.c:choose 沒有屬性，用於標籤 c:when和標籤c:otherwise 的父標籤。</h3>
	<h3>7.c:when 等價於「if」、「else if」語句，周含一個test屬性，該屬性表示需要判斷的條件。</h3>
	<h3>8.c:otherwise 沒有屬性，等價於「else」語句。</h3></font>
	<font color="red"><p>\${x} = ${x}</p></font>
	<c:choose>
		<c:when test="${x==11}"><font color="red">\${x == 11} is true</font></c:when>
		<c:when test="${x==12}"><font color="red">\${x == 12} is true</font></c:when>
		<c:when test="${x==13}"><font color="red">\${x == 13} is true</font></c:when>
		<c:otherwise><font color="red">x != 11,12,13</font></c:otherwise>
	</c:choose><br><br><br>
		
	
	<h3>9.c:forEach 用於頁面裡的重複結構。</h3>
	<form action='${pageContext.request.contextPath}/JSTL/jstl_lib_core.jsp'>
		<table>
		<tr><td>選擇程式語言</td>
			<td><select name='languages' size='6' multiple>
				<option value='Ada'>Ada</option>
				<option value='C'>C</option>
				<option value='C++'>C++</option>
				<option value='Cobol'>Cobol</option>
				<option value='Objective-C'>Objective-C</option>
				<option value='Java'>Java</option>
			</select></td>
		</tr>
		</table>
		<p><input type='submit' value='Finish Survey'></p>
	</form>
	<c:forEach var="lang" items="${paramValues.languages}" varStatus="status">
		<font color="red">"索引值:${status.index}, 第${status.count}個, 值:${lang}"</font>
		</c:forEach><br><br>
		
		
	<h3>10.c:forTokens 可以根據某個分隔符號將字串切割，和java.util.StringTokenizer類別的使用方法相似。</h3>
	<c:forTokens items="aa,bb,cc,dd" delims="," var="aValue">
		<p><font color="red">${aValue}</font></p>
	</c:forTokens>
	<c:forTokens items="aa-bb-cc-dd" delims="-" var="aValue">
		<font color="blue">${aValue}</font>
	</c:forTokens><br><br>
	
	
	<h3>11.c:import 包含另一個url的內容到本頁來。</h3>
	<c:import url="http://localhost:8980/WDC_Web/importPage.jsp"/><br><br>
	
	
	<h3>12.c:url 得到rewriting(改寫)後的url。</h3>
	<c:url value="/hello.jsp" var="urlPage"/>
	<a href="${urlPage}">${urlPage}</a><br><br>
	
	
	<h3>13.c:redirect 用於頁面的重新導向，相當於response.sendRedirect()的作用。<br>
		重導向後瀏覽器會重送URL導致所有內容都消失。</h3><br><br>
<%-- 	<c:redirect url="https://www.google.com.tw/"/> --%>

	<h3>c:param 新增參數到request中，可用於 c:import 跟 c:url 跟 c:redirect 等標籤。</h3>
	參考param.jsp
	
</body>
</html>