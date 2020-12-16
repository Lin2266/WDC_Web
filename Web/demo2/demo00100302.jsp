<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>子頁二</title>
<script>
function queryAction2(primaryKey) {
	var pk = primaryKey; //由 function 參數傳入
	if(pk == undefined)
		pk = document.form2.pk.value; //由預先在 form 埋入的欄位給值
	if(pk == "") //無查詢條件者, 到此為止
		return;
	
	//ta.postForJSON("queryAction", { pk:pk }, function(ret) { //note: 在主頁中呼叫本被引入頁, 這樣子就可能呼叫不到正確的 action 了
	ta.postForJSON("${pageContext.request.contextPath}/demo00100302.ajax?_action=queryAction", { pk:pk }, function(ret) {
		ta.mapToForm(ret, "form2");
	});
}
function updateAction2() {
	//ta.postForJSON("updateAction", "form2", function(ret) { //note: 在主頁中呼叫本被引入頁, 這樣子就可能呼叫不到正確的 action 了
	ta.postForJSON("${pageContext.request.contextPath}/demo00100302.ajax?_action=updateAction", "form2", function(ret) {
		ta.showMessage(ret.message);
	});
}
</script>
</head>
<body onload="queryAction2();">

<form name="form2" id="form2">
	<div class="buttons">
		<button onclick="updateAction2(); return false;">確定修改</button>
	</div>
	
	<table class="formbody">
		<tr>
			<th>primary key：</th>
			<td colspan="3">
				<input type="text" name="pk" readonly="readonly"/>
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
</form>

</body>
</html>