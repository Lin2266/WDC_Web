<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>Beer</title>
</head>
<body>
	<h1>Beer Selection Page</h1>
	<form method="POST" action="BeerSelect.do">
	Color:
	<select name="color">
		<option value="light"> light </option>
		<option value="amber"> amber </option>
		<option value="brown"> brown </option>
		<option value="dark"> dark </option>
	</select>
	<br><br>
	<center>
	<input type="submit">
	</center>
	</form>
</body>
</html>