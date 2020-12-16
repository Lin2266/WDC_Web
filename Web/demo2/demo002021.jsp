<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>HTML Select 選單</title>
<style type="text/css">
table.formbody th, table.formbody td { vertical-align: top; }
</style>
<script>
function testAction() {
	ta.selectSelectAll("select6b"); //for 雙欄位受選項目的選單
	ta.showMessage(JSON.stringify(ta.formToMap("form1")));
}
function clean() {
	ta.formClean("form1");
	ta.selectMoveAll("select6b", "select6a"); //for 雙欄位受選項目的選單
}
</script>
</head>
<body>

<ol>
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
	<li>使用 &lt;t:select&gt;，可以在內寫固定的 &lt;option&gt; 內容，也可以利用 dataSourceURL 屬性來動態填入選單內容，
		否則便得自行以 JavaScript ta.selectDraw() 填充內容
	<li>配置：在預定放置 &lt;select&gt; 之處，使用 &lt;t:select&gt;
		<div class="enum">
			<b>&lt;t:select&gt;</b> 擁有如下屬性：
			<ol>
				<li><b>id</b>： (string)
				<li><b>name</b>： (string)
				<li><b><i>cssClass</i></b>： (string, optional) CSS class
				<li><b><i>style</i></b>： (string, optional) CSS style
				<li><b><i>onchange</i></b>： (string, optional) 指定 JavaScript change 事件處理程式碼
				<li><b><i>dataSourceURL</i></b>： (string, optional) 指定提供選單內容資料的 URL, 或可為: 當前畫面 backing bean 之 AJAX action name(或全名 \${pageContext.request.contextPath}/xxxxx.ajax?_action=ACTION_NAME)
				<li><b><i>size</i></b>： (number, optional) 選單框高度(單位:列)
				<li><b><i>multiple</i></b>： (string, optional) 是否多選(是:"multiple")
				<li><b><i>title</i></b>：  (string, optional) 當滑鼠游標停駐於欄位上時, 可顯示提示文字
				<li><b><i>cascadeTo</i></b>： (string, optional) 被連動的 select 選單的 id (本參數設置於關聯式選單之母選單上)
				<li><b><i>cascadeFromURL</i></b>： (string, optional) 提供被連動的選單內容的 Ajax request URL (本參數設置於關聯式選單之子選單上)
				<li><b><i>isTtriggerChangeOnRendered</i></b>： (boolean, default false) 選單內容畫完時, 是否要發動 "change" 事件
			</ol>
		</div>
	<li>server 端：使用 TUISelect 工具，依資料 list 而送出 &lt;select&gt; 內容，例：
<code>import com.tatung.commons.web.widget.TUISelect;
...
List&lt;SomeType&gt; data = ...; //由查詢而來
String options = new <b>TUISelect</b>&lt;SomeType&gt;(data).options(valueName, labelName).toString();
//valueName 代表 SomeType 之用來充當 option value 的屬性名
//labelName 代表 SomeType 之用來充當 option label 的屬性名
//將此字串<b>傳回前端</b>，由前端 JavaScript 置入 &lt;select&gt; 中
</code>
		或者自行控制如何對 VO 取值以充當 option value/label：
<code>import com.tatung.commons.web.widget.TUISelect;
...
List&lt;SomeType&gt; data = ...; //由查詢而來
String options = new <b>TUISelect</b>&lt;SomeType&gt;(data)
    .options(new <b>TUISelectOptionHandler</b>&lt;SomeType&gt;() {
        @Override
        public String value(int index, SomeType vo) {
            return vo.getAaaa();
        }

        @Override
        public String label(int index, SomeType vo) {
            return vo.getXxxx(); //也可在這裡對輸出的 label 字串作額外處理
        }
    }).toString();
</code>
</ol>

例：
<p/>
<form id="form1" name="form1" method="post">
	<div class="buttons">
		<button onclick="testAction(); return false;">測試</button>
		<button onclick="clean(); return false;">清除</button>
	</div>

	<table class="formbody">
		<tr>
			<th>下拉選單:</th>
			<td>
				<t:select name="select1" id="select1" style="width:150px;" dataSourceURL="drawSelect1Action"></t:select>
			</td>
			<td>
