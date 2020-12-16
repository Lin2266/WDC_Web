<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>Grid 資料表</title>
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
function onTestClick(rowid, rowdata) {
	ta.showMessage("內部rowid=" + rowid + ", 本筆資料=" + JSON.stringify(rowdata));
}
</script>
</head>
<body>

<ol>
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
	
	<li>運作模式: 先在網頁畫出 grid 外殼, 以 Ajax 方式向 server 端取得 JSON 格式的資料來填充 grid 內容。<br/>
		新增、修改以彈出視窗方式為主, 不使用 inline 編輯方式。
		
	<li>設置 grid: <b>&lt;t:grid&gt;&lt;/t:grid&gt;</b>;<br/>
		在其內定義欄位: <b>&lt;t:column&gt;&lt;/t:column&gt;</b>;<br/>
		grid 邊緣黏上按鈕，或每列資料設置按鈕: <b>&lt;t:button&gt;&lt;/t:button&gt;</b>。
		<div class="enum">
			<b>&lt;t:grid&gt;</b> 擁有如下屬性:
			<ol>
				<li><b>id</b>: (string) grid id. 網頁將據此產生名為 <span style="color:red; font-weight:bold;">"$" + id</span> 的 <b>global JavaScript 物件</b>, 可以 JavaScript 碼呼叫此物件的 method(<a href="#gridJavascriptAPI">如下所述</a>)
				<li><b><i>bindFormId</i></b>: (string) 與 grid 每筆資料搭配的 form 之 id。<br/>form 可額外設置 &lt;input name="_rowid"/&gt; 欄位以放置該筆資料 rowid (jqGrid 內部賦予)
				<li><b><i>width</i></b>: (number 或 string, default "auto") grid 寬度(單位 px)，或指定 auto (default)使與母容器等寬
				<li><b><i>height</i></b>: (number 或 string, default "auto") grid 高度(單位 px)，筆數太多超過 height 容納量時，將產生捲動軸；指定 auto (default)者, 高度隨資料筆數而改變, 不產生捲動軸 (<span class="comment">注意: 捲動軸效果在手持裝置上的瀏覽器無法作用</span>)
				<li><b><i>defaultHeight</i></b>: (number 或 string, default 100) height 指定為 auto 時，可以此參數指定預設框高(單位 px)；當資料筆數多至超過此外框高時，外框會隨之加大
				<li><b><i>buttonColumnWidth</i></b>: (number, optional) 當每列尾含按鈕欄位時, 必須指定此屬性(單位 px)
				<li><b><i>isMultiselect</i></b>: (boolean, default false) 是否可多選
				<li><b><i>isShowRowNo</i></b>: (boolean, default false) 是否要顯示序號欄(自 1 起算)
				<li><b><i>isShowTopNav</i></b>: (boolean, default true) 是否要顯示頂部導航條
				<li><b><i>isShowBottomNav</i></b>: (boolean, default false) 是否要顯示底部導航條
				<li><b><i>isScaleColWidth</i></b>:  (boolean, default true) 是否自動按欄位寬度比例調整至滿 grid 寬度, 設為 false 者, 欄位總寬超過 grid 寬度者, 將產生水平捲動效果
				<li><b><i>isServerSidePaging</i></b>: (boolean, default false) 是否使用 server 端分頁模式。<br/>
					若是, 應只使用 <a href="${pageContext.request.contextPath}/demo003010.jsp#postToGrid" target="_blank">ta.postToGrid()</a>
					工具或本標籤的 dataSourceURL 屬性來發出 request 取 grid 內容.<br/>
					request 將自動包含二參數: _rows (每頁最多幾筆)、_page (當前分頁是第幾頁),<br/>
					server 端也應依據此二參數抛回正確的分頁內容.
				<li><b><i>rowsPerPage</i></b>: (number, default 20) 每分頁的筆數
				<li><b><i>rowsPerPageList</i></b>: (string, optional) 可選擇的每分頁的筆數(以逗號分隔), 如:「10,20,30,40」。rowsPerPageList 和 rowsPerPage 屬性不能同時存在
				<li><b><i>selectableRowFilter</i></b>: (string, optional) 自定義 JavaScript statements, 當明確 return false 時, 代表該筆不可被選取
					<ul>
						<li>需明確寫出最終的 return true/false. 或: 未明確 return false 者, 一律視為 return true
						<li>可取用 _rowid 變數, 代表被點選列的 rowid
					</ul>
				<li><b><i>dataSourceURL</i></b>: (string, optional) 指定提供 grid 內容資料的 URL
					<ul>
						<li>可指定當前畫面 backing bean 之 AJAX action name,<br/>
							或完整 URL: &#36;{pageContext.request.contextPath}/xxxxx.ajax?_action=ACTION_NAME)
						<li>本屬性值字串可含 "?a=b&c=d..." 以傳送 request 參數.
						<li>當指定此屬性時, 可再指定 isDataSourceActImmediately 決定是否在 grid 產生後立刻發出 request(預設 false)
							<div class="warning">
								註: server 端分頁(isServerSidePaging=true)時, 為使連續對空 grid 新增資料後仍能正常自動翻頁<br/>
								&nbsp; &nbsp; (尚未執行 ta.postToGrid() 查詢前)者, 宜設置屬性 dataSourceURL, 使 grid 在筆數滿頁時<br/>
								&nbsp; &nbsp; 知道要翻頁(<span style="color:red;">TODO: 空 grid 設為 datatype=local, 直至 query 後且 grid 不為空, 立刻設為 datatype=json ?</span>)
							</div>
					</ul>
				<li><b><i>dataSourceParamFormId</i></b>: (string, optional) 當指定 dataSourceURL 時, 可再以本屬性所指的 form 之欄位值為附加的 request 參數
				<li><b><i>reorderColumnIndices</i></b>: (string, optional) 重排欄位顯示順序(如: "0,4,3", 以任何非數字字元分隔)未指定到的欄位均往前靠.<br/> 
					可配合 EL 而設定(至少指定 2 個欄位 index 才會發動重排)<br/>
					server 端傳送 grid 內容時也要配合調整
				<li><b><i>onClickRow</i></b>: (string, optional)  定義用戶點擊某筆(不分選取/不選取)時，應被執行的 Javascript 敘述。敘述內可取用變數:
					<ul>
						<li>_rowid: (string) 代表該筆資料的序號(jqGrid 內部賦予 rowid)
						<li>_rowdata: (plain object) 本筆資料內容(以 &lt;t:column name="xxx"/&gt; 之 name 屬性為 key)
						<li>_status: (boolean) true 代表選取，false 代表取消選取
						<li>注意不能與 onSelectRow 同用, 不宜與 onDBClickRow 同用
					</ul>
				<li><b><i>onSelectRow</i></b>: (string, optional)  定義用戶「選取」某筆時，應被執行的 Javascript 敘述。敘述內可取用變數:
					<ul>
						<li>_rowid: (string) 代表該筆資料的序號(jqGrid 內部賦予 rowid)
						<li>_rowdata: (plain object) 本筆資料內容(以 &lt;t:column name="xxx"/&gt; 之 name 屬性為 key)
						<li>當 grid 為單選型態時，同一筆無論點幾次，都視為「選取」的動作
						<li>當 grid 為多選型態時，同一筆被「取消選取」時，此 onSelectRow 的事件處理函數將不被呼叫<br/>
							(<i>當你想在某筆被點中時觸發動作，但不分「選取」、「取消選取」時，可考慮用 onClickRow 事件處理</i>)
						<li>注意不能與 onClickRow 同用, 不宜與 onDBClickRow 同用
					</ul>
				<li><b><i>onDBClickRow</i></b>: (string, optional) 定義用戶「double-click」某筆時, 應被執行的一段 Javascript 敘述。敘述內可取用變數:
					<ul>
						<li>_rowid: (string) 代表該筆資料的序號(jqGrid 內部賦予 rowid)
						<li>_rowdata: (plain object) 本筆資料內容(以 &lt;t:column name="xxx"/&gt; 之 name 屬性為 key)
						<li>注意不宜與 onClickRow 或 onSelectRow 同用
					</ul>
				<li><b><i>onSelectAll</i></b>: (string, optional) 定義用戶按下全選 checkbox 時，應被執行的 Javascript 敘述。敘述內可取用變數:
					<ul>
						<li>_rowids: (array of string) 所有被選取的 grid rowid 組成的陣列(jqGrid 內部賦予 rowid)
						<li>_status: (boolean) true 代表全選，false 代表全取消
					</ul>
				<li><b><i>onComplete</i></b>: (string, optional) 定義當 grid 內容透過 grid API 更動, 或換頁後, 應被執行的 JavaScript 敘述。<br/>
						(<i>會觸發此 grid complete 事件的動作很多樣，應慎用此功能</i>)
			</ol>
		</div>
		<div class="enum">
			<b>&lt;t:column&gt;</b> 擁有如下屬性:
			<ol>
				<li><b>label</b>: (string) 欄位名(顯示在 grid 標頭上的)
				<li><b>name</b>: (string) 欄位代號(與 grid 搭配之 form 的輸入欄位的 name 屬性值，<i>及在 jqGrid 中的 colModal 中用來當作 name 及 index 之屬性值</i>)
				<li><b><i>width</i></b>: (number, optional) 欄位寬(單位:px)。但當 &lt;grid width="auto"/&gt; 時，欄位寬度將按相對比例伸縮
				<li><b><i>sorttype</i></b>: (string, optional) 欄位值排序法
					<ul>
						<li>未指定者不可排序
						<li>string: (string) 按字元排序 (<span class="comment">不應對中文字串排序，因 Unicode 中文排序非按筆劃排列之故</span>)
						<li>int: (string) 按整數排序
						<li>float: (string) 按浮數排序
						<li>date: (string) 按日期排序
					</ul>
				<li><b><i>cssClass</i></b>: (String, optional) 自訂每筆欄位之 CSS class；可多個，以空白隔開
				<li><b><i>aligh</i></b>: (String, default left) 內容靠左/右/中 (left/right/center)
				<li><b><i>title</i></b>: (boolean, default true) 游標置於資料欄上時，是否顯示提示文字
				<li><b><i>hidden</i></b>: (boolean, default false) 該欄位是否要在資料表畫面上顯示(欄位仍在, 只是未顯示)
			</ol>
		</div>
		<div class="enum">
			<b>&lt;t:button&gt;</b> 擁有如下<b>選擇性</b>屬性:
			<ol>
				<li><b><i>onclick</i></b>: (string) 填入處理按鈕 click 事件的 JavaScript 碼。
					<ul>
						<li>為免引號「"」和「'」造成混亂使事件處理 JavaScript 碼無法執行，最好只寫 function 呼叫式
						<li>程式碼可取用「_rowid」、「_rowdata」二物件。
							<ul>
								<li>_rowid: (string) 代表該筆資料的序號(jqGrid 內部賦予 rowid), 無受選取列則傳回 null
								<li>_rowdata: (plain object) 本筆資料內容(以 &lt;t:column name="xxx"/&gt; 之 name 屬性為 key), 無受選列者傳回空物件「{}」
								<li>_id: (string, 限列內按鈕) 資料列內按鈕實際被分配的 id
							</ul>
					</ul>
				<li><b><i>isPressThenCloseDialog</i></b>: (boolean, 預設 false) 是否按下後要關閉對話窗
				<li><b><i>isPressThenCleanDialogForm</i></b>: (boolean, 預設 false) 是否按下後要清空對話窗內的 form 欄位
				<li><b><i>isGridRowButton</i></b>: (boolean, 預設 false) 本按鈕是否為嵌於 grid 內每筆之特定儲存格內的按鈕
				<li><b><i>id</i></b>: (string) 
					<ul>
						<li>如果此按鈕位於 grid 上緣時(isGridRowButton=false), 此 id 即是本按鈕的實際 id
						<li>如果此按鈕位於每資料列末時(isGridRowButton=true), 「[id]_[資料列rowid]」才會組合成此按鈕的實際 id.<br/>
							&nbsp; &nbsp; 如: grid 第五列 rowid="5", 指定按鈕id="foo" =&gt; 本列實際按鈕id="foo_5"<br/>
							但如果未指定 id 者, 列內按鈕仍配給預設 id=[grid id]_[資料列rowid]_[按鈕index].<br/>
							&nbsp; &nbsp; 如: gridId="grid1", 第五列rowid="5" =&gt; grid1_5_0, grid1_5_1, grid1_5_2... (多顆按鈕時, 由 0 起算)
					</ul>
				<li><b><i>cssClass</i></b>: (string) CSS class 名字
				<li><b><i>style</i></b>: (string) CSS style 碼
				<li><b><i>disabled</i></b>:　(string, 值可為: disabled/true/false) 是否失效
			</ol>
		</div>
