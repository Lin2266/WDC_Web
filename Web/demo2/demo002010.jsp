<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>檔案上傳</title>
<style type="text/css">
li { margin:1em 0; }
div.enum li, div.code li, code li { margin:.5em 0; }
</style>
<script>
function testAction() {
	document.form1.action = "${pageContext.request.contextPath}/demo002010.jsp?_action=testAction";
	document.form1.submit(); //換頁
}
function testAction2() {
	ta.post("testAction2", "form1", function(ret) { //連同 form1 一般欄位值一齊送出
		ta.html("message1", ta.html("message1") + " &nbsp; " + ret + "<br/>");
	});
}
function uploadAction() {
	ta.post("uploadAction", "form1Dialog", function(ret) { //連同 form1Dialog 一般欄位值一齊送出
		ta.html("message2", ta.html("message2") + " &nbsp; " + ret + "<br/>");
	});
}
function uploadDialog() {
	ta.html("message1", "");
	$dialog1.open();
}
function clean() {
	ta.formClean("form1");
	ta.html("message1", "").html("message2", "");
}
function clean1() {
	ta.formClean("form1Dialog");
	ta.html("message2", "");
}
</script>
</head>
<body>

<ol>
	<li>一次可上傳多個檔案，也可附帶一般欄位參數
	<li>使用法：
		<ul>
		    <li>在 &lt;form&gt; 內置入檔案上傳欄位，例：
		        <code>&lt;input <font color="red">type="file"</font> <font color="blue">name</font>="xxx" class="readonly"/&gt;</code>
	        <li>使用 AJAx 方式 submit form 時(不換頁)，呼叫 ta.post() 或 ta.postForJSON() 即可
	        <li>使用換頁模式 submit form 時，form 一定要有「enctype="multipart/form-data"」屬性，其餘用一般標準 form submit 方式即可
       	</ul>
       	
    <li>server 端 backing bean 取檔案的方法：<br/>
		在 backing bean 設置以 <b>@Input</b> 修飾的 <b>property</b>，與畫面 file 欄位的 name 對應。<br/> 
		其型態可為：
		<div class="enum">
			<ul>
				<li>String 或 File： 代表檔案上傳後的<u>實體路徑</u> (如果 server 端已存在同名檔案，將另存為其他檔名)
				<li>String[] 或 File[]： 同上，但該欄位可接受一次多選上傳檔案。 畫面上的該欄位需有 multiple 屬性(&lt;input type="file" <b>multiple="multiple"</b> .../&gt;)
			</ul>
			<span class="warning">注意1：別把上傳後的檔案<b>實體</b>路徑輕易地顯示在前端畫面，而引發安全疑慮</span><br/>
			<span class="warning">注意2：較舊的瀏覽器(如 IE 9-)不支援 multiple 屬性</span>
		</div>
		<s>或者 backing bean 不設置 File property，而是在 action method 直接使用 <b>JspUtil.uploadFile()</b> 工具取得檔案.</s><br/>
		server 端獲取上傳檔案後，置於暫存目錄下，應立刻處理或移至他處 (<span class="comment">可使用 com.tatung.commons.util.FileUtil.move() 工具，而不應呼叫 File.renameTo()，因實體貯存設備不同者，即告失敗</span>)
</ol>
<br/>
例：
<form name="form1" id="form1" method="POST">
	<fieldset>
		<legend>(1) <b>Form</b> (可含或不含 enctype="multipart/form-data" 屬性)</legend>
	
		上傳時, 連同一般欄位值一齊送出<br/>
		一般欄位：<input type="text" name="testField" value="spam"/><br/>
		檔案上傳：<input type="file" name="fileField1" class="readonly" multiple="multiple"/><br/>
		檔案上傳：<input type="file" name="fileField2" class="readonly" multiple="multiple"/><br/>
		<button onclick="testAction2(); return false;">確定 (不換頁)</button> &nbsp; &nbsp;
		<%-- <button onclick="testAction()">傳統 form submit (要換頁, form 一定要有 enctype="multipart/form-data" 屬性)</button> &nbsp; &nbsp; --%>
		<button onclick="clean(); return false;">清除</button>
		<div id="message1" class="comment">${message}</div><%-- 放置 server 傳回的已上傳資訊 --%>
	</fieldset>
