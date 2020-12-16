<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>Dialog 對話窗</title>
<style type="text/css">
li { margin:1em 0; }
div.enum li, div.code li, code li { margin:.5em 0; }
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
</style>
<script type="text/javascript">
function openDialog() {
	$dialog0.open();
}
function openDialog1() {
	$dialog1.open();
}
function confirm1() {
	ta.showMessage("你輸入了: 姓名=''" + document.form1.name.value + "', 生日='" + document.form1.birthday.value + "'");
	$dialog1.close();
}
function close1() {
	$dialog1.close();
}
function openDialog2() {
	$dialog2.open();
}
function confirm2() {
	ta.showMessage("你輸入了: 姓名=''" + document.form2.name.value + "', 生日='" + document.form2.birthday.value + "'");
	$dialog2.close();
}
function close2() {
	$dialog2.close();
}
function openDialog3() {
	$dialog3.open();
}
function openDialog3a() {
	$dialog3a.open();
}
function openDialog4() {
	$dialog4.open();
}
function onopen4() {
	ta.showMessage("打開 dialog!");
}
function onclose4() {
	ta.showMessage("關閉 dialog!");
}
</script>
</head>
<body>

<ol>
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
	
	<li>為求方便，彈出視窗並未獨立為另一 JSP 檔，而是直接藏在當前網頁中。<br/>
		在當前 JSP body 內的適當地方，以標籤 <b>&lt;t:dialog&gt;&lt;/t:dialog&gt;</b> 圍住一區域，區域內放置彈出視窗的內容。
		<div class="enum">
		    <b>&lt;t:dialog&gt;</b> 擁有如下屬性：
		    <ol>
		    	<li><b>id</b>： (string, 必填) 彈出視窗內容區塊的 id 屬性
		    	<li><b><i>width</i></b>： (number, 單位 px, 預設 400px) 彈出視窗的預設寬度
		    	<li><b><i>minHeight</i></b>：(number, 單位 px, 預設 150px) 彈出視窗的最小高度
		    	<li><b><i>title</i></b>： (string) 彈出視窗的頂部標題文字
		    	<li><b><i>onOpen</i></b>： (string) 指定對話窗 open 事件的處理程式, 可取用 _dialog 物件(實際的彈出對話窗 DOM 物件)<br/>
											<span class="comment">最好只寫 function 執行碼, 以避免單雙引號的混餚</span>
				<li><b><i>onClose</i></b>： (string) 指定對話窗 close 事件的處理程式, 可取用 _dialog 物件(實際的彈出對話窗 DOM 物件)<br/>
											<span class="comment">最好只寫 function 執行碼, 以避免單雙引號的混餚</span>
				<li><b><i>onCreate</i></b>： (string) 指定對話窗 create 事件的處理程式(於瀏覽器載入畫面階段, dialog 物件生成後觸發), 可取用 _dialog 物件(實際的彈出對話窗 DOM 物件)<br/>
											<span class="comment">最好只寫 function 執行碼, 以避免單雙引號的混餚</span>
		    	<li><b><i>isDefaultOpen</i></b>： (boolean, 預設 false) 是否畫面載入時就開啟
		    	<li><b><i>isModal</i></b>： (boolean， 預設 true) 是否占住整個畫面的焦點
		    	<li><b><i>isCleanFormOnOpen</i></b>： (boolean, 預設 false) 打開對話窗時, 是否順便清空內含的 form 欄位
		    	<li><b><i>isCleanFormReadOnlyFieldOnOpen</i></b>： (boolean, 預設 false) 對話窗打開時是否清空內含 form 的唯讀欄位(isCleanFormOnOpen=true 時才有作用)
		    	<li><b><i>isCleanFormHiddenFieldOnOpen</i></b>： (boolean, 預設 true) 對話窗打開時是否清空內含 form 的隱藏欄位(isCleanFormOnOpen=true 時才有作用)
		    	<li><b><i>isShowOnTop</i></b>： (boolean, 預設 false) 對話窗打開時是否讓對話窗置於畫面頂部(注意: 指定置頂者, 當畫面上下太長而當前視景又駐留在畫面下方時, 會逼使用戶捲頁至畫面上方才看得到對話窗)
		    </ol>
	    </div>
	
	<li>彈出視窗內可置入按鈕：在  <b>&lt;t:dialog&gt;&lt;/t:dialog&gt;</b> 之<b>內</b>的適當處
		插入 <b>&lt;t:button&gt; &lt;/t:button&gt;</b>。<br/>
		<div class="enum">
			<b>&lt;t:button&gt;</b> 擁有如下<b>選擇性</b>屬性：
			<ol>
				<li><b><i>onclick</i></b>： (string) 填入處理按鈕 click 事件的 JavaScript 碼。為免引號「"」和「'」造成的混亂，最好只寫 function 呼叫式。
				<li><b><i>isPressThenCloseDialog</i></b>： (boolean, 預設 false) 是否按下後要關閉對話窗
				<li><b><i>isPressThenCleanDialogForm</i></b>： (boolean, 預設 false) 是否按下後要清空對話窗內的 form 欄位
				<li><b><i>id</i></b>： (string) button element id
				<li><b><i>cssClass</i></b>： (string) CSS class 名字
				<li><b><i>style</i></b>： (string) CSS style 碼
				<li><b><i>disabled</i></b>: (string) 指定為 disabled="disabled" 或不指定
			</ol>
		</div>
		
	<li><span id="gridJavascriptAPI">Client 端 JavaScript dialog API</span>:
		<div style="float:right;">(定義於 main.js, 配合 jQuery + jqGrid)</div>
		<div style="font-weight:lighter; font-size:90%; font-style:italic;"><a href="#dialogClose">close()</a>, <a href="#dialogOpen">open()</a>, <a href="#dialogTitle">title()</a></div>
		<table class="desc">
			<tr>
				<th><b>dialog 物件</b></th>
				<td colspan="3">例: 對 <b>id="diglog1"</b> 的全域 dialog object 操作其 method
