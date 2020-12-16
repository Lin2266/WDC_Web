<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>日期欄位</title>
<style type="text/css">
li { margin:1em 0; }
div.enum li, div.code li, code li { margin:.5em 0; }
</style>
</head>
<body>

<h3>用例</h3>
畫面：
<form name="form1" id="form1">
	<table class="formbody">
		<tr>
			<th>生日：</th>
			<td>
				<t:input type="date" name="birthday"/>
			</td>
		</tr>
		<tr>
			<th>生日2：</th>
			<td>
				<t:input type="date" name="birthday2" isROCYear="true" placeholder="民國年, YYY-MM-DD"/>
			</td>
		</tr>
	</table>
</form>
code：
<code>&lt;<b>t:input</b> type="<b>date</b>" name="birthday"/&gt;
&lt;<b>t:input</b> type="<b>date</b>" name="birthday2" isROCYear="true" placeholder="民國年, YYY-MM-DD"/&gt;
</code>

<h3>說明</h3>
<ol>
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
	<li>在原預訂放置日期文字欄位 &lt;input&gt; 之處，放置 &lt;<b>t:input</b> type="<b>date</b>"/&gt; 標籤<br/>
		(<span class="comment">注意：&lt;input type="date"/&gt; 是 HTML5 才有的型態，一般瀏覽器尚未支援</span>)
		<div class="enum">
			<b>&lt;t:input type="date"&gt;</b> 擁有如下屬性：
			<ol>
				<li><b>name</b>： (string)
				<li><b>type</b>： (string) 在此專指 date
				<li><b><i>id</i></b>： (string, optional)
				<li><b><i>value</i></b>： (string, optional) 欄位值 
				<li><b><i>size</i></b>： (number, optional) 欄位在畫面上的寬度(單位:字元) 
				<li><b><i>maxlength</i></b>： (number, optional) 欄位最大容許輸入字元數 
				<li><b><i>readonly</i></b>： (string, optional) 指定欄位是否為唯讀, 值可為: readonly
				<li><b><i>disabled</i></b>： (string , optional) 指定欄位是否為無作用, 值可為: disabled 
				<li><b><i>placeholder</i></b>： (string , optional) 埋在欄位內的提示字串(IE9- 無作用)
				<li><b><i>title</i></b>：  (string, optional) 當滑鼠游標停駐於欄位上時, 可顯示提示文字 
				<li><b><i>onclick</i></b>： (string, optional) click 事件處理器 
				<li><b><i>onchange</i></b>： (string, optional) change 事件處理器 
				<li><b><i>calendarIcon</i></b>： (string, default "&lt;CONTEXT_PATH&gt;/images/calendar.gif") 自訂月曆小圖示路徑(直接作為 img 標籤的 src 值)
				<li><b><i>isROCYear</i></b>： (boolean, default false) type=date 時, 是否採民國紀元
				<li><b><i>cssClass</i></b>： (string, optional) CSS class 
				<li><b><i>style</i></b>： (string, optional) CSS style
			</ol>
		</div>
	<li>與日期欄位標籤搭配的小圖示(&lt;img/&gt;標籤)會自動編列 id 屬性，規則如下：
		<code>[日期欄位id]<b>_icon</b></code>
</ol>



<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002026.jsp.txt" target="_blank">demo002026.jsp</a>
</div>

</body>
</html>