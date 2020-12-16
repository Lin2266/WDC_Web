<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>多畫面的導頁</title>
<script type="text/javascript">
function submit1() {
	//server 端傳回新頁的 URI(不含 context path)
	ta.postForJSON("doSomethingAction", "form1", function(ret) {
		//ta.submit("${pageContext.request.contextPath}" + ret); //server 如果傳回新頁網址的話, 也可在此換頁
		ta.html("messages", "server 傳回：<br/>col1=" + ret["col1"] + "<br/>col2=" + ret["col2"] + "<br/>target1=" + ret["target1"]);
		ta.show("messages");
	});
}
function submit2() {
	//傳統方式:
	//document.form1.action = "${pageContext.request.contextPath}/demo001010.jsp?_action=doSomething2Action";
	//document.form1.submit();
	
	ta.submit("doSomething2Action", "form1"); //換頁至當前頁, 將 form1 欄位值化為傳送參數, 執行 backing bean 的 "doSomething2Action" method
}
</script>
</head>
<body>

<div class="buttons">
	<button onclick="submit1(); return false;">Ajax不換頁</button> &nbsp;
	<button onclick="submit2();">submit換頁</button>
</div>

<form name="form1" id="form1" method="post">
	<table class="formbody">
		<tr>
			<th>
				<label>欄位一：</label>
			</th>
			<td>
				<input type="text" name="col1"/>
			</td>
			<th>
				<label>欄位二：</label>
			</th>
			<td>
				<input type="text" name="col2"/>
			</td>
		</tr>
		<tr>
			<th>
				<label>欄位三：</label>
			</th>
			<td colspan="3">
				<input type="radio" name="target1" value="1" checked="checked"/><label>選項一</label> &nbsp; &nbsp;
				<input type="radio" name="target1" value="2"/><label>選項二</label> &nbsp; &nbsp;
				<input type="radio" name="target1" value="3"/><label>選項三</label> &nbsp; &nbsp;
				<input type="radio" name="target1" value="4"/><label>選項四</label> &nbsp; &nbsp;
				<input type="radio" name="target1" value="5"/><label>選項五</label> &nbsp; &nbsp;
				<input type="radio" name="target1" value="6"/><label>選項六</label> &nbsp; &nbsp;
			</td>
		</tr>
	</table>
</form>
<div id="messages" style="display:none"></div>

&nbsp;
<p/>
<ul>
	<li>使用 Ajax 者，不離開當頁，直接在指定位置顯示 server 傳回的資料
	<li>有時 SA 就是非得要設計出每個動作都要換頁的畫面：
		<ul>
			<li>使用 forward 者(導頁後的瀏覽器網址不等於導頁後的實際網址)
				<ol>
					<li>JSP 上的 action 呼叫 ta.submit()
					<li>backing bean 上的　action method 要標注 @Action，
						最後　return "forward:下一頁的網址" 即可
						(網址不用指定 context path)
					<li>欲傳遞參數至下一頁者，使用 <a href="${pageContext.request.contextPath}/demo000001.jsp#關於 backing bean 的 scope" target="_blank"><b>Flash scope 物件</b></a>
				</ol>
			<li>使用 redirect 者(瀏覽器將發出第二個 request 指向下一頁網址)
				<ol>
					<li>JSP 上的 action 呼叫 ta.submit()
					<li>backing bean 上的　action method 要標注 @Action，
						最後　return "redirect:下一頁的網址" 即可
						(網址<b>要</b>指定 context path，或指定本 AP 以外的其他網頁的完整網址)
					<li>欲傳遞參數至下一頁者，使用 <a href="${pageContext.request.contextPath}/demo000001.jsp#關於 backing bean 的 scope" target="_blank"><b>Flash scope 物件</b></a>，
						或直接接在　return "redirect:...."　敘述之後變成「return "redirect:...?xxx=xxx&xxx=xxxx"」
				</ol>
			<li>其他特殊傳遞參數方法，也請參考： 「<a href="${pageContext.request.contextPath}/demo000001.jsp#關於 backing bean 的 scope" target="_blank">關於 backing bean 的 scope</a>」
	</ul>
</ul>



<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo001010.jsp.txt" target="_blank">demo001010.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo001010.java.txt" target="_blank">Demo001010.java</a>
</div>

</body>
</html>