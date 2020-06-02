<%@page import="java.util.*"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EL 運算子</title>
</head>
<body>
<%
	String s1 = "jin";
	pageContext.setAttribute("s1",s1);
	
	String s2 = new String("jim");
	pageContext.setAttribute("s2",s2);
	
	String s3 = "jim3";
	pageContext.setAttribute("s3",s3);	
%>
	是否相等:<br>
	1.${5 == 2}<br>
	2.${5 eq 2}<br>
	3.s1 == s2 : ${s1 == s2}<br>
	4.s1 eq s2 : ${s1 eq s2}<br>
	5.s1 == s3 : ${s1 == s3}<br>
	
	是否不相等:<br>
	1.${5 != 2}<br>
<%-- 	會變字串無法使用2.${5 ne 2} --%>
	
	是否小於:<br>
	1.${5 < 2}<br>
	2.${5 lt 2}<br>
	
	是否大於:<br>
	1.${5 > 2}<br>
	2.${5 gt 2}<br>
	
	是否小於等於:<br>
	1.${5 <= 2}<br>
	2.${5 le 2}<br>
	
	是否大於等於:<br>
	1.${5 >= 2}<br>
	2.${5 ge 2}<br>
	
	<%
		List<String> names = new ArrayList<String>();
		names.add("Jim1");
		names.add("Jim2");
		pageContext.setAttribute("names", names);
		
		List<String> jobs = new ArrayList<>();
		pageContext.setAttribute("jobs",pageContext.PAGE_SCOPE);
		
		Map<String, Object> map = new HashMap<>();
		pageContext.setAttribute("map",pageContext.REQUEST_SCOPE);
		
		int[] ss = new int[10];
		pageContext.setAttribute("ss",pageContext.SESSION_SCOPE);
		
	%>
	
	Logical邏輯運算子
	測試陣列或List物件是否為空(empty):<br>
	1.List:${empty names},${names[0]}<br>
	2.List:${empty jobs},${jobs}<br>
	3.Map:${empty map},${map}<br>
	4.array:${empty ss},${ss}<br>
	
	
	
	</body>
</html>