<pre>JSP tag:
    &lt;t:dialog id="<b>dialog1</b>"&gt;
        ...
    &lt;/t:dialog&gt;

JavaScript code:
    <b>$dialog1</b>.open();
    <b>$dialog1</b>.close();
</pre>
				</td>
			</tr>
			<tr><th></th><td></td><td></td><td></td></tr>
			<tr><th><b>method</b></th><th><b>參數</b></th><th><b>傳回值</b></th><th><b>說明</b></th></tr>
			<tr>
				<th id="dialogOpen"><b>open</b>()</th>
				<td></td>
				<td>dialog 物件自身</td>
				<td>打開對話視窗</td>
			</tr>
			<tr>
				<th id="dialogClose"><b>close</b>()</th>
				<td></td>
				<td>dialog 物件自身</td>
				<td>關閉對話視窗.<p/>
					例: server 完事後(正常地傳回可解析的 JSON 物件回 client 端), 關閉當前對話窗
<pre>ta.postForJSON("updateAction", ..., function(ret) {
    $dialog1.close();
});</pre>
				</td>
			</tr>
			<tr>
				<th id="dialogTitle"><b>title</b>(<br/>title)</th>
				<td>
					<ul>
						<li><b>title: string</b> 對話視窗標題
					</ul> 
				</td>
				<td>dialog 物件自身</td>
				<td>改變彈出視窗的標題文字</td>
			</tr>
		</table>
		
	<%--
	<li>欲開啟彈出視窗，使用 JavaScript 碼：<br/>
		<code>$彈出視窗的id.open();</code>
		<br/>
		關閉彈出視窗：
		<code>$彈出視窗的id.close();</code>
	--%>
</ol>

例:<br/>
<button onclick="openDialog(); return false;">彈出訊息視窗</button>
<code>//按鈕的 onclick
function <b>openDialog</b>() {
    $dialog1.open();
}
</code>
<code>&lt;<b>t:dialog</b> id="dialog1" title="訊息框"&gt;
    訊息文字...
&lt;/<b>t:dialog</b>&gt;
</code>

<p/>&nbsp;<p/>
<button onclick="openDialog4(); return false;">帶事件的彈出視窗</button>
<code>//按鈕的 onclick
function <b>openDialog</b>() {
    $dialog1.open();
}
function <b>onopen</b>() {
    ta.showMessage("打開 dialog!");
}
function <b>onclose</b>() {
    ta.showMessage("關閉 dialog!");
}
</code>
<code>&lt;<b>t:dialog</b> id="dialog1" onOpen="onopen()" onClose="onclose()"&gt;
    訊息文字...
&lt;/<b>t:dialog</b>&gt;
</code>

