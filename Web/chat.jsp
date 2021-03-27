<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <title>聊天室</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <link href="css/main.css" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Lato" rel="stylesheet" type="text/css">
    <link href="https://fonts.googleapis.com/css?family=Montserrat" rel="stylesheet" type="text/css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <style type="text/css">
        .msg_board {
            width: 600px;
            height: 400px;
            border: solid 1px darkcyan;
            padding: 5px;
            overflow-y: scroll;
        // 文字長度大於div寬度時換行顯示
        word-break: break-all;
        }
        /*set srcoll start*/
        ::-webkit-scrollbar
        {
            width: 10px;
            height: 10px;
            background-color: #D6F2FD;
        }
        ::-webkit-scrollbar-track
        {
            -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,0.3);
            /*border-radius: 5px;*/
            background-color: #D6F2FD;
        }
        ::-webkit-scrollbar-thumb
        {
            height: 20px;
            /*border-radius: 10px;*/
            -webkit-box-shadow: inset 0 0 6px rgba(0,0,0,.3);
            background-color: #89D7F7;
        }
        /*set srcoll end*/
        #chatframe{
            margin-top: 140px;
            margin-left: 200px;
            margin-bottom: 50px;
        }
    </style>
    <script>
        function changeColor(){
            var color="#f00|#0f0|#00f|#880|#808|#088|yellow|green|blue|gray";
            color=color.split("|");
            document.getElementById("run").style.color=color[parseInt(Math.random() * color.length)];
        }
        setInterval("changeColor()",200);
    </script>
</head>
<body id="myPage" data-spy="scroll" data-target=".navbar" data-offset="50">

<nav class="navbar navbar-inverse navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#myNavbar">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <img src="images/Logo.jpg" alt="logo" class="img-thumbnail" style="width: 100px;height: 50px">
            <a class="navbar-brand" href="index.jsp">TH SocialMedia</a>
        </div>
        <div>
            <div class="collapse navbar-collapse" id="myNavbar">
                <ul class="nav navbar-nav navbar-right">
                    <li><a href="main.jsp">主頁</a></li>
                    <li><a href="">社群網站</a></li>
                    <li><a href="">遊戲</a></li>
                    <li><a href="">動漫</a></li>
                    <li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">旅遊<span class="caret"></span></a>
                        <ul class="dropdown-menu">
                            <li><a href="#section41">景點</a></li>
                            <li><a href="#section42">美食</a></li>
                            <li><a href="#section42">飯店</a></li>
                        </ul>
                    </li>
                    <li><a href="">拍賣網</a></li>
                    <li><a href="">登出</a></li>
                    <li><a href="">設定</a></li>
                </ul>
            </div>
            <div class="row" style="font-size: 20px">
                <marquee direction="scroll" id="run">歡迎進入主頁面</marquee>
            </div>
        </div>
    </div>
</nav>
<div class="container-fluid" id="chatframe">
    <div class="row">
        <div class="row-cols-6">
    <label>房間名</label>
    <input id="input_roomName" size="10" maxlength="10">
    <input type="button"  value="進入聊天室" onclick="initWebSocket()" />
    <input type="button" value="退出聊天室" onclick="closeWs()" /><br>
    <div class="msg_board"></div>
    <input id="input_msg" size="43" maxlength="40">
    <input type="button" value="傳送" onclick="send_msg()" />
        </div>
    </div>
</div>
<script type="text/javascript">
    var webSocket;

    function send_msg() {
        if (webSocket != null) {
            var input_msg = document.getElementById("input_msg").value.trim();
            if (input_msg == "") {
                return;
            }
            webSocket.send(input_msg);
            // 清除input框裡的資訊
            document.getElementById("input_msg").value = "";
        } else {
            alert("您已掉線，請重新進入聊天室...");
        }
    };

    function closeWs() {
        webSocket.close();
    };

    function initWebSocket() {
        var roomName = document.getElementById("input_roomName").value;
        // 房間名不能為空
        if (roomName == null || roomName == "") {
            alert("請輸入房間名");
            return;
        }
        if ("WebSocket" in window) {
//            alert("您的瀏覽器支援 WebSocket!");
            if (webSocket == null) {
                var url = "ws://192.168.50.62:100/WDC_Web/webSocket/chat/" + roomName;
                // 開啟一個 web socket
                webSocket = new WebSocket(url);
            } else {
                alert("您已進入聊天室...");
            }

            webSocket.onopen = function () {
                alert("已進入聊天室，暢聊吧...");
            };

            webSocket.onmessage = function (evt) {
                var msg_board = document.getElementsByClassName("msg_board")[0];
                var received_msg = evt.data;
                var old_msg = msg_board.innerHTML;
                msg_board.innerHTML = old_msg + received_msg + "<br>";
                // 讓滾動塊往下移動
                msg_board.scrollTop = msg_board.scrollTop + 40;
            };

            webSocket.onclose = function () {
                // 關閉 websocket，清空資訊板
                alert("連線已關閉...");
                webSocket = null;
                document.getElementsByClassName("msg_board")[0].innerHTML = "";
            };
        }
        else {
            // 瀏覽器不支援 WebSocket
            alert("您的瀏覽器不支援 WebSocket!");
        }
    }
</script>
<!-- Footer -->
<footer class="text-center">
    <a class="up-arrow" href="#myPage" data-toggle="tooltip" title="TO TOP">
        <span class="glyphicon glyphicon-chevron-up"></span>
    </a><br><br>
    <p>Copyright &COPY; TH SocialMedia</p>
</footer>
</body>
</html>
