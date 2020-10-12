<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/ErrPage.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="ta" uri="commons.tatung.com" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.io.*" %>
<%@ page import="tools.StrUtil" %>
<jsp:useBean id="catId" scope="request" class="dcstools.cherry.AllTab"/>
<jsp:useBean id="cat2Id" scope="request" class="dcstools.cherry.AllTab"/>
<%
   final String context_path = request.getContextPath();
   final jdcs3c000.jdcs3c000Bean b = (request.getAttribute("list") == null) ? new jdcs3c000.jdcs3c000Bean() : (jdcs3c000.jdcs3c000Bean)request.getAttribute("list");
   final String tmpMSG = StrUtil.print(request.getAttribute("tmpMSG"));
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>整卷歸檔</TITLE>
<LINK REL="stylesheet" HREF="${pageContext.request.contextPath}/css/prg.css" TYPE="text/css">

<script>var contextPath = "${pageContext.request.contextPath}";</script>
<script src="${pageContext.request.contextPath}/js/min/jquery.js"></script>
<script src="${pageContext.request.contextPath}/js/min/jquery.form.js"></script>
<script src="${pageContext.request.contextPath}/js/main.js"></script>
<SCRIPT>
function jdcs3c000_print(){
  if(document.form1.filename.value==""){
    alert("無資料可供列印");
    return;
  }
  document.form1.action="${pageContext.request.contextPath}/tmp/"+document.form1.filename.value;
  document.form1.submit();
}
function jdcs3c000_chg(){
	document.form1.select1.length= 0;
	document.form1.select2.length= 0;
	document.form1.batno.value= '';
	document.form1.subj.value= '';
<%if(b.get("mod").equals("0")||b.get("mod").equals("1")){%>
	document.form1.cat2.value= '';
<%}%>
	document.form1.clan.value= '';
	document.form1.sno.value= '';
	document.form1.exsubj.value= '';
}
function jdcs3c000_clear(){
  jdcs3c000_chg();
  document.form1.cat.value= '';
}
function jdcs3c000_sno_search(){
	var sno = document.form1.sno.value;
	if(sno==""){ alert("請先輸入文號"); return;}

	ta.postForJSON("${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_06T?action=QUERY_TO_FILE_DATA", 
		{ sno: sno }, 
		function(ret) {
			if(ret.count == -1){
				alert("請先輸入文號"); return;
			}else if(ret.count == 0){
				alert("查無相關資料"); return;
			}else{
				if(ret.batno == ""){
					ta.submit("${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_01T", {isOneSno: "Y", cat: ret.cat, sno: sno});
				}else{
					ta.submit("${pageContext.request.contextPath}/jdcs3c000.jdcs3c000_04T.do?action=query", {isOneSno: "Y", cat: ret.cat, batno: ret.batno, sno: sno});
				}
			}
		});
}
function jdcs3c000_search(v){
  if(v==1){//新增查詢
    if(document.form1.cat.value==""){ alert("請先點選分類號"); return;}
    jdcs3c000_chg();
    document.form1.msg.value="\"新增查詢\" 處理中，請稍後...";
    document.form1.action="${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_01T";
    document.form1.submit();
  }else if(v==2){//修改查詢
    if(document.form1.cat.value==""&& document.form1.batno.value==""&&
    		document.form1.subj.value==""){ alert("請至少輸入一個查詢條件");return;}
    document.form1.msg.value="\"修改查詢\" 處理中，請稍後...";
    document.form1.refg.value="";
    ta.submit("${pageContext.request.contextPath}/jdcs3c000.jdcs3c000_04T.do?action=query", "form1");
//    document.form1.action="${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_04T";
//    document.form1.submit();
  }else if(v==3){//退件未收
	document.form1.msg.value="\"退件未收查詢\" 處理中，請稍後...";
	document.form1.refg.value="1";
	ta.submit("${pageContext.request.contextPath}/jdcs3c000.jdcs3c000_04T.do?action=query", "form1");
//	document.form1.action="${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_04T";
//	document.form1.submit();
  }else if(v==4){//未歸檔清單
	  document.form1.msg.value="\"未歸檔清單\" 報表準備中，請稍後...";
    document.form1.action="${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_05T";
    document.form1.submit();
  }else alert("處理代號錯誤，無法處理");
}
function jdcs3c000_confirm(){
  if((document.form1.cat.value=="")){ alert("請先點選分類號"); return;}
  if((document.form1.subj.value=="")){ alert("請輸入案名"); return;}
  if((document.form1.clan.value=="")){ alert("請輸入宗數"); return;}
  if(isNaN(parseInt(document.form1.clan.value, 10))) { alert("宗數欄位須為半形數字!"); return; }
  if((document.form1.exsubj.value=="")) document.form1.exsubj.value=document.form1.subj.value;
   //var str = document.form1.subj.value;
   //str = str.replace(/&#22301;/g,"&#59018;");
   //str = str.replace(/&#32137;/g,"&#58621;");
   //str = str.replace(/&#20227;/g,"&#58154;");
   //str = str.replace(/&#24419;/g,"&#58198;");
   //str = str.replace(/&#22531;/g,"&#58421;");
   //str = str.replace(/&#28913;/g,"&#58448;");
   //str = str.replace(/&#33747;/g,"&#58555;");
   //document.form1.subj.value=str;

  if(document.form1.select1.options.length+document.form1.select2.options.length==0){
    alert("無資料可供確認，請先作查詢");return;
  }
  if(document.form1.mod.value=='0' && document.form1.select2.options.length==0){
    alert("請點選欲新增之資料");return;
  }
  if(document.form1.mod.value=='1' && document.form1.select2.options.length==0){
    if(!confirm("是否要刪除此筆資料？")) return;
  }
  if(document.form1.cat2.value!='' && document.form1.select2.options.length>=0){
    if(!confirm("是否要修改分類號？")) return;
  }
  
  var listid2=document.form1.listid2;
  var list2=document.form1.list2;
  listid2.value="";list2.value="";
  var e = document.form1.select2;
  document.form1.listcnt2.value=e.options.length;
  if (e != null){
    for (i=0; i<e.options.length; i++){
      listid2.value+=e.options(i).value+"$";
      list2.value+=e.options(i).text+"$";
    }
  }

  //var listid1=document.form1.listid1;
  //var list1=document.form1.list1;
  //listid1.value="";list1.value="";
  e = document.form1.select1;
  document.form1.listcnt1.value=e.options.length;
  //if (e != null){
  //  for (i=0; i<e.options.length; i++){
  //    listid1.value+=e.options(i).value+"$";
  //    list1.value+=e.options(i).text+"$";
  //  }
  //}
  
  document.form1.msg.value="<資料庫更新>處理中，請稍後...";
  document.form1.action="${pageContext.request.contextPath}/servlet/jdcs3c000.jdcs3c000_02T";
  document.form1.submit();
}

