<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>EL 從servlet取值</title>
    </head>
    <body>
        Test variable:<br>
        1.${myNum}<br>
        2.${myStr}<br><br>
        
        Test Java bean:<br>
        1.${empl.address.location}<br>
        2.${empl.address["location"]}<br><br>
        
        Test Map:<br>
        1.${myMap}<br>
        2.${myMap.empl}<br>
        3.${myMap.empl2}<br>
        4.${myMap["empl"].name}<br>
        5.${myMap.empl.name}<br>
        6.${myMap[myStr].name}<br><br>
        
        Test List:<br>
        1.${myList}<br>
        2.${myList["0"].name}<br>
        3.${myList[0].name}<br>
        4.${myList[myNum].name}<br><br>
        
        Test Array:<br>
        1.${myArray}<br>
        2.${myArray[0].name}<br>
        3.${myArray[1].name}<br>
        3.${myArray["0"].name}<br>
        4.${myArray[myNum].name}<br><br>
        
        <p><%=request.getContextPath()%></p>
        pageContext用來取得JSP隱含物件
        <p>${pageContext.request.contextPath}</p>
        

    </body>
</html>