<code>&lt;<b>t:select</b> name="select1" id="select1"
    style="width:150px;" 
    dataSourceURL="drawSelect1Action"&gt;
&lt;/<b>t:select</b>&gt;
</code>
				或者不對 &lt;t:select&gt; 指定 dataSourceURL 屬性，而是自行以 JavaScript 接取 server 端所傳出的 JSON 碼(由如上述的 Java 碼所産生)，然後繪出 select 選單的內容：
<code>function drawSelectAction() {
    ta.postForJSON("drawSelectAction", 
        {}, 
        function(ret) {
            ta.<b>selectDraw</b>("select1", ret);
        }
    );
}
</code>
			</td>
		</tr>
		<tr>
			<th>
				選單框(單選):<br/>
				<span class="warning">(不適用於手持裝置)</span>
			</th>
			<td>
				<t:select name="select2" id="select2" size="10" style="width:150px;" dataSourceURL="drawSelect2Action"></t:select>
			</td>
			<td>
<code>&lt;<b>t:select</b> name="select2" id="select2" 
    <b>size</b>="10"
    style="width:150px;" 
    dataSourceURL="drawSelect2Action"&gt;
&lt;/<b>t:select</b>&gt;
</code>
			</td>
		</tr>
		<tr>
			<th>
				選單框(多選):<br/>
				<span class="warning">(不適用於手持裝置)</span>
			</th>
			<td>
				<t:select name="select3" id="select3" size="10" multiple="multiple" style="width:150px;" dataSourceURL="drawSelect3Action"></t:select>
			</td>
			<td>
<code>&lt;<b>t:select</b> name="select3" id="select3"
    <b>size</b>="10"
    <b>multiple</b>="multiple"
    style="width:150px;"
    dataSourceURL="drawSelect3Action"&gt;
&lt;/<b>t:select</b>&gt;
</code>
			</td>
		</tr>
		<tr>
			<th>關聯式選單:</th>
			<td>
				<t:select name="select4" id="select4" style="width:150px;" dataSourceURL="drawSelect4Action" cascadeTo="select4a" isTriggerChangeOnRendered="true"></t:select>
				<br/>
				<t:select name="select4a" id="select4a" style="width:150px;" cascadeTo="select4b" cascadeFromURL="renderSelect4a" isTriggerChangeOnRendered="true">
					<option value=""> -- 請選擇 -- </option>
				</t:select>
				<br/>
				<t:select name="select4b" id="select4b" style="width:150px;" cascadeFromURL="renderSelect4b">
					<option value=""> -- 請選擇 -- </option>
				</t:select>
			</td>
			<td>
<code>&lt;<b>t:select</b> name="select4" id="select4" 
        style="width:150px;"
        dataSourceURL="drawSelect4Action"
        <b>cascadeTo</b>="select4a"
        isTtriggerChangeOnRendered="true"&gt;
&lt;/<b>t:select</b>&gt; &nbsp;
&lt;<b>t:select</b> name="select4a" id="select4a"
        style="width:150px;"
        <b>cascadeTo</b>="select4b"
        <b>cascadeFromURL</b>="renderSelect4a"
        isTriggerChangeOnRendered="true"&gt;
    &lt;option value=""&gt; -- 請選擇 -- &lt;/option&gt;
&lt;/<b>t:select</b>&gt; &nbsp;
&lt;<b>t:select</b> name="select4b" id="select4b"
        style="width:150px;"
        <b>cascadeFromURL</b>="renderSelect4b"&gt;
    &lt;option value=""&gt; -- 請選擇 -- &lt;/option&gt;
&lt;/<b>t:select</b>&gt;
</code>
			</td>
		</tr>
		<tr>
			<th>
				移動選單內項目:<br/>
				<span class="warning">(不適用於手持裝置)</span>
			</th>
			<td>
				<table>
					<tr>
						<td>
							<t:select name="select5" id="select5" size="10" style="width:6em;" dataSourceURL="renderSelect5"></t:select>
						</td>
						<td>
							<button onclick="ta.selectMoveUp('select5'); return false;" style="width:4em">↑</button>
							<br/>
							<button onclick="ta.selectMoveDown('select5'); return false;" style="width:4em">↓</button>
						</td>
					</tr>
				</table>
			</td>
			<td>