</ol>

<br/><br/>
<h4 id="gridJavascriptAPI">Client 端 JavaScript grid API</h4>
<div class="comment" style="float:right;">(定義於 main.js, 配合 jQuery + jqGrid)</div>
<div style="font-weight:lighter; font-size:90%; font-style:italic;">
	- 獨立 function: &nbsp;<a href="${pageContext.request.contextPath}/demo003010.jsp#postToGrid" target="_blank">ta.postToGrid()</a><br/>
	- grid 物件 method: &nbsp;<a href="#gridAddRow">addRow()</a>, <a href="#gridAllData">allData()</a>, <a href="#gridAllRowid">allRowid()</a>, <a href="#gridClean">clean()</a>, <a href="#gridDeleteRow">deleteRow()</a>, <a href="#gridDeselect">deselect()</a>, <a href="#gridDraw">draw()</a>, <a href="#getColModel">getColModel()</a>, <a href="#gridRecords">records()</a>, <a href="#gridResetWidth">resetWidth()</a>, <a href="#gridRowData">rowData()</a>, <a href="#gridSelect">select()</a>, <a href="#gridSelectedData">selectedData()</a>, <a href="#gridSelectedRowid">selectedRowid()</a>, <a href="#gridToForm">toForm()</a>, <a href="#gridUpdateRow">updateRow()</a>, <a href="#gridWidth">width()</a>
