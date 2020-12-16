<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>單表增、刪、修、查例</title>
<script type="text/javascript">
function init() {
	document.form1.COL1.focus();
}
function queryAction() {
	ta.postToGrid("queryAction", "form1", "grid1");
	//或寫成:
	//ta.postToGrid({
	//		url: "queryAction",
	//		args: "form1",
	//		gridId: "grid1"
	//});
}
function cleanAction() {
	ta.formClean("form1");
	ta.formClean("fmEdit");
	ta.formClean("fmAdd");
	$grid1.clean();
}
function viewDetailAction(rowid, rowdata) {
	if(rowid == null) //多筆被選取時, rowid 為 null
		return false;
	ta.mapToForm(rowdata, "fmView"); //先把　grid 之該筆內容倒入　form 中
	$dlgView.open();
	ta.postForJSON("queryDetailAction", { pk: rowdata.pk }, function(ret) { //如果 dialog 內有 grid 所未包含的資料, 要從 server 查詢(此例中以 pk 當查詢的條件)後再塞進 dialog
		ta.mapToForm(ret, "fmView");
	});
}
function editAction(rowid, rowdata) {
	if(rowid == null)
		return false;
	ta.mapToForm(rowdata, "fmEdit"); //或者在 ＜t:table bindFormId="fmEdit"／＞ 設置 bindFormId 屬性, 所以按下按鈕時已自動把該筆資料置入 form(id=fmEdit), 就不需人工放進 form
	ta.value(document.fmEdit.col5, (typeof(rowdata.col5) == "string") ? rowdata.col5.split(",") : rowdata.col5); //多值欄位得自行把 value 拆成陣列, 以便對 checkbox 群組設值
	ta.value(document.fmEdit.col7, (typeof(rowdata.col7) == "string") ? rowdata.col7.split(",") : rowdata.col7); //多值欄位得自行把 value 拆成陣列, 以便對多選選單設值
	$dlgEdit.open();
}
function editSubmitAction() {
	if(document.fmEdit.pk.value == "")
		return false;
	ta.postForJSON("updateAction", "fmEdit", function(ret) {
		$grid1.updateRow(ret); //不指定 grid 內部 rowid 者, 代表更新當前被選取的那一筆
		$dlgEdit.close();
	});
}
function deleteAction(rowid, rowdata) {
	if(rowid == null || !confirm("確定刪除？"))
		return false;
	ta.postForJSON("deleteAction", { pk: rowdata.pk }, function(ret) {
		$grid1.deleteRow(rowid);
	});
	//或寫成如下型式:
	//ta.postForJSON({
	//		url: "deleteAction",
	//		args: { pk: rowdata.pk },
	//		onSuccessful: function(ret) {
	//			$grid1.deleteRow(rowid);
	//		}
	//});
}
function addAction() {
	$dlgAdd.open();
	//ta.formClean("fmAdd"); //清除對話框各欄位值
}
function addSubmitAction() {
	ta.postForJSON("addAction", "fmAdd", function(ret) {
		$grid1.addRow(ret); //把 server 傳來的欄位值塞到 grid 最後面
		$dlgAdd.close();
	});
}
function filterGrid1(rowid) {
	var rowdata = $grid1.rowData(rowid);
	//return !(Number(rowdata.COL1) > 5);
	return !(Number(rowid) > 7);
}
function directOutputAction() {
	ta.submit("directOutputAction");
}
</script>
</head>
<body onload="init();">

<div class="buttons">
	<button onclick="directOutputAction(); return false;">測直接輸出內容</button>
	<button onclick="queryAction();return false;">查詢</button> &nbsp;
	<button onclick="cleanAction();return false;">清除</button> &nbsp;
</div>