<code>&lt;table&gt;
    &lt;tr&gt;
        &lt;td&gt;
            &lt;<b>t:select</b> name="select5" id="select5"
                size="10"
                style="width:6em;"
                dataSourceURL="renderSelect5"&gt;&lt;/t:select&gt;
        &lt;/td&gt;
        &lt;td&gt;
            &lt;button onclick=
                "<b>ta.selectMoveUp</b>('select5');return false;"
                style="width:4em"&gt;↑&lt;/button&gt;
            &lt;br/&gt;
            &lt;button onclick=
                "<b>ta.selectMoveDown</b>('select5');return false;"
                style="width:4&gt;↓&lt;/button&gt;
        &lt;/td&gt;
    &lt;/tr&gt;
&lt;/table&gt;
</code>
			</td>
		</tr>
		<tr>
			<th rowspan="2">
				兩欄式選單:<br/>
				<span class="warning">(不適用於手持裝置)</span>
			</th>
			<td colspan="2">
				<table>
					<tr>
						<td>
							<t:select name="select6a" id="select6a" size="10" multiple="multiple" style="width:150px;" dataSourceURL="renderSelect6a"></t:select>
						</td>
						<td>
							<button onclick="ta.selectMove('select6a', 'select6b'); return false;" style="width:5em"> &gt; </button><br/>
							<button onclick="ta.selectMoveAll('select6a', 'select6b'); return false;" style="width:5em"> &gt; &gt; </button><br/>
							<button onclick="ta.selectMoveAll('select6b', 'select6a'); return false;" style="width:5em"> &lt; &lt; </button><br/>
							<button onclick="ta.selectMove('select6b', 'select6a'); return false;" style="width:5em"> &lt; </button>
						</td>
						<td>
							<t:select name="select6b" id="select6b" size="10" multiple="multiple" style="width:150px;"></t:select>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan="2">
<code>&lt;table&gt;
    &lt;tr&gt;
        &lt;td&gt;<div style="background-color:#f7f7f7">
            &lt;<b>t:select</b> name="select6a" id="select6a" 
                <b>size</b>="10"
                multiple="multiple"
                style="width:150px;"
                dataSourceURL="renderSelect6
            &lt;/<b>t:select</b>&gt;</div>
        &lt;/td&gt;
        &lt;td&gt;<div style="background-color:#f7f7f7">
            &lt;button onclick=
                "<b>ta.selectMove</b>('select6a','select6b'); return false;"
                style="width:5em"&gt; &amp;gt; &lt;/button&gt;
            &lt;br/&gt;
            &lt;button onclick=
                "<b>ta.selectMoveAll</b>('select6a','select6b'); return false;"
                style="width:5em"&gt; &amp;gt; &amp;gt; &lt;/button&gt;
            &lt;br/&gt;
            &lt;button onclick=
                "<b>ta.selectMoveAll</b>('select6b','select6a'); return false;"
                style="width:5em"&gt; &amp;lt; &amp;lt; &lt;/button&gt;
            &lt;br/&gt;
            &lt;button onclick=
                "<b>ta.selectMove</b>('select6b','select6a'); return false;"
                style="width:5em"&gt; &amp;lt; &lt;/button&gt;</div>
        &lt;/td&gt;
        &lt;td&gt;<div style="background-color:#f7f7f7">
            &lt;<b>t:select</b> name="select6b" id="select6b"
                <b>size</b>="10"
                <font color="red"><b>multiple</b></font>="multiple"
                style="width:150px;"&gt;
            &lt;/<b>t:select</b>&gt;</div>
        &lt;/td&gt;
    &lt;/tr&gt;
&lt;/table&gt;
&lt;!--
<b>受選項專用的選單(在本例為 select6b)一定要有 multiple 屬性，且 action 函數中在送出 request 之前一定要先將此選單予以全選(使用 ta.selectSelectAll())，本受選選單內的項目才能傳送至 server 端</b>
--&gt;
</code>
			</td>
		</tr>
	</table>
</form>


<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002021.jsp.txt" target="_blank">demo002021.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo002021.java.txt" target="_blank">Demo002021.java</a>
</div>

</body>
</html>