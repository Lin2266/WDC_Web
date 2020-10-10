<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
<meta charset="UTF-8">
<title>checkbox - forEach</title>
</head>
<body>
	<h1>&lt;c:forEach&gt; Demo</h1>
      <form name="forEachForm"
          action="${pageContext.request.contextPath}/JSTL/forEach_checkbox.jsp"
          method="POST">
             Select your programming companions: <br/>
             C<input type="checkbox" name="langChoice" value="C"/><br/>
             C++<input type="checkbox" name="langChoice" value="C++"/><br/>
             Java<input type="checkbox" name="langChoice" value="Java"/><br/>
             C#<input type="checkbox" name="langChoice" value="C#"/><br/>
             PHP<input type="checkbox" name="langChoice" value="PHP"/><br/>
             Ruby<input type="checkbox" name="langChoice" value="Ruby"/><br/>
             jQuery<input type="checkbox" name="langChoice" value="jQuery"/><br/><br/>
          <input type="submit" value="Submit"/>
   </form>
  <br/>
   You selected:
   <c:forEach var="lang" items="${paramValues.langChoice}">
        <font color="#00CC00"><c:out value="${lang}"/>,</font>
   </c:forEach>
</body>
</html>