</div>
		<table class="desc">
			<tr>
				<th><b>grid 物件</b></th>
				<td colspan="3">例: 對 <b>id="grid1"</b> 的全域 grid object 操作其 method
<pre>JSP tag:
    &lt;t:grid id="<b>grid1</b>"&gt;
        ...
    &lt;/t:grid&gt;

JavaScript code:
    <b>$grid1</b>.clean();
    <b>$grid1</b>.draw(...);
</pre>
				</td>
			</tr>
			<tr><th></th><td></td><td></td><td></td></tr>
			<tr><th><b>method</b></th><th><b>參數</b></th><th><b>傳回值</b></th><th><b>說明</b></th></tr>
			<tr>
				<th id="gridClean"><b>clean</b>()</th>
				<td></td>
				<td>grid 物件自身</td>
				<td>清除 grid 內容</td>
			</tr>
			
			<tr>
				<th id="gridDraw"><b>draw</b>(<br/>gridData)</th>
				<td>
					<ul>
						<li><b>gridData: string/plain object/array</b> grid 資料內容
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>填入 jqGrid 資料表的內容, 格式可為下列之一:<br/>
					(<span class="comment">實際開發時使用 Java 工具 TUIGrid 來產生</span>)
					<ol>
						<li>jqGrid 工具內部預設接受的 JSON 格式
