<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>多對一資料表 Master-Detail Grid</title>
<script>
function queryDetailAction(rowdata) {
	ta.hide("detaildetail");
	ta.postToGrid("queryDetailAction", { pk: rowdata["id_"] }, "grid2");
}
function showDetailDetailAction(rowdata) {
	ta.mapToForm(rowdata, "form1"); //暫只把該筆內容直接顯示在 form 上
	ta.show("detaildetail");
}
</script>
</head>
<body>

<h3>Master：</h3>
<t:grid id="grid1" dataSourceURL="queryAction1" onSelectRow="queryDetailAction(_rowdata);" rowsPerPage="3" defaultHeight="100">
	<t:column name="id_" label="" hidden="true"/>
	<t:column name="col1" label="欄位一"/>
	<t:column name="col2" label="欄位二"/>
	<t:column name="col3" label="欄位三"/>
</t:grid>

<p/>
<h3>Detail：</h3>
<t:grid id="grid2" onSelectRow="showDetailDetailAction(_rowdata)">
	<t:column name="id_" label="" hidden="true"/>
	<t:column name="cola" label="欄位A"/>
	<t:column name="colb" label="欄位A"/>
</t:grid>

<div id="detaildetail" style="display:none">
	<form name="form1" id="form1">
		<table class="formbody">
			<tr>
				<th>欄位A：</th>
				<td>
					<input type="text" name="cola" readonly="readonly"/>
				</td>
				<th>欄位B：</th>
				<td>
					<input type="text" name="colb" readonly="readonly"/>
				</td>
			</tr>
		</table>
	</form>
</div>


<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo001002.jsp.txt" target="_blank">demo001002.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo001002.java.txt" target="_blank">Demo001002.java</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/model/vo/Test4.java.txt" target="_blank">Test4.java (VO)</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/model/vo/Test5.java.txt" target="_blank">Test5.java (VO)</a>
</div>

</body>
</html>