<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action='JSTLifFor1.jsp'>
		<table>
		<tr><td>選擇程式語言</td>
			<td><select name='languages' size='6' multipl>
				<option value='Ada'>Ada</option>
				<option value='C'>C</option>
				<option value='C++'>C++</option>
				<option value='Cobol'>Cobol</option>
				<option value='Objective-C'>Objective-C</option>
				<option value='Java'>Java</option>
			</select>
			</td>
		</tr>
		</table>
		<p><input type='submit' value='Finish Survey'></p>
	</form>
	<font color="red">
		<c:if test="${not empty paramValues.languages}" var="langSelected" scope="session"/>		
		Selected languages were:<br><br>
		<c:forEach var="lang" items="${paramValues.languages}" varStatus="status"/>
		
	</font>
</body>
</html>