<%@page language="java" contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        
    </head>
    <body>   
        <%
            List<String> list = new ArrayList<>();
            list.add("A");
            list.add("B");
            pageContext.setAttribute("names",list);
            %>
        
        <h2>pageContext:</h2>
        1.${pageContext.request.method}<br> 
        2.${pageContext.request.contextPath}<br>
        3.${pageContext.out.bufferSize}
        
        <h2>pageScope:</h2>
        1.${pageScope.names[0]}<br>
        2.${names[1]}
        
        <h2>requestScope:</h2>
        1.${requestScope.emp2}
        
        <h2>sessionScope:</h2>
        1.${sessionScope.emp1.address.location}<br>
        2.${sessionScope["emp1"].address["location"]}
                
    </body>
</html>
