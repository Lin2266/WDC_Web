<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>子頁三</title>
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
function clean1() {
	$grid2.clean();
}
function queryAction1() {
	ta.postToGrid("queryAction", {}, "grid2"); //note: 在主頁中呼叫本被引入頁, 這樣子就可能呼叫不到正確的 action 了
}
</script>
</head>
<body>

<div id="loading_"><img src="${pageContext.request.contextPath}/images/ajax-loading.gif"/></div>
<script>ta.registerLoadingMarkId("loading_");</script>


<t:grid id="grid2" dataSourceURL="queryAction"
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