function selectMove(id1, id2){
	ta.selectMove(id1, id2)
}

function selectMoveAll(id1, id2){
	ta.selectMoveAll(id1, id2)
}
</SCRIPT>
</HEAD>
<BODY>
<FORM method="post" name="form1" id="form1">

<div style="text-align:center;">
	<img src="${pageContext.request.contextPath}/images/title3c000.gif">
</div>

<table class="formtab" style="width:900px; margin-top:5px;">
	<tr><td width="8%"></td><td width="16%"></td><td width="8%"></td><td width="16%"></td><td width="8%"></td><td></td></tr>
	<tr>
		<th>分類號：</th>
		<td>
			<select name="cat" id="cat" onchange="jdcs3c000_chg()" style="width:10em;">
				<option value=""></option>
<%
	String userdb= (String) session.getAttribute("UserDB");
	String userid= (String) session.getAttribute("UserID");
	String sql="select distinct tf_cat,ct_memo " +
			" from dcs0_to_file " + 
			" left outer join dcs0_cat on tf_cat=ct_cat " +
			" where tf_psid='" + userid + "'" +
			" and tf_date is null " +
			"union " +
			"select distinct bm_cat,ct_memo " +
			" from dcs3_bat_mst " +
			" left outer join dcs0_cat on bm_cat=ct_cat " +
			" where bm_psid='" + userid+"'" +
			" order by 1";
	catId.Procit(dcstools.ConnPool.getInstance(),userdb,sql,2);
	for (int i=0; i< catId.getListno(); i++) {
		String cat, cat_name;
		cat = catId.getCol(0,i);
		cat_name = catId.getCol(0,i) + " " + catId.getCol(1, i);
%>
				<option value="<%=cat%>" <%=(b.get("cat").equals(cat)?"selected":"")%>><%=cat_name%></option>
<%	} %>
			</select>
		</td>
		<th>批次號：</th>
		<td>
			<input type="text" name="batno" style="width:10em;" value='<%=b.get("batno")%>' onchange="jdcs3c000_chg()"/>
		</td>
		<th>案名：</th>
		<td>
			<input type="text" name="subj" maxlength="100" style="width:10em;"" value='<%=b.get("subj")%>'/>
			<span style="font-size:smaller; color:red;">*案名限制100個字元數。</span>
		</td>
	</tr>
	<tr>
		<th>宗數：</th>
		<td>
			<input type="text" name="clan" style="width:10em;" value='<%=b.get("clan")%>'/>
		</td>
		<th>匯送案名：</th>
		<td colspan="3">
			<input type="text" name="exsubj" maxlength="100" style="width:10em;" value='<%=b.get("exsubj")%>'/>
			<span style="font-size:smaller; color:red;">*匯送案名限制100個字元數並請勿涉及個人隱私。</span>
		</td>
	</tr>
