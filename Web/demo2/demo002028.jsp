<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<title>多區塊切換</title>
<style type="text/css">
#item0, #item1, #item2, #item3, #item4, #item5 { margin-left:4em; margin-right:4em; border:1px solid #666; min-height:20em; border-top-left-radius:5px; border-top-right-radius:5px; }
.heading { color:#fff; padding:2px 1em; background-color:#488AC7; border-top-left-radius:4px; border-top-right-radius:4px; }
.return0 { float:right; }
.return0 a { text-decoration:none; color:#fff; }
</style>
<script>
function init() {
	ta.exclusiveItems([ "item0", "item1", "item2", "item3", "item4", "item5" ], "itemsGroup");//.exclusiveItem("item0", "itemsGroup");
}
</script>
</head>
<body onload="init();">

<ul>
	<li>實作一組 div 區塊，一次只顯示其中一個，可随意切換
	<li>使用共用 JavaScript 程式庫 ta.exclusiveItem<b>s</b>(), ta.exclusiveItem()
	<li>先在 HTML 檔上寫好欲納入切換的 div 區塊, 然後藉 &lt;body onload="..."&gt; 事件處理或在 div 區塊之後，埋 JavaScript 碼 ta.exclusiveItem<b>s</b>() 將所有區塊預登錄
	<li>切換區塊函數 ta.exclusiveItem(itemIds, itemsGroupName, animation)<br/>
		其中 animation 參數可不指定，或指定：
		<ul>
			<li>1: 由上向下滑動顯示
			<li>2: 由下向上滑動顯示
			<li>3: 由左向右滑動顯示
			<li>4: 由右向左滑動顯示
		</ul>
	<li>詳細 API 說明見「<a href="${pageContext.request.contextPath}/demo003010.jsp">共用 JavaScript API</a>」
	<li><b>div 區塊內宜避免産生 scroll bar (不使用 CSS overflow:auto或scroll屬性)，以免 div 區塊及瀏覽器畫面産生多重 scroll bar，降低可用度</b>
</ul>

<div id="item0">
	<div class="heading">主區塊</div>
	<p/>
	<button onclick="ta.exclusiveItem('item1', 'itemsGroup', 1); return false;">區塊 1 ↘</button><p/>
	<button onclick="ta.exclusiveItem('item2', 'itemsGroup', 2); return false;">區塊 2 ↖</button><p/>
	<button onclick="ta.exclusiveItem('item3', 'itemsGroup', 3); return false;">區塊 3 →</button><p/>
	<button onclick="ta.exclusiveItem('item4', 'itemsGroup', 4); return false;">區塊 4 ←</button><p/>
	<button onclick="ta.exclusiveItem('item5', 'itemsGroup'); return false;">區塊 5 (無動畫特效)</button>
</div>

<div id="item1" style="display:none">
	<div class="heading">區塊1 <span class="return0">[<a href="#" onclick="ta.exclusiveItem('item0', 'itemsGroup', 2);">返回</a>]</span></div>
	
</div>

<div id="item2" style="display:none">
	<div class="heading">區塊2  <span class="return0">[<a href="#" onclick="ta.exclusiveItem('item0', 'itemsGroup', 1);">返回</a>]</span></div>
	
</div>

<div id="item3" style="display:none">
	<div class="heading">區塊3  <span class="return0">[<a href="#" onclick="ta.exclusiveItem('item0', 'itemsGroup', 4);">返回</a>]</span></div>
	
</div>

<div id="item4" style="display:none">
	<div class="heading">區塊4  <span class="return0">[<a href="#" onclick="ta.exclusiveItem('item0', 'itemsGroup', 3);">返回</a>]</span></div>
	
</div>

<div id="item5" style="display:none">
	<div class="heading">區塊5  <span class="return0">[<a href="#" onclick="ta.exclusiveItem('item0', 'itemsGroup');">返回</a>]</span></div>
	
</div>



<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002028.jsp.txt" target="_blank">demo002028.jsp</a>
</div>

</body>
</html>