<form name="form1" id="form1" method="post">
	<table class="formbody">
		<!-- <caption><div>查詢條件：</div></caption> -->
		<tr>
			<th width="10%">欄位一：</th>
			<td  width="40%" nowrap="nowrap">
				<input name="COL1" type="text"/>
				<span class="comment">(數字)</span>
			</td>
			<th width="10%">欄位二：</th>
			<td width="40%">
				<t:input name="col2" type="text" placeholder="(輸入一般文字)"/>
			</td>
		</tr>
		<tr>
			<th>欄位三：</th>
			<td nowrap>
				<t:input type="date" name="col3"/>
			</td>
			<th>欄位四：</th>
			<td>
				<input name="col4" type="text"/>
			</td>
		</tr>
		<tr>
			<th>欄位五：</th>
			<td>
				<t:input type="checkbox" name="col5" value="a" label="值a" checked="checked"/> &nbsp;
				<t:input type="checkbox" name="col5" value="b" label="值b"/> &nbsp;
				<t:input type="checkbox" name="col5" value="c" label="值c"/> &nbsp;
				<t:input type="checkbox" name="col5" value="d" label="值d"/> &nbsp;
			</td>
			<th>欄位六：</th>
			<td>
				<t:input type="radio" name="col6" value="x" label="值x" checked="checked"/> &nbsp;
				<t:input type="radio" name="col6" value="y" label="值y"/> &nbsp;
				<t:input type="radio" name="col6" value="z" label="值z"/> &nbsp;
			</td>
		</tr>
		<tr>
			<th>欄位七：</th>
			<td nowrap="nowrap" colspan="4">
				<select name="col7" id="col7a" multiple="multiple" size="4">
					<option value="1">一</option>
					<option value="2">二</option>
					<option value="3">三</option>
					<option value="4">四</option>
				</select>
				<span class="comment">(可多選)</span>
			</td>
		</tr>
	</table>
</form>

<t:grid id="grid1" defaultHeight="200" isShowRowNo="true" isMultiselect="true" rowsPerPageList="10,20,30" selectableRowFilter="return filterGrid1(_rowid);">
	<t:column label="" name="pk" hidden="true"/>
	
	<t:column label="欄位一" name="COL1" width="100" sorttype="int"/>
	<t:column label="欄位二" name="col2" width="200" sorttype="string"/>
	<t:column label="欄位三" name="col3" width="200" sorttype="string"/>
	<t:column label="欄位四" name="col4" width="200" sorttype="string"/>
	<t:column label="欄位五" name="col5" width="200" sorttype="string"/>
	<t:column label="欄位六" name="col6" width="200" sorttype="string"/>
	<t:column label="欄位七" name="col7" width="200" sorttype="string"/>

	<t:button onclick="addAction()">新增</t:button>	
	<t:button id="btnG1" onclick="viewDetailAction(_rowid, _rowdata)">內容</t:button>
	<t:button id="btnG2" onclick="editAction(_rowid, _rowdata)">修改</t:button>
	<t:button id="btnG3" onclick="deleteAction(_rowid, _rowdata)">刪除</t:button>
	<t:button onclick="alert(JSON.stringify($grid1.selectedData()))">已選data</t:button>
	<t:button onclick="alert(decodeURIComponent(ta.queryString({ gridData:$grid1.allData() })))">allData</t:button>
</t:grid>

<!-- for view detail -->
<t:dialog id="dlgView" width="600">
	<form name="fmView" id="fmView">
		<input type="hidden" name="pk"/><%-- key for update/delete --%>
		
		<table class="formbody">
			<tr><th>欄位一：</th><td><input type="text" name="COL1" readonly/></td></tr>
			<tr><th>欄位二：</th><td><input type="text" name="col2" readonly/></td></tr>
			<tr><th>欄位三：</th>	<td><input type="text" name="col3" readonly/></td></tr>
			<tr><th>欄位四：</th><td><input type="text" name="col4" readonly/></td></tr>
			<tr><th>欄位五：</th><td><input type="text" name="col5" readonly/></td></tr>
			<tr><th>欄位六：</th><td><input type="text" name="col6" readonly/></td></tr>
			<tr><th>欄位七：</th><td><input type="text" name="col7" readonly/></td></tr>
			<tr><th colspan="2">本筆其他細目</th></tr>
			<tr><th>欄位A：</th><td><input type="text" name="cola" readonly/></td></tr>
			<tr><th>欄位B：</th><td><input type="text" name="colb" readonly/></td></tr>
			<tr><th>欄位C：</th><td><input type="text" name="colc" readonly/></td></tr>
		</table>
	</form>
	<t:button isPressThenCloseDialog="true">關閉</t:button>
