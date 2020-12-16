<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>子頁一</title>
<script>
function clean1() {
	$grid2.clean();
}
function queryAction1() {
	//ta.postToGrid("queryAction", {}, "grid2"); //note: 在主頁中呼叫本被引入頁, 這樣子就可能呼叫不到正確的 action 了
	ta.postToGrid("${pageContext.request.contextPath}/demo00100301.ajax?_action=queryAction", {}, "grid2");
}
</script>
</head>
<body>

<t:grid id="grid2" dataSourceURL="${pageContext.request.contextPath}/demo00100301.ajax?_action=queryAction"
		isShowBottomNav="true" isShowTopNav="false" isShowRowNo="true">
	<t:column name="col1" label="欄位一"/>
	<t:column name="col2" label="欄位二"/>
	<t:column name="col3" label="欄位三"/>
	<t:column name="col4" label="欄位四"/>
	<t:button onclick="queryAction1()">查詢</t:button>
	<t:button onclick="clean1();">清除</t:button>
</t:grid>

</body>
</html>