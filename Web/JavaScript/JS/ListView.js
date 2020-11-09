  var LvScale=1.25;
  var SpanName="";
  var TdInputType="radio";
  var SelRowid=0;
  var AllSelect=0;
  var StrListViewName="";
  var showListViewName="";
  function SetAllSelected(){//20100617 增加預設全選
  	AllSelect=1;
  }
  function ChangeStr(sstr,s1,s2){
    var str=""+document.all(sstr).value;
    if((c=str.indexOf(s1))==-1)return;
    while((c=str.indexOf(s1))!=-1){
      str=""+str.substring(0,c)+s2+str.substring(c+s1.length,str.length);
    }
    document.all(sstr).value=str;
  }
  function ChangeStr1(sstr,s1,s2){
    var str=""+sstr;
    if((c=str.indexOf(s1))==-1)return sstr;
    while((c=str.indexOf(s1))!=-1){
      str=""+str.substring(0,c)+s2+str.substring(c+s1.length,str.length);
    }
    return str;
  }
  function SetTdInputType(setStr){
    if(setStr==null)TdInputType="radio";
    else TdInputType=setStr;
  }
  function SetRowid(rowid){
    SelRowid=rowid;
  }
  function SetScrollHeight(lv,sh){
    document.all("ScrollFrame"+lv).scrollTop=sh;
  }
  function GetScrollHeight(lv){
    return document.all("ScrollFrame"+lv).scrollTop;
  }
  function GetCheckedValue(lv) {
    var radioObj=document.all("lv_rowid_"+lv);
    if(!radioObj)
        return "";
    var radioLength = radioObj.length;
    if(radioLength == undefined)
        if(radioObj.checked)
            return radioObj.value;
        else
            return "";
    for(var i = 0; i < radioLength; i++) {
        if(radioObj[i].checked) {
            return radioObj[i].value;
        }
    }
    return "";
  }

  function SetCheckedValue(lv, newValue) {
        var radioObj=document.all("lv_rowid_"+lv);
    if(!radioObj)
        return;
    var radioLength = radioObj.length;
    if(radioLength == undefined) {
        radioObj.checked = (radioObj.value == newValue.toString());
        return;
    }
    for(var i = 0; i < radioLength; i++) {
        radioObj[i].checked = false;
        if(radioObj[i].value == newValue.toString()) {
            radioObj[i].checked = true;
        }
    }
  }

  function setAlighPropertyOfListViewHtml(controlId, property_align) {
	  var cid = controlId + "_XMLtable2";
	  document.getElementById(cid).align = property_align;
  }

  function getListViewSize(controlId) {
	  var cid = controlId + "_XMLtable";
	  var table = document.getElementById(cid);
	  return table.rows.length;
  }

  function getListViewCell(row, cell_idx) {
	  var cell = row.cells[cell_idx];
	  //alert(cell.innerHTML);
	  return cell;
  }

  function getListViewRow(controlId, row_idx) {
	  var size = getListViewSize(controlId);
	  if(size <= 0){
		  alert('無資料！！');
		  return;
	  }

	  var cid = controlId + "_XMLtable";
	  var table = document.getElementById(cid);
	  var row = table.rows[row_idx];
	  return row;
  }

  function getShowListViewName() {
	  return showListViewName;
  }
  
  var decorateListViewFunctionPoint = null;

  function setListViewDecorateFunction(func) {
	  decorateListViewFunctionPoint = func;
//	  alert(StrListViewName);
  }

  function decorateListView(controlId) {
	  var size = getListViewSize(controlId);
	  var i = 0;
	  var cell = null;
	  var row = null;
	  for(i=0;i<size;i++){
		  row = getListViewRow(controlId, i);
//		  cell = getListViewCell(row, cell_idx);
		  decorateListViewFunctionPoint(row);
	  }
  }

  function CreateListViewHtml(TheListViewName,BrowClick,TheListViewStr,TheTdInputValueNm,htmlWid,htmlHeight,OnClickFunc,xmlStrName,xmlDocOther,xmlDocNm,xslDocOther,xslDocNm){
    //userid,100,center,使用者帳號;username,120,center,使用者姓名;date,120,center,使用時間;funlg,240,center,使用資料記錄;
    //div名稱,是否使用RowClick將資料往上帶,欄位設定,radio(input)要帶的值(關係著要不要顯示radio),寬,高,OnClick要跑的Func,xmlStr名稱,xmlDoc,xmlDoc名稱,xslDoc,xslDoc名稱
      if(htmlWid==null)htmlWid=600;
      if(htmlHeight==null)htmlHeight=210;
      if(xmlStrName==null)xmlStrName="xml_str";
      StrListViewName=TheListViewName;
      htmlWid=htmlWid;
      htmlHeight=htmlHeight*LvScale;
      if(xmlDocOther==null||xmlDocNm==null)
          xmlDocNm="xmlDoc";
      else if(typeof(xmlDocOther)=='string')
            xmlDocNm="xmlDoc";
      if(xslDocOther==null||xslDocNm==null)
          xslDocNm="xslDoc";
      else if(typeof(xslDocOther)=='string')
          xslDocNm="xslDoc";
      if (typeof(OnClickFunc) == "undefined")OnClickFunc="";

      var ListViewHtml=
        "<table  id='"+TheListViewName+"_XMLtable2' border=0 cellpadding=0 cellspacing=0 align=center width="+htmlWid+">"+
        "<tr>"+
        "<td valign=top>"+
//        "<div style='width:"+htmlWid* 58 / 60 +"px;overflow-x:scroll'>"+
        "<div style='width:"+(htmlWid-20) +"px;overflow-x:scroll'>"+
        "<table  id='"+TheListViewName+"_XMLtable3' border=1 cellspacing=0 class=Table>"+
        "  <tr>";
      var xsl_str="";
      var RowClickStr="";//<xsl:value-of select=\"@rowid\" />
      var str=TheListViewStr;
      //--------許碩舜0908增加的  目的是確保第一個變數有值
      var uniqueStr=str.substring(0, (TheListViewStr.indexOf(";")));
      var lastIndexNum = eval(uniqueStr.lastIndexOf(","));
      var firstIndexNum = eval(uniqueStr.lastIndexOf(",", lastIndexNum-1));
      uniqueStr = uniqueStr.substring(firstIndexNum+1, lastIndexNum);
      //--------許碩舜0908增加的--------end
      var msg1="ListView輸入格式錯誤,不可空白";
      var msg2="ListView輸入格式錯誤\n範例：sno,100(寬度),center,收文號(欄位顯示名稱);";
      var TheListViewStrNum=0;
      var b_hidden="";
      if(TheTdInputValueNm==null)TheTdInputValueNm="";
      if( TheTdInputValueNm !=""){
          ListViewHtml+="<th width=40 align=center class=Table_th>選取</th>";
          xsl_str+="<td width=\"41\" align=\"center\" id=\""+TdInputType+"_"+TheListViewName+"\">" +
            "<input type=\""+TdInputType+"\" name=\"lv_rowid"+"_"+TheListViewName+"\" class=\"Radio\" >";
          if(AllSelect==1)
            xsl_str+="<xsl:attribute name=\"checked\">1</xsl:attribute>";
          xsl_str+="<xsl:attribute name=\"value\"><xsl:value-of select=\"@"+TheTdInputValueNm+"\"/>" +
            "</xsl:attribute></input></td>";
      }
      SpanName="ListViewData"+TheListViewName;
      while((i=str.indexOf(";"))!=-1){
          substr=str.substring(0,i+1);
          str=str.substring(i+1,str.length);
          i=substr.indexOf(",");
          if(i==0)alert(msg1); if(i==-1)alert(msg2);
          b_hidden=substr.substring(0,i);//adar
          substr=substr.substring(i+1,substr.length);
          i=substr.indexOf(",");
          if(i==0)alert(msg1); if(i==-1)alert(msg2);
//          try{
//              num_dar=substr.substring(0,i);
//              eval(num_dar);
//          }catch (e){
//              alert("數量必須為半型的阿拉伯數字。");
//              return;
//          }
//          if(num_dar.indexOf("1")==-1 && num_dar.indexOf("2")==-1 && num_dar.indexOf("3")==-1 && num_dar.indexOf("4")==-1 && num_dar.indexOf("5")==-1 && num_dar.indexOf("6")==-1 && num_dar.indexOf("7")==-1 && num_dar.indexOf("8")==-1 && num_dar.indexOf("9")==-1 && num_dar.indexOf("0")==-1){
//              alert("輸入錯誤\n  數量必須為半型的阿拉伯數字。");
//              return;
//          }
          if(b_hidden!="hidden"){
              ListViewHtml+="    <th width="+substr.substring(0,i);
              xsl_str+="<td width=\""+substr.substring(0,i);
          }
          substr=substr.substring(i+1,substr.length);
          i=substr.indexOf(",");
          if(i==0)alert(msg1); if(i==-1)alert(msg2);
          if(b_hidden!="hidden"){
              ListViewHtml+=" align=center";
              xsl_str+="\" align=\""+substr.substring(0,i);
          }
          substr=substr.substring(i+1,substr.length);
          i=substr.indexOf(",");
          if(i==0)alert(msg1); if(i==-1)alert(msg2);
          if(b_hidden!="hidden"){
              ListViewHtml+=" onclick=\"sortfield('0','@"+substr.substring(0,i);
              xsl_str+="\"><xsl:value-of select=\"@"+substr.substring(0,i)+ "\" /></td>";
          }
          if(BrowClick)
              RowClickStr+=substr.substring(0,i)+"=<xsl:value-of select=\"@"+substr.substring(0,i)+"\" />;";
          substr=substr.substring(i+1,substr.length);
          i=substr.indexOf(",");
          if(i!=-1)alert("輸入錯誤，多了逗號。");
          i=substr.indexOf(";");
          if(i==0)alert(msg1);
          if(b_hidden!="hidden"){
              ListViewHtml+="','text','"+SpanName+"',"+xmlDocNm+","+xslDocNm+")\" class=Table_th >"+substr.substring(0,i)+"</th>";
          }
          TheListViewStrNum++;
      }
      if(str != "")alert("輸入錯誤。");
      xsl_str = " "+
        "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"+
        "<xsl:template match=\"/\">"+
        "<table BORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"0\" id=\""+TheListViewName+"_XMLtable\">"+
        "<xsl:for-each select='jsp/rs[@"+uniqueStr+"]'>"+  //--------許碩舜0908修改
        "<xsl:sort select=\"@xxx\" data-type=\"number\" order=\"ascending\"/>"+
        "<tr onmouseover=\"this.style.backgroundColor='#C3D9FF'\" onmouseout=\"this.style.backgroundColor=''\">"+
        "<xsl:attribute name=\"onclick\">RowClick(\""+RowClickStr+"\",\""+TheListViewName+"\",\""+TheTdInputValueNm+"\");"+OnClickFunc+"</xsl:attribute>"+//
        "    <xsl:if test=\"(position() mod 2) = 0\">"+
        "    <xsl:attribute name=\"class\">Table_td</xsl:attribute>"+
        "    </xsl:if>"+
        "    <xsl:if test=\"(position() mod 2) = 1\">"+
        "    <xsl:attribute name=\"class\">Table_td2</xsl:attribute>"+
        "    </xsl:if>"+
        "<xsl:attribute name=\"title\"><xsl:value-of select=\"@title\" /></xsl:attribute>"+
        // "<A>"+
        // "<xsl:attribute name=\"href\">osw19000.osw19000ToEdit?rowid=<xsl:value-of select=\"@rowid\" />"+
        // "</xsl:attribute>"+
        // "<xsl:attribute name=\"target\"><xsl:value-of select=\"@target\" />"+
        // "</xsl:attribute>"+
        // "</A>"+
        xsl_str+
        "</tr>"+
        "</xsl:for-each>"+
        "</table>"+
        "</xsl:template>"+
        "</xsl:stylesheet>";

      if(xslDocOther==null){
          xslDoc.async = false;
          xslDoc.loadXML(xsl_str);
      }else{
          xslDocOther.async = false;
          xslDocOther.loadXML(xsl_str);
      }

      var xmlString;
      if (typeof(xml_str) == "undefined")
        xmlString = document.all(xmlStrName).value;
      else
        xmlString = xml_str;
      var lineCounter = 0;
      var stringPosition = 0;

      while( (stringPosition = xmlString.indexOf(uniqueStr, stringPosition)) >= 0){
           lineCounter++;
           stringPosition = stringPosition + uniqueStr.length+1;
      }
      var OutHeight=(htmlHeight*(10+1.5))/10+25;//cherry update
      var InHeight;//每列高度
      if (typeof(select_cnt) == "undefined"){
        InHeight = (27)*(lineCounter+2);
      }else if(select_cnt != null && select_cnt != "" && eval(select_cnt)!=0){
         InHeight= (27)*(eval(select_cnt)+2);
      }else{
         InHeight = (27)*(lineCounter+2);
      }
      InHeight=InHeight*(LvScale+1);

      ListViewHtml+=
        "  </tr>"+
        "  <tr><td colspan="+TheListViewStrNum+">"+
        "  <div id=DataFrame"+TheListViewName+" style='position:relative;width:100%;overflow:hidden;Height:"+htmlHeight+"'>"+
        "   <div id=DataGroup"+TheListViewName+" style='position:relative'>"+
        "     <span id=ListViewData"+TheListViewName+" ></span>"+
        "   </div>"+
        "  </div>"+
        "  </td></tr>"+
        "</table>"+
        "</div>"+
        "</td>"+
        "<td valign=top>"+
        "<div id=ScrollFrame"+TheListViewName+" style=position:relative;background:#000;overflow-y:scroll;Height:"+OutHeight+" onscroll=DataGroup"+TheListViewName+".style.posTop=-ScrollFrame"+TheListViewName+".scrollTop;>"+
        "  <div id=ScrollGroup"+TheListViewName+" style=position:relative;width:1px;visibility:hidden;Height:"+InHeight+" ></div>"+
        "</div>"+
        "</td></tr></table>";
      //alert(ListViewHtml);

      showListViewName=ListViewHtml;
      document.all(TheListViewName).innerHTML=ListViewHtml;
      if (typeof(xml_str) == "undefined"){
          if(xmlDocNm=='xmlDoc'&&xslDocNm=='xslDoc')
            CreateHtml(document.all(xmlStrName).value,xmlDoc,xslDoc);
          else if(xmlDocNm=='xmlDoc'&&xslDocNm!='xslDoc')
              CreateHtml(document.all(xmlStrName).value,xmlDoc,xslDocOther);
          else if(xmlDocNm!='xmlDoc'&&xslDocNm=='xslDoc')
              CreateHtml(document.all(xmlStrName).value,xmlDocOther,xslDoc);
          else if(xmlDocNm!='xmlDoc'&&xslDocNm!='xslDoc')
              CreateHtml(document.all(xmlStrName).value,xmlDocOther,xslDocOther);
      }else{
            if(xmlDocNm=='xmlDoc'&&xslDocNm=='xslDoc')
              CreateHtml(xml_str,xmlDoc,xslDoc);
            else if(xmlDocNm=='xmlDoc'&&xslDocNm=='xslDoc')
                CreateHtml(xml_str,xmlDoc,xslDocOther);
            else if(xmlDocNm=='xmlDoc'&&xslDocNm=='xslDoc')
                CreateHtml(xml_str,xmlDocOther,xslDoc);
            else if(xmlDocNm=='xmlDoc'&&xslDocNm=='xslDoc')
            CreateHtml(xml_str,xmlDoc,xslDoc);
      }
      //document.all("ScrollFrame"+TheListViewName).style.height=document.all(TheListViewName+"_XMLtable2").clientHeight;
      //document.all("ScrollGroup"+TheListViewName).style.height=document.all(TheListViewName+"_XMLtable").clientHeight+document.all(TheListViewName+"_XMLtable3").clientHeight;
  }

