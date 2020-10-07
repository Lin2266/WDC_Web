<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<jsp:useBean id="myBean" scope="request" class="course.model.CustomerBean"/>
<html>
<head>
<meta charset="UTF-8">
<title>setProperty</title>
</head>
<body>
		<p>1.標籤屬性name:bean元件的名稱</p>
	<p>2.標籤屬性property:bean元件的的欄位名稱</p>
	<ul>
	<li>property="*"</li>
	<li>property="propertyName"</li>
	<li>property="propertyName" param="propertyName"</li>	
	<li>property="propertyName" value="propertyValue</li>
	</ul>
	
	<jsp:setProperty name="myBean" property="email" />
	<p>轉譯後為:mybean.setEmail(request.getParameter("email"))</p>
	
	<p>request的參數名稱和javaBean元件的欄位名稱不同，則兩者分別指定:</p>
	<jsp:setProperty property="email" name="myBean" param="emailAddress"/>
	<p>轉譯後為:mybean.setEmail(request.getParameter("emailAddress"))</p>
	
	
	<jsp:setProperty property="email" name="myBean" value="joe@host.com"/>
<%-- 	<jsp:setProperty property="email" name="mybean" value="<%=  %>"/> --%>
	<p>property="*"(表示要設定javaBean元件的所有欄位)</p>
	<form action="setProperty.jsp">
	<p>name:<input type="text" id="name" name="name"></p>
	<p>email:<input type="text" id="email" name="email"></p>
	<p>email1:<input type="text" id="email1" name="emailAddress"></p>
	<p>phone:<input type="text" id="phone" name="phone"></p>	
	<input type="submit">
	</form>
	
	mybean:${myBean.email}
	
	
	
</body>
</html>