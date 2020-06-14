<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>使用Multipart的表單上傳檔案</title>
</head>
<body>
	<!--multipart/form-data處理表單提交時伴隨文件上傳的場合-->
	<form action="${pageContext.request.contextPath}/upload" enctype="multipart/form-data" method="post">
	<p>
		Description:<input type="text" name="desc"/>
	</p>
	<p>
		File:<input type="file" name="data"/>
	</p>
	<input type="submit" value="Upload"/>
	</form>
</body>
</html>