<pre>{
    "total": "xxx", <span class="comment">//查詢所得的總頁數</span>
    "page": "yyy", <span class="comment">//當前頁頁碼(自1起算)</span>
    "records": "zzz", <span class="comment">//總資料筆數</span>
    "rows": [ <span class="comment">//每一筆資料</span>
        { "id":"1", "cell":["val11","val12", ...] },
        { "id":"2", "cell":["val21","val22", ...] },
        { "id":"3", "cell":["val31","val32", ...] },
        ...
    ]
    <span class="comment">//id: jqGrid 內部配給每一筆的 rowid</span>
    <span class="comment">//cell: 每一筆的每個欄位的值</span>
}</pre>
						<li>array of plain object<br/>
							(for client 端分頁), 如:
<pre>[
   { "col1":"val11", "col2":"val12", ... },
   { "col1":"val21", "col2":"val22", ... },
   { "col1":"val31", "col2":"val32", ... },
   ...
]</pre>
						<li>只含欄位值的 array of array, 其他資訊則於 client 端繪出 grid 時動態地求值(for client 端分頁):
<pre>[
   ["val11","val12", "val13", ...],
   ["val21","val22", "val23", ...],
   ["val31","val32", "val33", ...],
   ...
]</pre>
					</ol>
				</td>
			</tr>
			<tr>
				<th id="gridSelect"><b>select</b>(<br/>rowid, <br/><i>fireEvent</i>)</th>
				<td>
					<ul>
						<li><b>rowid: string/number/array</b>
							<ol>
								<li>type=string: grid 資料項 rowid (jqGrid 內部賦予的, 但可透過 <a href="#gridSelectedRowid">$GRID.selectedRowid()</a>、<a href="#gridAllRowid">$GRID.allRowid()</a> method 得到)
								<li>type=number: grid 資料項 index (自 0 起算, 即由 grid 資料第一筆起算)
								<li>type=array: 代表多筆, 陣列成員之型態, 如上二種型態之一
							</ol>
						<li><b><i>fireEvent: boolean (optional)</i></b> 資料列被選取時, 是否觸發 jqGrid 之 setRow 事件(單筆時 default=true). <br/>
							但 rowid 參數為陣列時一律不觸發; 且當被選取列不在當前分頁時, 也不觸發
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>選取資料表內之某一筆(或多筆), 不影響既選取項.
					<div class="warning"><b>注意</b>: 在 server 端分頁模式時, 只對當前分頁有作用</div> 
					例:
<pre>$grid1.select("2"); <span class="warning">//選取 rowid="2" 之 row</span>
$grid1.select(2); <span class="warning">//選取自 0 起算第 2 筆之 row</span>
$grid1.select(["3", "4"]); <span class="warning">//選取 rowid="3","4" 之 row</span>
</pre>
				</td>
			</tr>
			<tr>
				<th id="gridDeselect"><b>deselect</b>(<br/><i>rowid</i>)</th>
				<td>
					<ul>
						<li><b><i>rowid: string/number/array (optional)</i></b>
							<ol>
		                        <li>未指定時: 全部取消選取
								<li>type=string: grid 資料項 rowid (jqGrid 內部賦予的, 可透過 <a href="#gridSelectedRowid">$GRID.selectedRowid()</a>、<a href="#gridAllRowid">$GRID.allRowid()</a> 得到)
								<li>type=number: grid 資料表內欲選取項的 index (自 0 起算)
								<li>type=array: 代表多筆, 陣列成員如上二種型態之一
							</ol>
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>取消 grid 選取(單筆或多筆), 本未選取者不動作.
					<span class="warning">server 端分頁時, 只對當前分頁有作用</span>.<br/> 
					例:
