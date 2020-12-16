<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>Tab 頁籤</title>
<style type="text/css">
li { margin:1em 0; }
div.enum li, div.code li, code li { margin:.5em 0; }
</style>
<script type="text/javascript">

</script>
</head>
<body>

<ol>
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="t" uri="commons.tatung.com" %&gt; 宣告
	<li>原則上所有頁籤的內容寫在同一 JSP 檔內，但個別頁籤內容太多太複雜者，仍需單獨寫成另一 JSP 檔
	<li>以 <b>&lt;t:tabs&gt;&lt;/t:tabs&gt;</b> 容納一組頁籤，其中每個頁籤所包含的內容，各自置於 <b>&lt;t:tab&gt;&lt;/t:tab&gt;</b> 區域中
		<div class="enum">
			<b>&lt;t:tabs&gt;</b> 擁有如下屬性：
			<ol>
				<li><b>id</b> (string)
				<li><b><i>onCreate</i></b>: (string) 處理頁籤組件產生後的事件處理 JavaScript 碼
				<li><b><i>onActivate</i></b>: (string) 處理頁籤被點擊並成為當前作用中頁籤後 的事件處理 JavaScript 碼
					<ul class="warning">
						<li>可取用 "_id" (當前頁籤區域 id), "_previousId" (前個頁籤區域 id) 變數名.
						<li>注意: 本屬性對連往其他非 "當前網址的畫面" 的頁籤無效.
					</ul>
				<li><b><i>isAllTabAsLink</i></b>: (boolean, default false) 明確指明頁籤是否只單純作為一般的 link(tab 上可能具備 uri 屬性, 也可能藉 onclick 事件而換頁）
			</ol>
		</div>
		<div class="enum">
			<b>&lt;t:tab&gt;</b> 擁有如下屬性：
			<ol>
				<li><b>id</b>： (string)
				<li><b>label</b>：(string) 顯示在標籤上的文字
				<li><b><i>uri</i></b>： (string, optional) 通往其他頁面的頁籤(頁籤內容不在當前 JSP 檔中), 該頁面的網址
					<ul class="warning">
						<li>整組 &lt;t:tab&gt;，或者全部沒有 uri 屬性，或者全部都有
						<li>即使沒有 uri 屬性，但仍可能藉 onclick 事件以 JavaScript 進行切換頁面，此時得在 &lt;t:tabs isAllTabAsLink="true"&gt; 明確指明以換頁模式切換頁籤
					</ul>
				<li><b><i>onclick</i></b>： (string, optional) 處理標籤被點中時的 click 事件之 JavaScript 碼 (<span class="comment">當 uri 屬性存在時，不應指定本 onclick 屬性</span>)
				<li><b><i>isDefaultSelected</i></b>: (boolean, default false) 畫面載入時，是否直接顯示本頁籤？
					<ul class="warning">
						<li>一組頁籤中，只能有一個 isDefaultSelected="true"
						<li>若以換頁模式切換頁籤者，此屬性也無效
					</ul>
			</ol>
		</div>
	<li>有時需要以 JavaScript 程式碼控制開啟特定頁籤，呼叫 ta.tabNo(tabsId, tabIndex) 函數(tabIndex 自 0 起算)即可 (<b>只對不換頁的模式有效</b>)。
</ol>

例1 (不換頁模式)內容全部寫在同一頁：
<t:tabs id="tabs1">
	<t:tab label="頁籤一" id="tabs1a">
		這是頁籤一的內容
		<p/>&nbsp;<p/>&nbsp;<p/>
	</t:tab>
	<t:tab label="頁籤二" id="tabs1b">
		這是頁籤二的內容
		<p/>&nbsp;<p/>&nbsp;<p/>
	</t:tab>
	<t:tab label="頁籤三" id="tabs1c">
		這是頁籤三的內容
		<p/>&nbsp;<p/>&nbsp;<p/>
	</t:tab>
</t:tabs>

<button onclick="ta.tabNo('tabs1', 0)">切換至第 1 頁</button>
<button onclick="ta.tabNo('tabs1', 1)">切換至第 2 頁</button>
<button onclick="ta.tabNo('tabs1', 2)">切換至第 3 頁</button>

<code>&lt;<b>t:tabs</b> id="tabs1"&gt;
	&lt;<b>t:tab</b> label="頁籤一" id="tabs1a"&gt;
		這是頁籤一的內容
		&lt;p/&gt;&nbsp;&lt;p/&gt;&nbsp;&lt;p/&gt;
	&lt;/<b>t:tab</b>&gt;
	&lt;<b>t:tab</b> label="頁籤二" id="tabs1b"&gt;
		這是頁籤二的內容
		&lt;p/&gt;&nbsp;&lt;p/&gt;&nbsp;&lt;p/&gt;
	&lt;/<b>t:tab</b>&gt;
	&lt;<b>t:tab</b> label="頁籤三" id="tabs1c"&gt;
		這是頁籤三的內容
		&lt;p/&gt;&nbsp;&lt;p/&gt;&nbsp;&lt;p/&gt;
	&lt;/<b>t:tab</b>&gt;
&lt;/<b>t:tabs</b>&gt;

&lt;button onclick="ta.tabNo('tabs1', 0)&gt;切換至第 1 頁&lt;/button&gt;
&lt;button onclick="ta.tabNo('tabs1', 1)&gt;切換至第 2 頁&lt;/button&gt;
&lt;button onclick="ta.tabNo('tabs1', 2)&gt;切換至第 3 頁&lt;/button&gt;
</code>

<p/>
例2 (換頁模式)：每個頁籤均指定 uri 或藉 onclick 事件以 JavaScript 碼切換至不同網頁
	(<nobr>&lt;t:tab uri="..."&gt;</nobr> 或 <nobr>&lt;t:tab onclick="<i>go to another page</i>"&gt;</nobr>)<br/>
	<b>例子見「<a href="${pageContext.request.contextPath}/demo001011.jsp">跨頁 Bean Scope</a>」畫面</b>，
	其中一頁籤之網頁內容：
<code>&lt;<b>t:tabs</b> id="tabs01" <b>isAllTabAsLink</b>="true"&gt;
	&lt;<b>t:tab</b> label="說明頁" id="tab0" <b>isDefaultSelected</b>="true"&gt;

		當前頁籤的內容.....

	&lt;/<b>t:tab</b>&gt;
	
	&lt;<b>t:tab</b> label="第一頁" id="tab1" onclick="gotoPage1()"&gt;&lt;/<b>t:tab</b>&gt;
	&lt;<b>t:tab</b> label="第二頁" id="tab2" onclick="gotoPage2()"&gt;&lt;/<b>t:tab</b>&gt;
	&lt;<b>t:tab</b> label="第三頁" id="tab3" onclick="gotoPage3()"&gt;&lt;/<b>t:tab</b>&gt;
	&lt;<b>t:tab</b> label="out" id="tab4" <b>uri</b>="http://google.com"&gt;&lt;/<b>t:tab</b>&gt;
&lt;/<b>t:tabs</b>&gt;
</code>
	注意：&lt;t:tabs&gt; 最好加 <b>isAllTabAsLink</b>="true" 屬性；且代表當前頁的 &lt;t:tab&gt; 要加 <b>isDefaultSelected</b>="true" 屬性(除非是最前頁)


<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002022.jsp.txt" target="_blank">demo002022.jsp</a>
</div>

</body>
</html>