</t:dialog>

<!-- for edit -->
<t:dialog id="dlgEdit" width="600">
	<form name="fmEdit" id="fmEdit">
		<input type="hidden" name="pk"/><%-- key for update/delete --%>
		
		<table class="formbody">
			<tr>
				<th>欄位一：</th>
				<td>
					<input type="text" name="COL1" placeholder="(整數)"/>
				</td>
			</tr>
			<tr>
				<th>欄位二：</th>
				<td>
					<input type="text" name="col2" placeholder="(字串)"/>
				</td>
			</tr>
			<tr>
				<th>欄位三：</th>
				<td nowrap>
					<t:input type="date" name="col3" placeholder="(yyyy-mm-dd)"/>
				</td>
			</tr>
			<tr>
				<th>欄位四：</th>
				<td>
					<input type="text" name="col4" placeholder="(字串)"/>
				</td>
			</tr>
			<tr>
				<th>欄位五：</th>
				<td>
					<t:input type="checkbox" name="col5" value="a" label="值a"/> &nbsp;
					<t:input type="checkbox" name="col5" value="b" label="值b"/> &nbsp;
					<t:input type="checkbox" name="col5" value="c" label="值c"/> &nbsp;
					<t:input type="checkbox" name="col5" value="d" label="值d"/> &nbsp;
				</td>
			</tr>
			<tr>
				<th>欄位六：</th>
				<td>
					<t:input type="radio" name="col6" value="x" label="值x"/> &nbsp;
					<t:input type="radio" name="col6" value="y" label="值y"/> &nbsp;
					<t:input type="radio" name="col6" value="z" label="值z"/> &nbsp;
				</td>
			</tr>
			<tr>
				<th>欄位七：</th>
				<td nowrap="nowrap" colspan="4">
					<select name="col7" id="col7b" multiple="multiple" size="4">
						<option value="1">一</option>
						<option value="2">二</option>
						<option value="3">三</option>
						<option value="4">四</option>
					</select>
				</td>
			</tr>
		</table>
	</form>
	<t:button onclick="editSubmitAction()">確定</t:button>
	<t:button isPressThenCleanDialogForm="true">清除</t:button>
	<t:button isPressThenCloseDialog="true">關閉</t:button>
</t:dialog>

<!-- for add -->
<t:dialog id="dlgAdd" isCleanFormOnOpen="true">
	<form name="fmAdd" id="fmAdd">
		<table class="formbody">
			<tr>
				<th>欄位一：</th>
				<td>
					<input type="text" name="COL1" placeholder="(整數)"/>
				</td>
			</tr>
			<tr>
				<th>欄位二：</th>
				<td>
					<input type="text" name="col2" placeholder="(字串)"/>
				</td>
			</tr>
			<tr>
				<th>欄位三：</th>
				<td nowrap>
					<t:input type="date" name="col3" placeholder="(yyyy-mm-dd)"/>
				</td>
			</tr>
			<tr>
				<th>欄位四：</th>
				<td>
					<input type="text" name="col4" placeholder="(字串)"/>
				</td>
			</tr>
		</table>
	</form>
	<t:button onclick="addSubmitAction()">確定</t:button>
	<t:button isPressThenCleanDialogForm="true">清除</t:button>
	<t:button isPressThenCloseDialog="true">關閉</t:button>
</t:dialog>



<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo001000.jsp.txt" target="_blank">demo001000.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo001000.java.txt" target="_blank">Demo001000.java</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/model/vo/Test1.java.txt" target="_blank">Test1.java (VO)</a>
	&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/model/service/DemoDataService.java.txt" target="_blank">DemoDataService.java (Service)</a>
</div>

</body>
</html>