<% if(b.get("mod").equals("0")||b.get("mod").equals("1")) { %>
	<tr>
		<th>新分類號：</th>
		<td colspan="5">
			<select name="cat2" id="cat2" style="width:10em;">
				<option value=""></option>
<%
		sql="select distinct bm_cat, ct_memo " +
	  		" from dcs3_bat_mst " +
	    	" left outer join dcs0_cat on bm_cat=ct_cat " +
	        " where bm_psid='"+userid+"'"+
	        "union " +
	    	"select distinct dd_cat, ct_memo " +
	        " from dcs5_dty_desc " +
	  		" left outer join dcs0_cat on dd_cat=ct_cat " +
	        " where dd_psid='"+userid+"'"+
	        " order by 1,2";
		cat2Id.Procit(dcstools.ConnPool.getInstance(), userdb, sql, 2);
		for (int i=0; i< cat2Id.getListno(); i++) {
			String cat2,cat_name;
			cat2 = cat2Id.getCol(0, i);
			cat_name = cat2Id.getCol(0, i) + " " + cat2Id.getCol(1, i);
%>
				<option value="<%=cat2%>" <%=(b.get("cat2").equals(cat2)?"selected":"")%>><%=cat_name%></option>
<%		} %>
			</select>
			<span style="font-size:smaller; color:red;">*附記限制300個中文字。</span>
		</td>
	</tr>
	<tr>
		<th>附記：</th>
		<td colspan="5">
			<input type="text" name="memo" style="width:49em;" value="<%= b.get("memo") %>"/>
		</td>
	</tr>
<%	} %>
	<tr>
		<th>查詢文號：</th>
		<td colspan="5">
			<input type="text" name="sno" style="width:10em;" maxlength="11" value='<%=b.get("sno")%>'/>
			<button onclick="jdcs3c000_sno_search(); return false;">單一文號查詢</button>
		</td>
	</tr>
	
</table>

<div style="width:650px; margin-left:auto; margin-right:auto;">
	<table class="formtab" style="border-spacing:0;" border="1" class="Table">
		<td  width=55 align=middle class="Table_th" >收發別</td>
		<td  width=95 align=middle class="Table_th" >文電字</td>
		<td  width=100 align=middle class="Table_th" >文號</td>
		<td  width=400 align=middle class="Table_th" >主旨</td>
	</table>
	<SELECT multiple class="Select" ondblclick=selectMove("select1","select2") name="select1" id="select1" size=2 style="FONT-FAMILY: monospace;HEIGHT: 150px; WIDTH: 651px">
<%
	List<jdcs3c000.jdcs3c000Bean> v1 = (List<jdcs3c000.jdcs3c000Bean>) request.getAttribute("list1");
	if(v1 != null) {
		for(jdcs3c000.jdcs3c000Bean f : v1) {
%>
		<OPTION value='<%=f.get("rowid")%>' <%=f.get("sel")%>><%=f.get("name")%>
<%  	}
	}