<pre>$grid1.deselect("2"); <span class="warning">//取消選取 rowid="2"</span>
$grid1.deselect(2); <span class="warning">//取消選取自 0 起算第 2 筆</span>
$grid1.deselect(["3", "4"]); <span class="warning">//取消選取 rowid="3","4"</span>
</pre>
				</td>
			</tr>
			<tr>
				<th id="getColModel"><b>getColModel</b>()</th>
				<td></td>
				<td>(plain object)<br/>grid 底層的欄位資訊</td>
				<td>(底層欄位模型)</td>
			</tr>
			<tr>
				<th id="gridRecords"><b>records</b>()</th>
				<td></td>
				<td>(number)<br/>grid 內資料筆數</td>
				<td>查 grid 總筆數</td>
			</tr>
			<tr>
				<th id="gridAddRow"><b>addRow</b>(<br/>args)</th>
				<td>
					<ul>
						<li><b>args: string/plain object/array</b>
							<ol>
								<li>type=string:<br/>視為 form id (<span class="comment">但指定 null 值者, 視為空物件 {}</span>).<br/>
									整個 form 內的欄位值當作<b>一筆</b> grid row (form input name 與 grid column name 對應)
								<li>type=plain object:<br/><b>一筆</b>row, 由「欄位名 : 欄位值」組成, 如:
<pre>{ 欄位name1 : value1,
  欄位name2 : value2, 
  ... }</pre>
								<li>type=array of plain object:<br/>一次加入<b>數筆</b> row, 每筆型態如上第 2 之所述
								<li>type=array of array:<br/>加入<b>數筆</b> row, 每筆為純由欄位值組成的 array, 組成順序要按照欄位名排列(含隱藏欄位), 如:
<pre>[ value1, value2, ... ]</pre>
							</ol>
					</ul>
				</td>
				<td>(array of string)<br/>新增的row(s) - jqGrid 內部所賦予的 rowid.<br/> &nbsp; <br/>新增失敗則傳回空陣列 []</td>
				<td>
					加入<b>一筆</b>或<b>數筆</b>資料至 grid 最後一頁最後面.<br/>
					<b><span style="color:red">**</span> <span class="error">要一次加入多筆 row 時, 
					別以迴圈反覆呼叫本 function 一次加入一筆, 不但速度較慢, 還會有難以追踪的問題！</span></b><br/>
					(可能肇因於每次迴圈都會發動至少一次 <b>reloadGrid</b> 事件, jqGrid 組件追不上迴圈的速度)
					<p/>
					例: 以 server 傳回的一筆 row 資料(JSON 格式)加入 grid 畫面(秀於 grid 最後分頁之最後)
<pre>ta.postForJSON("addAction", ..., function(ret) {
    $grid1.addRow(ret);
});</pre>
				</td>
			</tr>
			<tr>
				<th id="gridDeleteRow"><b>deleteRow</b>(<br/><i>rowid</i>)</th>
				<td>
					<ul>
						<li><b><i>rowid: string/number/array (optional)</i></b>
							<ol>
								<li>type=string: grid 資料項 rowid (jqGrid 內部賦予的, 可透過 <a href="#gridSelectedRowid">$GRID.selectedRowid()</a>、<a href="#gridAllRowid">$GRID.allRowid()</a> 得到)
								<li>type=number: grid 資料表內欲刪除 row 的 index (自 0 起算)
								<li>type=array: 代表多筆, 陣列成員如上二種型態之一
								<li>未指定參數者, 刪除 grid 當前已被選取項
							</ol>
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>移除 grid 中的一筆或數筆資料列.<p/>
					例: 在 server 端刪除一筆成功後, 再移除畫面 grid 之受選 row
<pre>ta.postForJSON("deleteAction", ..., function(ret) {
    $grid1.deleteRow();
});</pre>
				</td>
			</tr>
			<tr>
				<th id="gridUpdateRow"><b>updateRow</b>(<br/>value, <br/><i>rowid</i>)</th>
				<td>
					<ul>
						<li><b>value: string/plain object</b>
							<ol>
								<li>type=string: form id, 把 form 欄位值置入與 grid 內同 name 的欄位(未指定或為 null 者, 視為空 {})
								<li>type=plain object: 代表欲更動 grid 某筆資料的新值. values 內之 key 有符合 grid 之欄位名者, key 對應的值取以為 grid 之指定資料欄位的新值
							</ol>
						<li><b><i>rowid: string/number (optional)</i></b>
							<ol>
								<li>type=string: grid 資料項 rowid (jqGrid 內部賦予, 可透過 <a href="#gridSelectedRowid">$GRID.selectedRowid()</a>、<a href="#gridAllRowid">$GRID.allRowid()</a> 得到)
								<li>type=number: grid 內被選取項的 index (自 0 起算)
								<li>未指定者, 取代 grid 當前已被選取的單筆資料
							</ol>
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>更動 grid 某一筆資料的欄位值. 
					<br>例: 以 server 端傳回的 row 資料(JSON格式)更新畫面 grid 當前選取項
