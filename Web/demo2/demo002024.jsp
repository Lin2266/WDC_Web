<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>Radio 欄位</title>
<style type="text/css">
li { margin:1em 0; }
div.enum li, div.code li, code li { margin:.5em 0; }
</style>
</head>
<body>


<h3>用例</h3>
畫面：
<form name="form1" id="form1">
	<div class="buttons">
		<button onclick="ta.showMessage(JSON.stringify(ta.formToMap('form1'))); return false;">測試</button>
	</div>
	<table class="formbody">
		<tr>
			<th>欄位一：</th>
			<td>
				<t:input type="radio" name="col1" value="v1" label="值1"/> &nbsp;
				<t:input type="radio" name="col1" value="v2" label="值2" checked="checked"/> &nbsp;
				<t:input type="radio" name="col1" value="v3" label="值3"/> &nbsp;
				<t:input type="radio" name="col1" value="v4" label="值4"/> &nbsp;
			</td>
		</tr>
	</table>
</form>
code：
<code>&lt;t:input type="radio" name="col1" value="v1" label="值1"/&gt;
&lt;t:input type="radio" name="col1" value="v2" label="值2" checked="checked"/&gt;
&lt;t:input type="radio" name="col1" value="v3" label="值3"/&gt;
&lt;t:input type="radio" name="col1" value="v4" label="值4"/&gt;
</code>

<h3>說明</h3>
<ol>
	<li>亦可直接使用原始 HTML &lt;input type="radio"/&gt; 標籤實作
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
	<li>使用 &lt;<b>t:input</b> type="<b>radio</b>"/&gt; 標籤
		<div class="enum">
			<b>&lt;t:input type="radio"&gt;</b> 擁有如下屬性：
			<ol>
				<li><b>name</b>： (string)
				<li><b>type</b>： (string) 在此專指 radio
				<li><b>value</b>： (string) 欄位值
				<li><b><i>label</i></b>： (string) 與本 radio 對應的 label 文字
				<li><b><i>id</i></b>： (string, optional)
				<li><b><i>readonly</i></b>： (string, optional) 指定欄位是否為唯讀, 值可為: readonly
				<li><b><i>disabled</i></b>： (string , optional) 指定欄位是否為無作用, 值可為: disabled 
				<li><b><i>title</i></b>：  (string, optional) 當滑鼠游標停駐於欄位上時, 可顯示提示文字
				<li><b><i>onclick</i></b>： (string, optional) click 事件處理器 
				<li><b><i>cssClass</i></b>： (string, optional) CSS class 
				<li><b><i>style</i></b>： (string, optional) CSS style
			</ol>
		</div>
	<li>與本 radio 標籤搭配的 &lt;label/&gt;標籤(如果有指定 label 屬性的話)會自動編列 id 屬性，規則如下：
		<code>[radio欄位id]<b>_label</b></code>
</ol>



<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002024.jsp.txt" target="_blank">demo002024.jsp</a>
</div>

</body>
</html>