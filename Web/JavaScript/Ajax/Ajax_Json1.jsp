<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
String path = request.getContextPath();
String basePath = request.getScheme();
basePath=request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println("basePath="+basePath);
%>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.11.3.min.js"></script> 
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<script type="text/javascript">
$(function($) {
$("#record").append("當前測試頁面完成載入。<br/>");
});
function getFirstFloorValue(element) {
$("#record").append("<br/>獲取到資訊：您將要取得第一級選項資訊……");
$("#record").append("<br/>正在使用ajax為您獲取資料，您可以繼續停留在頁面並進行其他操作。");
$.ajax({
url : 'ValueGetController',
type : 'post',
data : 'action=GetFirstFloorValue',
datatype : 'json',
success : function(data) {
$("#record").append("<br/>操作成功，正在為您準備資料……");
$(element).empty();
$("#record").append("<br/>清除原始資料成功！");
var ops = $.parseJSON(data);
$("#record").append("<br/>即時資料準備成功！");
for ( var i = 0; i < ops.length; i  )
$(element).append(
"<option value=\""   ops[i]   "\">"   ops[i]
"</option>");
$("#record").append("<br/>更新列表成功！<br/>");
}
});
}
</script>
<title>Insert title here</title>
</head>
<body>
<div>
<select id="select1" onfocus=getFirstFloorValue(this)>
<option value="1">點選取值</option>
</select>
</div>
<dir>
<h3>記錄資訊：</h3>
<span id="record"></span>
</dir>
</body>
</html>