<pre>ta.postForJSON("updateAction", ..., function(ret) {
    $grid1.updateRow(ret);
});</pre>
				</td>
			</tr>
			<tr>
				<th id="gridAllRowid"><b>allRowid</b>()</th>
				<td></td>
				<td>(string[])<br/>jqGrid 內部 rowid 組成的字串陣列</td>
				<td>取 grid 所有資料之 rowid (jqGrid 內部賦予)組成的陣列</td>
			</tr>
			<tr>
				<th id="gridSelectedRowid"><b>selectedRowid</b>()</th>
				<td></td>
				<td>(string[])<br/>grid 所有資料之 rowid (jqGrid 內部賦予)</td>
				<td>取 grid 受選取項之 rowid (jqGrid 內部賦予)陣列</td>
			</tr>
			<tr>
				<th id="gridSelectedData"><b>selectedData</b>()</th>
				<td></td>
				<td>(array of plain object)<br/>受選 row 資料所組成的陣列</td>
				<td>取 grid 受選 rows. 例: 
<pre><span class="warning">//把 grid (<span class="comment">id="grid1"</span>) 被勾選資料(array of plain object), 
//化為字串(用 JSON.stringify())
//顯示出來(用 ta.showMessage())</span>
ta.showMessage(
    JSON.stringify(
        $grid1.selectedData()));</pre>
				</td>
			</tr>
			<tr>
				<th id="gridRowData"><b>rowData</b>(<br/>rowid)</th>
				<td>
					<ul>
						<li><b>rowid: string/number</b> 
							<ol>
								<li>type=string: grid 資料項 rowid (jqGrid 內部賦予的, 可透過 <a href="#gridSelectedRowid">$GRID.selectedRowid()</a>, <a href="#gridAllRowid">$GRID.allRowid()</a> 得到)
								<li>type=number: grid 受指定項 index (自 0 起算)
							</ol>
					</ul>
				</td>
				<td>(plain object)<br/>每筆資料內容, 由「欄位名: 值」組成的 plain object.<p/>取不到值時, 傳回空 {}</td>
				<td>取 grid 被選定的該筆的內容. 例: 
<pre><span class="warning">//把指定 rowid 的單筆資料(plain object), 
//化為字串(使用 JSON.stringify())
//顯示出來(使用 ta.showMessage())</span>
ta.showMessage(
    JSON.stringify(
        $grid1.rowData("xx05")));</pre>
				</td>
			</tr>
			<tr>
				<th id="gridAllData"><b>allData</b>()</th>
				<td></td>
				<td>(array of plain object)<br/>每筆資料組成的陣列</td>
				<td>取 grid 資料內容.<br/>
					例:
<pre><span class="warning">//把 grid 全部資料(array of plain object), 
//化為字串(用 JSON.stringify())
//顯示出來(用 ta.showMessage())</span>
ta.showMessage(
    JSON.stringify(
        $grid1.allData()));</pre>
				</td>
			</tr>
			<tr>
				<th id="gridToForm"><b>toForm</b>(<br/>rowid, <br/>formId)</th>
				<td>
					<ul>
						<li><b>rowid: string/number</b>
							<ol>
								<li>type=string: grid 指定資料項 rowid (jqGrid 內部賦予的, 可透過 <a href="#gridSelectedRowid">$GRID.selectedRowid()</a>, <a href="#gridAllRowid">$GRID.allRowid()</a> 得到)
								<li>type=number: grid 指定項 index (自 0 起算)
							</ol>
						<li><b>formId: string</b> form id
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>把 grid 指定的 row 資料置入 form 同 name 的欄位中.
					<div class="comment">
						<ul>
							<li>本函數會在資料中額外埋入名為 _rowid 的參數值, <br/>form 可以埋入一個 name="_rowid" 的欄位承接此值, 以標示本筆在 grid 的內部 rowid
							<li>form 之既有欄位值不會被清掉, 如希望 form 只存在 grid 的欄位值, 在執行 $GRID.toForm() 前, 應先執行 <a href="${pageContext.request.contextPath}/demo003010.jsp#formClean" target="_blank">ta.formClean()</a>
						</ul>
					</div>
				</td>
			</tr>
			<tr>
				<th id="gridWidth"><b>width</b>(<br/><i>width</i>)</th>
				<td>
					<ul>
						<li><b><i>width: number (optional)</i></b> grid 寬度之新值(單位: px)
					</ul>
				</td>
				<td>grid 物件自身(用於設值時).<p/>或 <p/>(number)grid 寬度值(用於取值時, 單位 px)</td>
				<td>取 grid 寬度值或為其設定寬度</td>
			</tr>
			<tr>
				<th id="gridResetWidth"><b>resetWidth</b>(<br/><i>isOnGridinit</i>)</th>
				<td>
					<ul>
						<li><b><i>isOnGridinit: boolean (optional)</i></b> 是否在 grid 繪製完成時就立刻重設寬度 (default: false)
					</ul>
				</td>
				<td>grid 物件自身</td>
				<td>重設 grid 寬度, 使接近 grid 所在的容器的寬度.</td>
			</tr>
		</table>

