<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>子頁四</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery-ui.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/ui.jqgrid.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/main.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/calendar.css"/>
<script language="javascript" src="${pageContext.request.contextPath}/js/min/jquery.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/min/jquery.cookie.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/min/jquery-ui.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/min/jquery.form.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/min/jquery.json.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/min/jquery.jqGrid.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/grid.locale-en.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/main.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/utils.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/menus.js"></script>
<script language="javascript" src="${pageContext.request.contextPath}/js/calendar.js"></script>
<script type="text/javascript">ta.internal.initContextPath("<%= request.getContextPath() %>").initRequestURI("<%= request.getContextPath() + request.getServletPath() %>");</script>
<script>
function queryAction2(primaryKey) {
	var pk = primaryKey; //由 function 參數傳入
	if(pk == undefined)
		pk = document.form2.pk.value; //由預先在 form 埋入的欄位給值
	if(pk == "") //無查詢條件者, 到此為止
		return;
	
	ta.postForJSON("queryAction", { pk:pk }, function(ret) {
		ta.mapToForm(ret, "form2");
	});
}
function updateAction2() {
	ta.postForJSON("updateAction", "form2", function(ret) {
		ta.showMessage(ret.message);
	});
}
</script>
</head>
<body onload="queryAction2();">

<form name="form2" id="form2">
	<div id="content">
		<div class="buttons">
			<button onclick="updateAction2(); return false;">確定修改</button>
		</div>
		
		<table class="formbody">
			<tr>
				<th>primary key：</th>
				<td colspan="3">
					<input type="text" name="pk" value="${param.pk}" readonly="readonly"/>
				</td>
			</tr>
			<tr>
				<th>欄位一：</th>
				<td>
					<input type="text" name="col1"/>
				</td>
				<th>欄位二：</th>
				<td>
					<input type="text" name="col2"/>
				</td>
			</tr>
			<tr>
				<th>欄位三：</th>
				<td colspan="3">
					<t:input type="radio" name="col3" value="1" label="值1"/>
					<t:input type="radio" name="col3" value="2" label="值2"/>
					<t:input type="radio" name="col3" value="3" label="值3"/>
				</td>
			</tr>
			<tr>
				<th>欄位四：</th>
				<td colspan="3">
					<input type="text" name="col4"/>
				</td>
			</tr>
		</table>
	</div>
</form>

</body>
</html>