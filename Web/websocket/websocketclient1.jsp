<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.11.3.min.js"></script> 
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<title>Java後端WebSocket的Tomcat實現</title>
</head>
<body>
Welcomer<br>
<input type="text" id="text" name="text">
<button onclick="send()">傳送訊息</button>
<hr>
<button onclick="closeWebSocket()">關閉WebSocket連線</button>
<hr>
<div id="message"></div>
</body>
<script type="text/javascript">
var websocket = null;
//判斷當前瀏覽器是否支援webSocket
if('WebSocket' in window){
	//對應java的@ServerEndpoint("/websocket1")
	websocket = new WebSocket("ws://localhost:900/WDC_Web/websocket1");
}else{
	alert('當前瀏覽器 Not support websocket');
}

//將訊息顯示在網頁上
function setMessageInnerHTML(innerHTML){
	document.getElementById("message").innerHTML += innerHTML + '<br>';
}
//關閉WebSocket連線
function closeWebSocket() {
	websocket.close();
}
//傳送訊息
function send() {
	var message = document.getElementById("text").value;
	websocket.send(message);
}

//連線發生錯誤的回撥方式
websocket.onerror = function (){
	setMessageInnerHTML("WebSocket連線發生錯誤");
}
//連線成功的回撥方式
websocket.onopen = function (){
	setMessageInnerHTML("WebSocket連線成功");
}
//接收到訊息的回撥方法
websocket.onmessage = function (event){
	setMessageInnerHTML(event.data);
}
//連線關閉的回撥方式
websocket.onclose = function (){
	setMessageInnerHTML("WebSocket連線關閉");
}
//監聽視窗關閉事件，當視窗關閉時，主動去關閉websocket連線，防止連線還沒斷開就關閉視窗，server端會拋異常。
window.onbeforeunload = function (){
	closeWebSocket();
}
</script>
</html>