<br/><br/>
<h4>使用 Grid:</h4>
<t:tabs id="gridModeTabs">
	<t:tab label="Client端分頁" id="tbGrid1">
		<h5>JSP:</h5>
		例:單選
		<code>&lt;<b>t:grid</b> id="grid1" rowsPerPage="5" dataSourceURL="preLoadGridAction?param=aaa"&gt;
    &lt;<b>t:column</b> name="col1" label="欄位一" sorttype="int"/&gt;
    &lt;<b>t:column</b> name="col2" label="欄位二" sorttype="string"/&gt;
    
    &lt;<b>t:button</b> onclick="onTestClick(_rowid, _rowdata)"&gt;被選取項&lt;/<b>t:button</b>&gt;
&lt;/<b>t:grid</b>&gt;
</code>

		<t:grid id="grid1" rowsPerPage="5" dataSourceURL="preLoadGridAction?param=aaa">
			<t:column name="col1" label="欄位一" sorttype="int"/>
			<t:column name="col2" label="欄位二" sorttype="string"/>
			<t:button onclick="onTestClick(_rowid, _rowdata)">被選取項</t:button>
		</t:grid>

		<br/>
		例:多選<br/>
		&nbsp; &nbsp; &nbsp; 注意全部未選取，與多選時的按鈕行為.<br/>
		&nbsp; &nbsp; &nbsp; <span class="warning">且 server 端分頁模式時, 當前頁被選取項資訊, 至翻到下頁再回來當前頁時, 即告丢失.</span>
		<code>&lt;<b>t:grid</b> id="grid2" rowsPerPage="5" dataSourceURL="preLoadGridAction?param=aaa"
        isMultiselect="true"
        onSelectRow="onselectrow(_rowid, _rowdata);"&gt;
    &lt;<b>t:column</b> name="col1" label="欄位一" sorttype="int"/&gt;
    &lt;<b>t:column</b> name="col2" label="欄位二" sorttype="string"/&gt;
    &lt;<b>t:button</b> onclick="onTestClick(_rowid, _rowdata)"&gt;測試&lt;/<b>t:button</b>&gt;
&lt;/<b>t:grid</b>&gt;
</code>

		<t:grid id="grid2" rowsPerPage="5" dataSourceURL="preLoadGridAction?param=aaa" isMultiselect="true" 
				onSelectRow="onselectrow(_rowid, _rowdata);">
			<t:column name="col1" label="欄位一" sorttype="int"/>
			<t:column name="col2" label="欄位二" sorttype="string"/>
			<t:button onclick="onTestClick(_rowid, _rowdata)">測試</t:button>
		</t:grid>
		<script>
		function onselectrow(rowid, rowdata) {
			ta.showMessage("「選」中了: rowid=" + rowid + ", rowdata=" + JSON.stringify(rowdata));
		}
		</script>
		
		<br/>
		例: 欄位自動按比例伸縮(預設行為)<br/>
		&nbsp; &nbsp; &nbsp; 欄位有<b>指定寬度</b>(單位:px)時，在 grid 生成階段，使欄位總寬剛好塞滿 grid 寬，不出現水平捲動軸.<br/>
		&nbsp; &nbsp; &nbsp; <b>欄位數不多</b>或<b>總寬不會過長</b>的情形下適用.
		<code>&lt;t:grid id="grid3" dataSourceURL="preLoadGrid2Action" 
        defaultHeight="100" rowsPerPageList="5,10"&gt;
    &lt;t:column name="col1" label="欄位一" sorttype="string" <b>width</b>="100"&gt;
    &lt;t:column name="col2" label="欄位二" sorttype="string" <b>width</b>="100"&gt;
    &lt;t:column name="col3" label="欄位三" sorttype="string" <b>width</b>="200"&gt;
    &lt;t:column name="col4" label="欄位四" sorttype="string" <b>width</b>="200"&gt;
    &lt;t:column name="col5" label="欄位五" sorttype="string" <b>width</b>="200"&gt;
    &lt;t:column name="col6" label="欄位六" sorttype="string" <b>width</b>="400"&gt;
&lt;/t:grid&gt;
</code>
		<t:grid id="grid3" dataSourceURL="preLoadGrid2Action" defaultHeight="100" rowsPerPageList="5,10">
			<t:column name="col1" label="欄位一" sorttype="string" width="100"/>
			<t:column name="col2" label="欄位二" sorttype="string" width="100"/>
			<t:column name="col3" label="欄位三" sorttype="string" width="200"/>
			<t:column name="col4" label="欄位四" sorttype="string" width="200"/>
			<t:column name="col5" label="欄位五" sorttype="string" width="200"/>
			<t:column name="col6" label="欄位六" sorttype="string" width="400"/>
		</t:grid>
		
		<br/>
		例: 欄寬固定  &nbsp;<span style="color:red;">(TODO: 目前版面無法隨視窗寬度伸縮, 待修)</span><br/>
		&nbsp; &nbsp; &nbsp; <b>欄位數很多</b>或<b>總寬很長時</b>適用，grid 將出現水平捲軸.
		<code>&lt;t:grid id="grid4" <b>isScaleColWidth="false"</b> 
        dataSourceURL="preLoadGrid2Action" defaultHeight="100" rowsPerPageList="5,10"&gt;
    &lt;t:column name="col1" label="欄位一" sorttype="string" <b>width</b>="100"&gt;
    &lt;t:column name="col2" label="欄位二" sorttype="string" <b>width</b>="100"&gt;
    &lt;t:column name="col3" label="欄位三" sorttype="string" <b>width</b>="200"&gt;
    &lt;t:column name="col4" label="欄位四" sorttype="string" <b>width</b>="200"&gt;
    &lt;t:column name="col5" label="欄位五" sorttype="string" <b>width</b>="200"&gt;
    &lt;t:column name="col6" label="欄位六" sorttype="string" <b>width</b>="400"&gt;
