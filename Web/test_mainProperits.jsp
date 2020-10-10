<%@page import="java.util.ResourceBundle"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<%	ResourceBundle resource= ResourceBundle.getBundle("main"); %>
<head>
<meta charset="UTF-8">
<title><%=resource.getString("name") %></title>
</head>

<body>
	取得main.properties的內容，version為key, 值:
	<%= resource.getString("version") %>
	
	<script type="text/javascript">
        	 //js 使用方式
            var version= 'resource.getString("version")';
        </script>
</body>
</html>