//  ShowText=window.parent.ScrollFrame.parentElement;//.parentElement.parentElement.childNodes(0).
//  alert(ShowText.innerHTML);
//  alert(ShowText.tagName);

  var fg = "descending";    //排序方式：ascending(升冪) 或 descending(降冪)
  function sortfield(col, sortby, type, sortSpanName, sortXmlDoc, sortXslDoc) {     // 排序的javascript
      if (fg=="descending")
          fg = "ascending";
      else
          fg = "descending";
  //之前的是寫 thenode = reportXSL.selectSingleNode("xsl:stylesheet/xsl:template/table/xsl:for-each/xsl:sort");
  //修改如下
      thenode = sortXslDoc.selectSingleNode("xsl:stylesheet/xsl:template/table/xsl:for-each/xsl:sort");
  //設定select,data-type,order等屬性
      thenode.setAttribute("select", sortby);
      //if(sortby=="@grp" || sortby=="@idxno")
      if(sortby.indexOf("grp")!=-1 || sortby.indexOf("idxno")!=-1 || sortby.indexOf("year")!=-1)
          thenode.setAttribute("data-type", "number"); // data-type(型態)有text,number兩種
      else
          thenode.setAttribute("data-type", type); // data-type(型態)有text,number兩種
      thenode.setAttribute("order", fg);
  //之前的是寫 ListViewData.innerHTML = xmlDoc.transformNode(reportXSL.documentElement);
  //修改如下
      document.all(sortSpanName).innerHTML = sortXmlDoc.transformNode(sortXslDoc);
      //alert(document.all(SpanName).innerHTML);//檢視資料內容
  }

  function CreateHtml(CreateHtmlStr,xmlDocOther,xslDocOther){
      xmlstr=CreateHtmlStr;
      xmlDocOther.async = false;
      xmlDocOther.loadXML(xmlstr);
      document.all(SpanName).innerHTML = xmlDocOther.transformNode(xslDocOther);
      //alert(document.all(SpanName).innerHTML); //檢視資料內容
  }

  function RowClick(str,lvnm,tdinput){
      if(lvnm=="")return;
      if(tdinput!=""){
          var src = window.event.srcElement.parentElement;
//          alert(src.tagName);
          if(src.tagName=="TD")
              src = window.event.srcElement.parentElement.parentElement;
          var lvrownm="lv_rowid_"+lvnm;
          var rowidvalue=src.children(""+TdInputType+"_"+lvnm).children(lvrownm).value;
          //alert(rowidvalue);
          if(document.all(tdinput))//20100617 adar add
            document.all(tdinput).value=rowidvalue;

          if(TdInputType=="radio")
            src.children(""+TdInputType+"_"+lvnm).children(lvrownm).checked=true;
          else{
            if(window.event.srcElement.parentElement.tagName!="TD")
              if(src.children(""+TdInputType+"_"+lvnm).children(lvrownm).checked==true)
                src.children(""+TdInputType+"_"+lvnm).children(lvrownm).checked=false;
              else
                src.children(""+TdInputType+"_"+lvnm).children(lvrownm).checked=true;
          }
      }
      if(str=="")return;
      while((i=str.indexOf(";"))!=-1){
          substr=str.substring(0,i+1);
          str=str.substring(i+1,str.length);
          i=substr.indexOf("=");
          if(document.all(substr.substring(0,i))!=null){
              if(document.all(substr.substring(0,i)).tagName==null){
                if(document.all(substr.substring(0,i))[0].type==TdInputType){
                  select_no = document.all(substr.substring(0,i)).length;
                  for (j=0 ; j< select_no; j++) {
                    if(document.all(substr.substring(0,i))[j].value==substr.substring(i+1,substr.length-1))
                      document.all(substr.substring(0,i))[j].checked=true;
                  }
                }
              }else{
                  if(document.all(substr.substring(0,i)).tagName=='TEXTAREA'){
                    document.all(substr.substring(0,i)).value=substr.substring(i+1,substr.length-1);
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"＆","&");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"＜","<");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"＞",">");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"’","'");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"”","\"");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"◎","\r\n");//20070306將替換掉的字換回來
                    if(document.all(substr.substring(0,i)).value==" ")
                        document.all(substr.substring(0,i)).value="";
                  }else if(document.all(substr.substring(0,i)).type=='checkbox'){
                    if(""!=substr.substring(i+1,substr.length-1)){
//                        document.all(substr.substring(0,i)).checked="true";
                        document.all(substr.substring(0,i)).checked="checked";
                    }else{
                        document.all(substr.substring(0,i)).checked=false;
                    }
                  }else{
                    document.all(substr.substring(0,i)).value=substr.substring(i+1,substr.length-1);
                    //if(document.all(substr.substring(0,i)).value==" ")
                    //    document.all(substr.substring(0,i)).value="";
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"＆","&");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"＜","<");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"＞",">");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"’","'");   //20070306將替換掉的字換回來
                    document.all(substr.substring(0,i)).value=ChangeStr1(document.all(substr.substring(0,i)).value,"”","\"");   //20070306將替換掉的字換回來
                  }
              }
          }
      }
  }
  function ReturnInputName(){//20100617
    return "lv_rowid"+"_"+StrListViewName;
  }
  function ListViewSelectAll(){//20100617 增加全選功能 for checkbox
  	if(document.all("lv_rowid"+"_"+StrListViewName)==null)return;
    var cnt = 0;
    if (document.all("lv_rowid"+"_"+StrListViewName).length>1) {
      for (var i=0;i<document.all("lv_rowid"+"_"+StrListViewName).length;i++) {
      	document.all("lv_rowid"+"_"+StrListViewName)[i].checked="checked";
          //wdcs58800.lv_rowid_ListView1[i].checked=false;
      }
    } else {
      document.all("lv_rowid"+"_"+StrListViewName).checked="checked";
    }
  }
  function ListViewCancelAll(){//20100617 增加全不選功能
  	if(document.all("lv_rowid"+"_"+StrListViewName)==null)return;
    if (document.all("lv_rowid"+"_"+StrListViewName).length>1) {
      for (var i=0;i<document.all("lv_rowid"+"_"+StrListViewName).length;i++) {
      	document.all("lv_rowid"+"_"+StrListViewName)[i].checked=false;
      }
    } else {
      document.all("lv_rowid"+"_"+StrListViewName).checked=false;
    }
  }
  function ListViewConfirmThis(fname,target) {
    var cnt = 0;
    if (document.all("lv_rowid"+"_"+StrListViewName).length>1) {
      for (var i=0;i<document.all("lv_rowid"+"_"+StrListViewName).length;i++) {
        if (document.all("lv_rowid"+"_"+StrListViewName)[i].checked) {
          cnt++;
        }
      }
    } else {
      if (document.all("lv_rowid"+"_"+StrListViewName).checked) {
        cnt++;
      }
    }
    if (cnt==0) {
      alert("請勾選一筆資料");
      return;
    }
    fname.method="POST";
    fname.action=target;
    fname.submit();
  }
