<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action='${pageContext.request.contextPath}/JSTL/forEach_select.jsp'>
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

<!-- 		c:if -->
<!-- 		屬性		描述				是否必要	預設 -->
<!-- 		test	條件				是		無 -->
<!-- 		var		用於存儲條件結果的變量	否		無 -->
<!-- 		scope	var屬性的作用域		否		page -->

	<font color="red">
	<p>表示式:${not empty paramValues.languages}為false</p>
	<p>表示式:${empty paramValues.languages}為true</p>
		
		<c:if test="${not empty paramValues.languages}" var="langSelected" scope="session">	
<!-- 		c:out在jstlIfFor2.jsp頁面印不出langSelected的值 -->
		<p>langSelected存的是:<c:out value="${langSelected}"></c:out></p>	
		
<!-- 		forEach -->
<!-- 		屬性			描述								是否必要	預設 -->
<!-- 		items		要被循環的信息						否	無 -->
<!-- 		begin		開始的元素（0=第一個元素，1=第二個元素)	否	0 -->
<!-- 		end			最後一個元素（0=第一個元素，1=第二個元素）	否	Last element -->
<!-- 		step		每一次迭代的步長						否	1 -->
<!-- 		var			代表當前條目的變量名稱					否	無 -->
<!-- 		varStatus	代表循環狀態的變量名稱					否	無 -->
		varStatus可以取索引(index)跟跑的次數(count)<br>
		forEach -> Selected languages :<br><br>
		<c:forEach var="lang" items="${paramValues.languages}" varStatus="status">
		${status.index}. ${status.count}. ${lang}
		</c:forEach>
		<br><br><br>
		</c:if>
		迴圈從0~10，2為間隔數<br>
		<c:if test="${not empty paramValues.languages}">
		<c:forEach begin="0" end="10" step="2" varStatus="status">
			${status.index},&nbsp;&nbsp;${status.count}	<br>
		</c:forEach>
		</c:if>
	</font>
	<br>
	<P>Go to <a href="test_forEach_select.jsp">test_forEach_select.jsp</a></P>
</body>
</html>