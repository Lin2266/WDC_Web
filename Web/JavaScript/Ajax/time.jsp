<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<times >
<%
      Date dNow = new Date( );
      SimpleDateFormat fd = new SimpleDateFormat ("yyyy-MM-dd");
      SimpleDateFormat ft = new SimpleDateFormat ("HH:mm:ss");
      String date = fd.format(dNow);
      String time = ft.format(dNow);
      out.println(date+"   "+time);
%> 
</times>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.11.3.min.js"></script> 
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<title>時間重整</title>
</head>
<body>
<p>沒ajax的時間不會重整<span class="times"></span></p>
</body>
</html>