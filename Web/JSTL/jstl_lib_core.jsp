<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta charset="UTF-8">
<title>jstl_core 函式庫標籤</title>
</head>
<body>
	<p>c:out 顯示運算結果或數值。</p>
	<p>c:set 設定變數或屬性的值，可指定儲存範圍。</p>
	<p>c:remove 刪除指定範圍內的變數。</p>
	<p>c:catch 在JSP頁面中捕捉拋出的例外(java.lang.Throwable或其子類別)。</p>
	<p>c:if 評估運算式結果，若為true將執行標籤本體。</p>
	<p>c:choose 沒有屬性，用於標籤 c:when和標籤c:otherwise 的父標籤。</p>
	<p>c:when 等價於「if」、「else if」語句，周含一個test屬性，該屬性表示需要判斷的條件。</p>
	<p>c:otherwise 沒有屬性，等價於「else」語句。</p>
	<p>c:forEach 用於頁面裡的重複結構。</p>
	<p>c:forTokens 可以根據某個分隔符號將字串切割，和java.util.StringTokenizer類別的使用方法相似。</p>
	<p>c:import 包含另一個url的內容到本頁來。</p>
	<p>c:url 得到rewriting後的url。</p>
	<p>c:redirect 用於頁面的重新導向，相當於response.sendRedirect()的作用。</p>
	<p>c:param 新增參數到request中，可用於 c:import 跟 c:url 跟 c:redirect 等標籤。</p>
</body>
</html>