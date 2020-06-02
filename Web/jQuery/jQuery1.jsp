<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-ui.js"></script>
<title>Insert title here</title>
<script>
	jQuery.onConflict();	
	(function($) {
		
	})(jQuery);
	
	var $alias = jQuery.noConflice();
	
		$('#el').css('background-color', 'green');
		 
</script>
</head>
<body>
	<p id="el">abcdef</p>
</body>
</html>