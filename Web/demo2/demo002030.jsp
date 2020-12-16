<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"  errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>Portlet 視窗</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.portlet.css" />
<style type="text/css">
.red { color:red; font-weight:bold; }
.green { color:green; font-weight:bold; }
.blue { color:blue; font-weight:bold; }
.gray { color:#98AFC7; font-weight:bold; }

.template2Class { border: 3px solid #FDD017; }
.template2Icon { background: transparent url(${pageContext.request.contextPath}/images/calendar.gif) no-repeat 20% 40%; } /* 自訂 portlet 上方左側小圖示 */
.template2TitleClass { background: #FDD017 !important; border-radius:0; border:0; } /* 覆蓋原始背景設定 */
.template2Btn2 { background: transparent url(${pageContext.request.contextPath}/images/assist.gif) no-repeat 20% 0 !important; } /* 自訂 portlet 按鈕的 class (要覆蓋預設按鈕設定) */

table.desc {
	border-collapse: collapse;
	border: 1px solid #777;
	border-right: 0;
	border-bottom: 0;
	border-spacing: 0;
	width: 100%;
}
table.desc tr td {
	border: 1px solid #777;
	border-left: 0;
    border-top: 0;
    padding: 3px;
    text-align: left;
}
table.desc tr th {
	background-color: #dde9ff;
	border: 1px solid #777;
    border-left: 0;
    border-top: 0;
    padding: 3px 3px 3px 5px;
    color: #004070;
    font-weight: normal;
    text-align: left;
    white-space: nowrap;
}
comment, .comment {
	color: #15317E;
}
</style>
<script src="${pageContext.request.contextPath}/js/portlet.js"></script>
<script>
function import00100302Action() {
	$portlets1.add({
		id: "IncludePage2", //必要
		title: "動態引入 demo00100302",
		url: "${pageContext.request.contextPath}/demo00100302.inc", //引入其他畫面
		args: { pk: 11111 },
		height: "300px",
		maximizedHeight: "600",
		columnIndex: 1,
		onmaximize: function() { ta.hide("buttons1"); },
		onnormalize: function() { ta.show("buttons1"); },
		onAfterRefresh: function(a, b, c) { ta.showMessage("id='" + c.id + "' portlet refreshed!"); }
	});
}
function createEmptyPortletAction() {
	$portlets1.add({ //沒有 url 屬性, 所以不發出 Ajax request 取外部 HTML 內容
		id: "template2", //必要(以下其他屬性都是 optional)
		
		title: "空內容 Portlet, id='template2'",
		content: "(空)", //只有當 url 屬性沒有指定時, 此屬性才有作用
		cssClass: "template2Class", //自訂 portlet 框的 CSS class
		titleClass: "template2TitleClass", //自訂 title bar 之 CSS class
		iconClass: "template2Icon", //自訂 title bar 左方的小圖示 CSS class
		columnIndex: 3, //自訂放置位置 x
		rowIndex: 1, //自訂放置位置 y
		disableRefresh: true, //取消重載入功能
		buttons: [ //自訂按鈕
           	{ onclick: function(a) { ta.showMessage("portlet id='" + a.id + "' 第一顆自訂按鈕! 使用預設 CSS class"); } }, //第一顆自訂按鈕
           	{ cssClass: "template2Btn2", onclick: function(a) { ta.showMessage("portlet id='" + a.id + "' 第二顆自訂按鈕! 自訂樣式 template2TitleClass"); } } //第二顆自訂按鈕
		],
		onAfterShow: function(a, b) { //portlet 顯示完畢後
			ta.showMessage("id='" + b.id + "' portlet content loading completed!"); 
		},
		onclose: function(p) { //portlet 視窗關閉時
			if(!confirm("id='" + p.id + "' portlet will be closed, are you sure?"))
				return false; 
		},
		onmaximize: function(a, b) { //portlet 視窗最大化時
			ta.showMessage("id='" + b.id + "' portlet maximized");
		},
		onnormalize: function(a, b) { //portlet 視窗由最大化恢復時
			ta.showMessage("id='" + b.id + "' portlet un-maximized");
		},
		onDragStop: function(a) { //portlet 拖拉至定位時
			ta.showMessage("id='" + a.id + "' portlet drag stopped...");
		}
	});
}
function closePortlet(id) {
	$portlets1.close(id);
}
function closeAllPortlet() {
	$portlets1.closeAll();
}
function showPortletPosition(id) {
	ta.showMessage($portlets1.position(id));
}
</script>
</head>
<body>

<div id="buttons1" class="buttons">
	<button onclick="import00100302Action(); return false;">引入00100302畫面</button>
	<button onclick="showPortletPosition('includedPage1'); return false;">求demo00100301視窗坐標</button>
	<button onclick="createEmptyPortletAction(); return false;">建立空 Portlet 於 (0,1)</button>
	<button onclick="closePortlet('template2'); return false;">關閉空 Portlet</button>
	<button onclick="closeAllPortlet(); return false;">關閉全部 Portlet</button>
</div>
<p/>

<t:portlets id="portlets1">

	<!-- 欄位一 -->
	<t:portletColumn width="500">
		
		<!-- 既存的 portlet 視窗 -->
		<t:portlet id="template1" title="說明" height="300px" maximizedHeight="100%"
				isDisableClose="true" isDisableMove="true" isDisableMinimize="true">
			<ul>
				<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
				<li>JSP 之 &lt;head&gt; 區加入 portlet JavaScript/CSS 程式庫：
					<code>&lt;link rel="stylesheet" type="text/css" href="\${pageContext.request.contextPath}/css/jquery.portlet.css" /&gt;

&lt;script src="\${pageContext.request.contextPath}/js/portlet.js"&gt;&lt;/script&gt;</code>
				<li>在 html body 裡建立「portlet 容器」、「portlet 欄位」二元素。<br/>
					如果有畫面載入之初既存的「portlet 視窗」，也要一同建立。<br/>&nbsp;<br/>
					例：欲建立具有二主欄位的 portlet 容器，第一欄位中有一個既存 portlet 視窗：
					<div style="border:1px dotted black; display:table; vertical-align:top; padding:.5em; margin:.5em; width:24em; background-color:#FFF8C6">
						portlet 容器<br/>
						&nbsp;
						<div style="border:1px dotted black; display:inline-block; padding:.5em; margin:.5em; width:10em; height:100%; float:left; background-color:#CCFB5D">
							欄位一(index=0)<br/>
							&nbsp;
							<div style="border:1px dotted black; padding:.5em; margin:.5em; background-color:#CFECEC">
								<div style="float:right; clear:both;">
									<div style="font-size:8px; background-color:#E9CFEC; border:1px dotted black; padding:1px;">
										自訂小按鈕<br/>
										(<i>optional</i>)
									</div>
								</div>
								
								<br/>&nbsp;<br/>
								既存 portlet 視窗<br/>
								&nbsp;<br/>
								...<br/>
								&nbsp;
							</div>
						</div>
						<div style="border:1px dotted black; display:inline-block; padding:.5em; margin:.5em; width:9em; height:100%; float:left; background-color:#CCFB5D">
							欄位二(index=1)<br/>
							&nbsp; <br/>
							&nbsp; <br/>
							&nbsp; <br/>
							&nbsp; <br/>
							&nbsp; <br/>
							&nbsp; <br/>
						</div>
					</div>
					以 taglib 套用如上版面：
<code>&lt;<span class="red">t:portlets</span> id="portlets1"&gt; &lt;!-- portlet 容器 --&gt;
    &lt;<span class="green">t:portletColumn</span> width="500"&gt; &lt;!-- 欄位一(寬度 500px, index=0) --&gt;
        &lt;<span class="blue">t:portlet</span> id="template1" title="既存 portlet 視窗"&gt;
        	<i>&lt;<span class="gray">t:button</span> onclick="..." cssClass="..."/&gt; &lt;!-- 自訂小按鈕(optional) --&gt;</i>
            ...
        &lt;/<span class="blue">t:portlet</span>&gt;
    &lt;/<span class="green">t:portletColumn</span>&gt;
    &lt;<span class="green">t:portletColumn</span> width="500"&gt; &lt;!-- 欄位二(寬度 600px, index=1) --&gt;
    
    &lt;/<span class="green">t:portletColumn</span>&gt;
&lt;/<span class="red">t:portlets</span>&gt;
</code> 
				<li>JSP Taglib:
					<div class="enum">
						<b>&lt;t:portlets&gt;</b> 擁有如下屬性：
						<ol>
							<li><b>id</b>: (string, 必填) portlet 容器的 id. 將於畫面產生 <span style="color:red; font-weight:bold;">"$" + id</span> 的 JavaScript <b>global object</b> (說明如下面的 JavaScript API).
							<li><b><i>isDisableMove</i></b>: (boolean, default false) 使所有 portlet 視窗不可拖動
							<li><b><i>isDisableMaximize</i></b>: (boolean, default false) 使所有 portlet 不可最大化
							<li><b><i>isDisableRefresh</i></b>: (boolean, default false) 使所有 portlet 不可重載入內容
							<li><b><i>onDragStart</i></b>: (string) 所有 portlet 開始拖動前欲執行的 js 碼(最好只放 function call statement), 可取用 "_event" 事件變數
							<li><b><i>onDragStop</i></b>: (string) 所有 portlet 拖動拖動結束後欲執行的 js 碼(最好只放 function call statement), 可取用 "_event" 事件變數
							<li><b><i>cssClass</i></b>: (string) 自訂 portlet 容器框的 CSS class name
							<li><b><i>style</i></b>: (string) 自訂 portlet 容器框的 CSS style
						</ol>
					</div>
					
					<div class="enum">
						<b>&lt;t:portletColumn&gt;</b> 擁有如下屬性：
						<ol>
							<li><b>width</b>: (number, 單位 px) portlet 所在的容器之欄位的寬度
							<li><b><i>cssClass</i></b>: (string) 自訂 portlet 欄位框的 CSS class name
							<li><b><i>style</i></b>: (string) 自訂 portlet 欄位框的 CSS style
						</ol>
					</div>
					
					<div class="enum">
						<b>&lt;t:portlet&gt;</b> 擁有如下屬性：
						<ol>
							<li><b>id</b>: (string, 必填) portlet 視窗的 id
							<li><b><i>title</i></b>: (string) portlet 視窗上方標題框
							<li><b><i>titleClass</i></b>: (string) 自訂 portlet 視窗上方標題框的 CSS class name, 以蓋過預設樣式
							<li><b><i>iconClass</i></b>: (string) 在 portlet 視窗上方標題框內左側加入自訂小圖示, 該圖示的 CSS class name
							<li><b><i>sourceUrl</i></b>: (string) 引入本 portlet 視窗所在之當前頁以外的畫面的 url; 無此屬性者, 不發送 request, 而是以本標籤所含的 HTML 內容為 portlet 之內容
							<li><b><i>requestData</i></b>: (string) 引入外部畫面時, URL 可夾帶的參數(JSON 格式)
							<li><b><i>height</i></b>: (sring) portlet 高度 (可寫成如 "100px" 或 "30%" 等型式)
							<li><b><i>maximizedWidth</i></b>: (string) portlet 最大化後的寬度(可寫成如 "100px" 或 "30%" 等型式)
							<li><b><i>maximizedHeight</i></b>: (string) portlet 最大化後的高度(可寫成如 "100px" 或 "30%" 等型式)
							<li><b><i>isDisableClose</i></b>: (boolean, default false) 是否使 portlet 視窗不可關閉
							<li><b><i>isDisableMaximize</i></b>: (boolean, default false) 是否取消 portlet 視窗最大化功能
							<li><b><i>isDisableMinimize</i></b>: (boolean, default false) 是否取消 portlet 視窗最小化功能
							<li><b><i>isDisableMove</i></b>: (boolean, default false) 是否取消 portlet 視窗可拖動功能
							<li><b><i>isDisableRefresh</i></b>: (boolean, default false) 是否取消 portlet 視窗內容重載入的功能
							<li><b><i>onAfterRefresh</i></b>: (string) 放置當 portlet 視窗內容重新載入後, 欲執行的 js 程式碼(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)<br/>
								(對不可重載入的 portlet 無作用)
							<li><b><i>onMaximize</i></b>: (string) 放置當 portlet 視窗最大化後欲執行的 js 程式碼(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)<br/>
								(對不可最大化的 portlet 無作用)
							<li><b><i>onNormalize</i></b>: (string) 放置當 portlet 視窗自最大化恢復後, 欲執行的 js 程式碼(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)<br/>
								(對不可最大化的 portlet 無作用)
							<li><b><i>onAfterShow</i></b>: (string) 放置當 portlet 視窗內容載入完成後, 欲執行的 js 程式碼(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)
							<li><b><i>onClose</i></b>: (string) 放置當 portlet 視窗關閉前, 欲執行的 js 程式碼, 最後若 return false 者則不關閉(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)<br/>
								(對不可關閉的 portlet 無作用)
							<li><b><i>onDragStart</i></b>: (string) 放置當 portlet 視窗開始拖動前, 欲執行的 js 程式碼(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)<br/>
								(對不可拖動的 portlet 無作用)
							<li><b><i>onDragStop</i></b>: (string) 放置當 portlet 視窗拖動結束後, 欲執行的 js 程式碼(最好只放 function call statement), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)<br/>
							(對不可拖動的 portlet 無作用)
							<li><b><i>cssClass</i></b>: (string) 自訂 portlet 框的 CSS class name
							<li><b><i>style</i></b>: (string) 自訂 portlet 框的 CSS style
						</ol>
					</div>
					
					<div class="enum">
						<b>&lt;t:button&gt;</b> 可擁有如下屬性：
						<ol>
							<li><b>onclick</b>: (string) 指定按鈕按下時要執行的 JavaScript 碼(最好只放 function 呼叫), 可取用 "_portlet" 變數(portlet 視窗的 HTML DOM 物件)
							<li><b><i>cssClass</i></b>: (string, optional) 指定按鈕的 CSS class name 以蓋過預設樣式.<br/> 
								CSS class 內容應以 background 指令控制樣式, 如下例 xxx class 直接寫在當前 JSP 檔中時: 
								<code>.xxx {
    background: transparent url(\${pageContext.request.contextPath}/images/foo.gif) no-repeat 20% 0 !important;
}
</code>
								或者把 xxx class 內容寫在 \${pageContext.request.contextPath}/css/xxx.css 中：
								<code>.xxx {
    background: transparent url(../images/foo.gif) no-repeat 20% 0 !important;
}
</code>
						</ol>
					</div>
					
				<li>Client 端 JavaScript API:  (<span class="comment">配合 main.js + portlet.js + jQuery + jQuery-UI</span>)
					<div style="font-weight:lighter; font-size:90%; font-style:italic;">method: <a href="#portletAdd">add()</a>, <a href="#portletClose">close()</a>, <a href="#portletCloseAll">closeAll()</a>, <a href="#portletPosition">position()</a></div>
					<table class="desc">
						<tr>
							<th><b>portlet 容器物件</b></th>
							<td colspan="3">例: 對 <b>id="portlets1"</b> 的全域 porlet 容器物件, 操作其 method
<pre>JSP tag:
    &lt;t:portlets id="<b>portlets1</b>"&gt;
        ...
    &lt;/t:portlets&gt;
			
JavaScript code:
    <b>$portlets1</b>.add(...);
    <b>$portlets1</b>.close(...);</pre>
							</td>
						</tr>
						<tr><th></th><td></td><td></td><td></td></tr>
						<tr><th>method</th><th>參數</th><th>傳回值</th><th>說明</th></tr>
						<tr>
							<th id="portletAdd"><b>add</b>(<br/>options)</th>
							<td>
								<ul>
									<li><b>options: map</b> 新 portlet 視窗資訊. 結構如下:
 <pre>{ <span class="comment">//以下斜體字參數, 代表為 optional 參數</span>
  <b>id</b>: "…", //<span class="comment">(string) 新 portlet 視窗的 id</span>
  <i>cssClass</i>: "…", //<span class="comment">(string) 新 portlet 視窗的自訂 CSS class name</span>
  <i>style</i>: "…", //<span class="comment">(string) 新 portlet 視窗的自訂 CSS style</span>
  <i>title</i>: "…", //<span class="comment">(string, default "") portlet 視窗標題</span>
  <i>titleClass</i>: "…", //<span class="comment">(string, default "") portlet 視窗標題 CSS class</span>
  <i>iconClass</i>: "…", //<span class="comment">(string, default null)</span>
                  //<span class="comment">portlet 視窗標題欄小圖示之 CSS class</span>
  <i>url</i>: "…", //<span class="comment">(string, optional) 欲引入的畫面之 URL</span>
            //<span class="comment">須含 context path, 可在後面夾帶參數</span>
            //<span class="comment">未指定 url 者, 不發送 request</span>
  <i>content</i>: "…", //<span class="comment">(string, optional) 欲置入 portlet 內容的 HTML 文字</span>
                //<span class="comment">只有當 url 屬性未指定時才有作用</span>
  <i>args</i>: {…}, //<span class="comment">(map 或 string) 發送引入畫面 request 所帶的參數.</span>
             //<span class="comment">type=string: form id, 欄位值將化為 request 參數</span>
             //<span class="comment">type=map: 單層 map</span>
  <i>height</i>: "…", //<span class="comment">(string) portlet 視窗高度(如: 300 或 300px)</span>
  <i>columnIndex</i>: …, //<span class="comment">(number, default 0) 新 portlet 放置位置</span>
                  //<span class="comment">(欄位, 自 0 起算)</span>
  <i>rowIndex</i>: …, //<span class="comment">(number, default null) 新 portlet 視窗放置</span>
               //<span class="comment">(列, 自 0 起算, 預設在最下方)</span>
  <i>maximizedWidth</i>: "…", //<span class="comment">(string) portlet 最大化後視窗寬度</span>
                       //<span class="comment">(如: 600 或 600px 或 100%)</span>
  <i>maximizedHeight</i>: "…", //<span class="comment">(string) portlet 最大化後的視窗高度</span>
                        //<span class="comment">(如: 600 或 600px 或 100%)</span>
  <i>buttons</i>: …, //<span class="comment">(array of map, default false) 自訂按鈕</span>
              //<span class="comment">map 型式: { cssClass:"…", onclick:function() {…}}</span>
              //<span class="comment">  cssClass: (optional)自訂按鈕CSS樣式, 取代預設樣式</span>
              //<span class="comment">  onclick: click 事件處理函數,</span>
              //<span class="comment">           可接受 portlet DOM 物件為參數(optional)</span>
  
  <i>disableClose</i>:…,//<span class="comment">(boolean, default false) portlet 不可關閉</span>
  <i>disableMaximize</i>:…,//<span class="comment">(boolean, default false) portlet 不可最大化</span>
  <i>disableMinimize</i>:…,//<span class="comment">(boolean, default false) portlet 不可最小化</span>
  <i>disableRefresh</i>:…,//<span class="comment">(boolean, default false) portlet 不可重載入</span>
                   //<span class="comment">false 時表示可再次發出 Ajax request 重載入內容</span>
  
  <i>onmaximize</i>:…,//<span class="comment">(function) portlet 最大化後執行的 callback</span>
               //<span class="comment">callback 第一參數(optional): portlet 設定(map)</span>
               //<span class="comment">callback 第二參數(optional): portlet DOM 物件</span>
  <i>onnormalize</i>:…,//<span class="comment">(function) 最大化 portlet 恢復後執行的 callback</span>
                //<span class="comment">callback 第一參數(optional): portlet 設定(map)</span>
                //<span class="comment">callback 第二參數(optional): portlet DOM 物件</span>
  <i>onclose</i>:…,//<span class="comment">(function) portlet 關閉前被執行的 callback</span>
            //<span class="comment">若 callback return false 者, 則不關閉 portlet</span>
            //<span class="comment">callback 參數(optional): portlet DOM 物件</span>
  <i>onAfterRefresh</i>:…,//<span class="comment">(function) portlet 重新整理後執行的 callback</span>
                   //<span class="comment">callback 第一參數(optional): portlet 內容(string)</span>
                   //<span class="comment">callback 第二參數(optional): portlet 設定(map)</span>
                   //<span class="comment">callback 第三參數(optional): portlet DOM 物件</span>
  <i>onAfterShow</i>:…,//<span class="comment">(function) portlet 繪製完畢後欲執行的 callback</span>
                //<span class="comment">callback 第一參數(optional): portlet 內容(string)</span>
                //<span class="comment">callback 第二參數(optional): portlet DOM 物件</span>
  <i>onDragStart</i>:…,//<span class="comment">(function) portlet 開始拖動事件之 callback</span>
                //<span class="comment">callback 參數(optional): portlet DOM 物件</span>
  <i>onDragStop</i>:…,//<span class="comment">(function) portlet 拖動結束事件之 callback</span>
               //<span class="comment">callback 參數(optional): portlet DOM 物件</span>
}</pre>
								</ul>
							</td>
							<td>portlet 容器物件自身</td>
							<td>在當前 portlet 容器內, 插入一個 portlet 視窗</td>
						</tr>
						<tr>
							<th id="portletClose"><b>close</b>(<br/>portletId)</th>
							<td>
								<ul>
									<li><b>portletId: string</b> portlet 視窗的 id
								</ul>
							</td>
							<td>portlet 容器物件自身</td>
							<td>關閉指定的 portlet 視窗, <br/>但對已設為<b>不可關閉</b>的 portlet 無作用.</td>
						</tr>
						<tr>
							<th id="portletCloseAll"><b>closeAll</b>()</th>
							<td></td>
							<td>portlet 容器物件自身</td>
							<td>關閉當前 portlet 容器內的所有 portlet 視窗, <br/>但對已設為<b>不可關閉</b>的 portlet 無作用.</td>
						</tr>
						<tr>
							<th id="portletPosition"><b>position</b>(<br/>portletId)</th>
							<td>
								<ul>
									<li><b>portletId: string</b> portlet 視窗的 id
								</ul>
							</td>
							<td>(array of int)<br/>x-y 坐標陣列(自 0 起算): <br/>[columnIndex, rowIndex]</td>
							<td>求 portlet 視窗在 portlet 容器中的位置.</td>
						</tr>
					</table>
			</ul>
		</t:portlet>
		
		<!-- 既存的 portlet 視窗 -->
		<t:portlet id="includedPage1" title="引入 demo00100301" sourceUrl="${pageContext.request.contextPath}/demo00100301.inc" requestData="{abc:'xyz'}" maximizedHeight="700">
			<t:button onclick="ta.showMessage('這是自訂按鈕!');"/><%-- 自訂小按鈕 --%>
		</t:portlet>
	
	</t:portletColumn>
	
	
	<!-- 欄位二 -->
	<t:portletColumn width="600">
		<t:portlet id="template3" title="id=template3 預置於第二欄">
			zzz
		</t:portlet>
	</t:portletColumn>
</t:portlets>


<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002030.jsp.txt" target="_blank">demo002030.jsp</a>
</div>
</body>
</html>