%>
	</SELECT>

	<div style="text-align:center;">
		<a href="javascript:selectMove('select1','select2');">
			<img  align=absmiddle alt="增加" border="0" name="btn_ist2" src="${pageContext.request.contextPath}/images/btn_ist2.gif" class="btn_image" onmouseover="this.src='${pageContext.request.contextPath}/images/btn_wist2.gif'" onmouseout="this.src='${pageContext.request.contextPath}/images/btn_ist2.gif'">
		</a>
		<a href="javascript:selectMoveAll('select1','select2');">
			<img  align=absmiddle alt="增加所有" border="0" name="btn_istall2" src="${pageContext.request.contextPath}/images/btn_istall2.gif" class="btn_image" onmouseover="this.src='${pageContext.request.contextPath}/images/btn_wistall2.gif'" onmouseout="this.src='${pageContext.request.contextPath}/images/btn_istall2.gif'">
		</a>
		&nbsp;&nbsp;
		<a href="javascript:selectMove('select2','select1');">
			<img  align=absmiddle alt="刪除" border="0" name="btn_del2" src="${pageContext.request.contextPath}/images/btn_del2.gif" class="btn_image" onmouseover="this.src='${pageContext.request.contextPath}/images/btn_wdel2.gif'" onmouseout="this.src='${pageContext.request.contextPath}/images/btn_del2.gif'">
		</a>
		<a href="javascript:selectMoveAll('select2','select1');">
			<img  align=absmiddle alt="刪除所有" border="0" name="btn_delall2" src="${pageContext.request.contextPath}/images/btn_delall2.gif" class="btn_image" onmouseover="this.src='${pageContext.request.contextPath}/images/btn_wdelall2.gif'" onmouseout="this.src='${pageContext.request.contextPath}/images/btn_delall2.gif'">
		</a>
	</div>

	<table class="formtab" style="border-spacing:0;" border="1" class="Table">
		<td  width=55 align=middle class="Table_th" >收發別</td>
		<td  width=95 align=middle class="Table_th" >文電字</td>
		<td  width=100 align=middle class="Table_th" >文號</td>
		<td  width=400 align=middle class="Table_th" >主旨</td>
	</table>
	<SELECT multiple class="Select" ondblclick=selectMove("select2","select1") name="select2" id="select2" size=2 style="FONT-FAMILY: monospace;HEIGHT: 150px; WIDTH: 651px">
<%
	List<jdcs3c000.jdcs3c000Bean> v2 = (List<jdcs3c000.jdcs3c000Bean>) request.getAttribute("list2");
	if(v2 != null) {
		for(jdcs3c000.jdcs3c000Bean f : v2) {
%>
		<OPTION value='<%=f.get("rowid")%>'><%=f.get("name")%>
<%		}
	}
%>
	</SELECT>
</div>

<div style="text-align:center; margin-top:5px;">
	<button onclick="jdcs3c000_search(1); return false;">新增查詢</button>
	<button onclick="jdcs3c000_search(2); return false;">修改查詢</button>
	<button onclick="jdcs3c000_search(3); return false;">退件未收</button>
			
<% if(b.get("mod").equals("0")||b.get("mod").equals("1")) { %>
	<button onclick="jdcs3c000_confirm(); return false;">確認</button>
<% } %>

<% if(!b.get("mod").equals("")&&!b.get("mod").equals("0")) { %>
	<button onclick="jdcs3c000_print(); return false;">列印</button>
<% } %>

	<button onclick="jdcs3c000_search(4); return false;">待歸檔清單</button>
	<button onclick="jdcs3c000_clear(); return false;">清除</button>

	<br>
	<LABEL class="Lable_visible">處理訊息：</LABEL>
	<input readOnly value="<%=tmpMSG%>" type="text" name="msg" size="30" class="Text_Read">
</div>

<input type="hidden" name="listid1">
<input type="hidden" name="listid2">
<input type="hidden" name="list1">
<input type="hidden" name="list2">
<input type="hidden" name="listcnt1">
<input type="hidden" name="listcnt2">
<input type="hidden" name="mod" value="<%=b.get("mod")%>">
<input type="hidden" name="refg" value="">
<INPUT type="hidden" name=filename value="<%=b.get("p_filename")%>">
</FORM>
</BODY>
</HTML>