&lt;/t:grid&gt;
</code>
		<t:grid id="grid4" isScaleColWidth="false" dataSourceURL="preLoadGrid2Action" defaultHeight="100" rowsPerPageList="5,10">
			<t:column name="col1" label="欄位一" sorttype="string" width="100"/>
			<t:column name="col2" label="欄位二" sorttype="string" width="100"/>
			<t:column name="col3" label="欄位三" sorttype="string" width="200"/>
			<t:column name="col4" label="欄位四" sorttype="string" width="200"/>
			<t:column name="col5" label="欄位五" sorttype="string" width="200"/>
			<t:column name="col6" label="欄位六" sorttype="string" width="400"/>
		</t:grid>

		<br/>
		<h5>Backing bean:</h5>
		使用 TUIGridView 工具，讀入資料 list，輸出 JSON 字串給前端填入 grid。<br/>
		以上 JSP grid 標籤皆用下列資料來源例:
<code>import com.tatung.commons.web.widget.TUIGridColumnsHandler;
import com.tatung.commons.web.widget.TUIGridView;
...
List&lt;SomeType&gt; data = ...; //由查詢得來的 list of VO

JSONObject grid =
    new <b>TUIGridView</b>&lt;SomeType&gt;(data).items(new <b>TUIGridColumnsHandler</b>&lt;SomeType&gt;() {
        @Override
        public Object[] generateColumns(int rowIndex, SomeType bean, String rowId)
                throws Throwable {
            Object[] cols = new Object[3]; //grid 有多少資料欄位，就有多少陣列成員
            cols[0] = bean.getCol1();
            cols[1] = bean.getCol2();
            cols[2] = bean.getCol3();
            return cols;
        }
    });
//由 Eclipse 幫助產生 TUIGridColumnsHandler callback function
//得出的 grid 字串傳回前端，交給前端 JavaScript 碼處理
</code>
	</t:tab>
	
	<t:tab label="Server端分頁" id="tbGrid2">
		<h5>JSP:</h5>
		grid tag 與 client 端分頁的選項幾乎相同, 只需再加 isServerSidePaging="true" 屬性
		<t:grid id="grid5" dataSourceURL="preLoadGrid5Action" defaultHeight="100" rowsPerPageList="5,10" isServerSidePaging="true">
			<t:column name="col1" label="欄位一" sorttype="string" width="100"/>
			<t:column name="col2" label="欄位二" sorttype="string" width="100"/>
			<t:column name="col3" label="欄位三" sorttype="string" width="200"/>
			<t:column name="col4" label="欄位四" sorttype="string" width="200"/>
			<t:column name="col5" label="欄位五" sorttype="string" width="200"/>
			<t:column name="col6" label="欄位六" sorttype="string" width="400"/>
		</t:grid>
		<code>JSP:
  &lt;t:grid id="grid5" dataSourceURL="preLoadGrid5Action" defaultHeight="100" rowsPerPageList="5,10"
          <b>isServerSidePaging="true"</b>&gt;
      &lt;t:column name="col1" label="欄位一" sorttype="string" <b>width</b>="100"&gt;
      &lt;t:column name="col2" label="欄位二" sorttype="string" <b>width</b>="100"&gt;
      &lt;t:column name="col3" label="欄位三" sorttype="string" <b>width</b>="200"&gt;
      &lt;t:column name="col4" label="欄位四" sorttype="string" <b>width</b>="200"&gt;
      &lt;t:column name="col5" label="欄位五" sorttype="string" <b>width</b>="200"&gt;
      &lt;t:column name="col6" label="欄位六" sorttype="string" <b>width</b>="400"&gt;
  &lt;/t:grid&gt;
</code>

		<h5>Backing bean:</h5>
	</t:tab>
</t:tabs>



<div class="footer">
	<hr/>
	原始碼: <a href="${pageContext.request.contextPath}/sourceview/web/demo002020.jsp.txt" target="_blank">demo002020.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo002020.java.txt" target="_blank">Demo002020.java</a>
</div>

</body>
</html>