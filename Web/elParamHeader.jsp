<%@page language="java" contentType="text/html;charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<form action='elParamHeaderResult.jsp'>
		<table>
			<tr>
				<td>輸入姓名:</td>
				<td><input type='text' name='name'></td>
			</tr>
			<tr>
				<td>選擇程式語言</td>
				<td><select name='languages' size='4' multiple>
						<option value='Ada'>Ada</option>
						<option value='C'>C</option>
						<option value='C++'>C++</option>
						<option value='Cobol'>Cobol</option>
						<option value='Objective-C'>Objective-C</option>
						<option value='Java'>Java</option>
				</select></td>
			</tr>
		</table>
		<p>
			<input type='submit' value='Finish Servey'>
		</p>
	</form>
</body>
</html>