<p/>&nbsp;<p/>
<button onclick="openDialog1(); return false;">彈出表單視窗</button>
<code>//按鈕的 onclick
function <b>openDialog</b>() {
    $dialog1.open();
}
function <b>confirm()</b> {
    ta.showMessage("你輸入了: 姓名=''" + document.form1.name.value + "', 生日='" + document.form1.birthday.value + "'");
    $dialog1.close();
}
function <b>close1()</b> {
    $dialog1.close();
}
</code>
<code>&lt;<b>t:dialog</b> id="dialog1"&gt;
    &lt;form name="form1" id="form1" method="post"&gt;
        &lt;table class="formbody"&gt;
            &lt;tr&gt;
                &lt;th&gt;姓名：&lt;/th&gt;
                &lt;td&gt;
                    &lt;input type="text" name="name" value="林大同"/&gt;
                &lt;/td&gt;
            &lt;/tr&gt;
            &lt;tr&gt;
                &lt;th&gt;生日：&lt;/th&gt;
                &lt;td&gt;
                    &lt;input type="text" name="birthday" value="2000-01-01"/&gt;
                &lt;/td&gt;
            &lt;/tr&gt;
        &lt;/table&gt;
    &lt;/form&gt;
    &lt;<b>t:button</b> onclick="confirm()"&gt;確定&lt;/<b>t:button</b>&gt;
    &lt;<b>t:button</b> onclick="close()"&gt;關閉&lt;/<b>t:button</b>&gt;
&lt;/<b>t:dialog</b>&gt;
</code>

<p/>&nbsp;<p/>
<button onclick="openDialog(); return false;">彈出空白表單視窗</button>
<code>//按鈕的 onclick
function <b>openDialog</b>() {
    $dialog1.open();
}
function <b>confirm()</b> {
    ta.showMessage("你輸入了: 姓名=''" + document.form1.name.value + "', 生日='" + document.form1.birthday.value + "'");
    $dialog1.close();
}
function <b>close()</b> {
    $dialog1.close();
}
</code>
<code>&lt;<b>t:dialog</b> id="dialog1" <b>width</b>="500" <b>isCleanFormOnOpen</b>="true"&gt;
    &lt;form name="form1" id="form1" method="post"&gt;
        &lt;table class="formbody"&gt;
            &lt;tr&gt;
                &lt;th&gt;姓名：&lt;/th&gt;
                &lt;td&gt;
                    &lt;input type="text" name="name"/&gt;
                &lt;/td&gt;
                &lt;th>生日：&lt;/th&gt;
                &lt;td&gt;
                    &lt;input type="text" name="birthday"/&gt;
                &lt;/td&gt;
            &lt;/tr&gt;
        &lt;/table&gt;
    &lt;/form&gt;
    &lt;<b>t:button</b> onclick="confirm()"&gt;確定&lt;/<b>t:button</b>&gt;
    &lt;<b>t:button</b> onclick="close()"&gt;關閉&lt;/<b>t:button</b>&gt;
&lt;/<b>t:dialog</b>&gt;
</code>

<p/>&nbsp;<p/>
<button onclick="openDialog3(); return false;">彈出視窗後又再彈出視窗</button>
<code>//按鈕的 onclick
function <b>openDialog</b>() {
    $dialog1.open();
}
function <b>openDialog2</b>() {
    $dialog2.open();
}
</code>
<code>&lt;<b>t:dialog</b> id="dialog1"&gt;
    視窗 1&lt;br/&gt;
    &lt;button onclick="openDialog2(); return false;"&gt;再彈出視窗&lt;/button&gt;
&lt;/<b>t:dialog</b>&gt;
</code>
<code>&lt;<b>t:dialog</b> id="dialog2"&gt;
    視窗 2
&lt;/<b>t:dialog</b>&gt;
</code>


<t:dialog id="dialog0" title="訊息框">
	訊息文字...
</t:dialog>

<t:dialog id="dialog4" onOpen="onopen4()" onClose="onclose4()">
	訊息文字...
</t:dialog>

<t:dialog id="dialog1">
	<form name="form1" id="form1" method="post">
		<table class="formbody">
			<tr>
				<th>姓名：</th>
				<td>
					<input type="text" name="name" value="林大同"/>
				</td>
			</tr>
			<tr>
				<th>生日：</th>
				<td>
					<input type="text" name="birthday" value="2000-01-01"/>
				</td>
			</tr>
		</table>
	</form>
	<t:button onclick="confirm1()">確定</t:button>
	<t:button onclick="close1()">關閉</t:button>
</t:dialog>

<t:dialog id="dialog2" width="500" isCleanFormOnOpen="true">
	<form name="form2" id="form2" method="post">
		<table class="formbody">
			<tr>
				<th>姓名：</th>
				<td>
					<input type="text" name="name"/>
				</td>
				<th>生日：</th>
				<td>
					<input type="text" name="birthday"/>
				</td>
			</tr>
		</table>
	</form>
	<t:button onclick="confirm2()">確定</t:button>
	<t:button onclick="close2()">關閉</t:button>
</t:dialog>

<t:dialog id="dialog3">
	視窗 1<br/>
	<button onclick="openDialog3a(); return false;">再彈出視窗</button>
</t:dialog>

<t:dialog id="dialog3a">
	視窗 2
</t:dialog>


<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002023.jsp.txt" target="_blank">demo002023.jsp</a>
</div>

</body>
</html>