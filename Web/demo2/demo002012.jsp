<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>發送 email</title>
<style type="text/css">
.warning { font-size:90% !important; }
</style>
<script>
function init() {
	ta.onReady(function() {
		document.form1.subject.focus();
	});
}
function sendAction() {
	ta.postForJSON("sendAction", "form1", function(ret) {
		ta.showMessage(ret.status);
	});
}
function clean() {
	ta.formClean("form1");
	document.form1.subject.focus();
}
</script>
</head>
<body onload="init();">

<div class="buttons">
	<button onclick="clean(); return false;">清除</button> &nbsp;
	<button onclick="sendAction(); return false;">傳送</button>
</div>

<form name="form1" id="form1">
	<table class="formbody">
		<tr>
			<th width="35%">主題:</th>
			<td>
				<input type="text" name="subject"/>
			</td>
		</tr>
		<tr>
			<th><span class="error">*</span>寄件人:</th>
			<td>
				<input type="text" name="from" placeholder="(如: xxx@tatung.com)"/>
			</td>
		</tr>
		<tr>
			<th>SMTP 帳號:</th>
			<td>
				<input type="text" name="smtpUser" placeholder="(SMTP server 需身份驗證時才填)"/>
			</td>
		</tr>
		<tr>
			<th>SMTP 密碼:</th>
			<td>
				<input type="password" name="smtpPassword" placeholder="(SMTP server 需身份驗證時才填)"/>
			</td>
		</tr>
		<tr>
			<th><span class="error">*</span>收件人1:</th>
			<td>
				<input type="text" name="to" placeholder="(如: xxx@tatung.com)"/>
			</td>
		</tr>
		<tr>
			<th>收件人2:</th>
			<td>
				<input type="text" name="to" placeholder="(如: xxx@tatung.com)"/>
			</td>
		</tr>
		<tr>
			<th>內容:<br/></th>
			<td>
				<textarea name="content" rows="5" cols="50" placeholder="(可含 HTML tag)"></textarea>
			</td>
		</tr>
		<tr>
			<th><span class="error">*</span>SMTP server:</th>
			<td>
				<input type="text" name="smtpServer" value="smtp.office365.com"/>
			</td>
		</tr>
		<tr>
			<th>SMTP port:</th>
			<td>
				<input type="text" name="smtpPort" value="587"/>
			</td>
		</tr>
	</table>
</form>

<p/>&nbsp;<p/>

<ul>
	<li>使用 MailSender 工具來包裹 email 內容並送出
	<li>收件人之類的欄位可能為多值，該類 &lt;input/&gt; 欄位之 name 屬性可設為相同，
		在 backing bean 端以 String[] 型態的屬性接取，請參考本例之 backing bean 原始碼
	<li>內容欄位，除可填寫一般文字，也可以插入 HTML 標籤
	<li>email 內容將為 HTML 格式，預設編碼 UTF-8
</ul>


<div class="footer">
	<hr/>
	原始碼: <a href="${pageContext.request.contextPath}/sourceview/web/demo002012.jsp.txt" target="_blank">demo002012.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo002012.java.txt" target="_blank">Demo002012.java</a>
</div>

</body>
</html>