</form>
<code><b>HTML</b>：
&lt;form name="form1" id="form1" method="POST"&gt;
	一般欄位：&lt;input type="text" name="testField" value="spam"/&gt;&lt;br/&gt;
	檔案上傳：&lt;input <b>type="file"</b> name="fileField1" multiple="multiple" class="readonly"/&gt;&lt;br/&gt;
	<span class="warning">(加 multiple="multiple" 屬性代表希望多選上傳, backing bean 對應的屬性一定要為 File[])</span>
	檔案上傳：&lt;input <b>type="file"</b> name="fileField2" multiple="multiple" class="readonly"/&gt;&lt;br/&gt;
	&lt;button onclick="testAction2(); return false;"&gt;確定 (不換頁)&lt;/button&gt;
	&lt;button onclick="clean(); return false;"&gt;清除&lt;/button&gt;
	&lt;div id="message1" class="comment"&gt;${message}&lt;/div&gt;&lt;!-- 放置 server 傳回的已上傳資訊 --&gt;
&lt;/form&gt;

<b>JavaScript</b>：
function testAction2() {
	<b>ta.post</b>("testAction2", "form1", function(ret) { //連同 form1 一般欄位值一齊送出
		ta.html("message1", ta.html("message1") + " &amp;nbsp; " + ret + "&lt;br/&gt;");
	});
}
function clean() {
	ta.formClean("form1");
	ta.html("message1", "").html("message2", "");
}
</code>
<p/>

(2) 使用對話框(另開對話框的新 form):<br/>
<button onclick="uploadDialog(); return false;">上傳...</button>
<code><b>HTML</b>：
&lt;button onclick="uploadDialog(); return false;"&gt;上傳...&lt;/button&gt;

&lt;t:dialog id="dialog1" title="上傳" width="400"&gt;
	&lt;form name="form1Dialog" id="form1Dialog" method="post"&gt;
		一般欄位：&lt;input type="text" name="testField" value="測試test"/&gt;&lt;br/&gt;
		上傳檔案：&lt;input type="file" name="fileField3" class="readonly" 
						onchange="uploadAction(); return false;"/&gt;&lt;br/&gt;
		&lt;span id="message2" class="comment"&gt;&lt;/span&gt;&lt;!-- 放置 server 傳回的已上傳資訊 --&gt;
	&lt;/form&gt;
	&lt;t:button onclick="clean1()"&gt;清除&lt;/t:button&gt;
	&lt;t:button onclick="$dialog1.close()"&gt;關閉&lt;/t:button&gt;
&lt;/t:dialog&gt;

<b>JavaScript</b>：
function uploadDialog() {
	ta.html("message1", "");
	$dialog1.open();
}
function clean1() {
	ta.formClean("form1Dialog");
	ta.html("message2", "");
}
function uploadAction() {
	<b>ta.post</b>("uploadAction", "form1Dialog", function(ret) { //連同 form1Dialog 一般欄位值一齊送出
		ta.html("message2", ta.html("message2") + " &amp;nbsp; " + ret + "&lt;br/&gt;");
	});
}
</code>
			
<%-- 檔案上傳專用小對話框 --%>
<t:dialog id="dialog1" title="上傳" width="400">
	<form name="form1Dialog" id="form1Dialog" method="post">
		一般欄位：<input type="text" name="testField" value="測試test"/><br/>
		　上傳檔案：<input type="file" name="fileField3" multiple="multiple" class="readonly" onchange="uploadAction(); return false;"/><br/>
		<span id="message2" class="comment"></span><%-- 放置 server 傳回的已上傳資訊 --%>
	</form>
	<t:button onclick="clean1()">清除</t:button>
	<t:button onclick="$dialog1.close()">關閉</t:button>
</t:dialog>



<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002010.jsp.txt" target="_blank">demo002010.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo002010.java.txt" target="_blank">Demo002010.java</a>
</div>

</body>
</html>