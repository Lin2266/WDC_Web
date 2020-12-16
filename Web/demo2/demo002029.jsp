<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" uri="commons.tatung.com" %>
<html>
<head>
<title>可滑動條列</title>
<style type="text/css">
/* 所有條列內的圖案寬度 */
.binderBar ul li > img { width:80px; }

/* slider3 訂制樣式 */
#slider3 {
	background-image: url("${pageContext.request.contextPath}/images/listexample/background.png"); /* 背景圖 */
	border-radius: 10px; /* 圓框半徑 */
	height: 95px; /* 總高(牽就左右鍵背景圖的高度) */
	border: 0;
}
#slider3 li { /* list 內每個項目 */
	width: 120px; /* 要大於內含的圖寬 */
}
#slider3 .btnPrev { /* 左鍵框 */
	width: 32px;
	border-top-left-radius: 10px; /* 左上圓角 */
	border-bottom-left-radius: 10px; /* 左下圓角 */
	background: url("${pageContext.request.contextPath}/images/items/menu_arrow_left.jpg") center center no-repeat;
}
#slider3 .btnPrev:hover {
	background: rgba(255,255,255,0.3) url("${pageContext.request.contextPath}/images/items/menu_arrow_left_hover.jpg") center center no-repeat;
}
#slider3 .btnNext { /* 右鍵框 */
	width: 32px;
	border-top-right-radius: 10px; /* 右上圓角 */
	border-bottom-right-radius: 10px; /* 右下圓角 */
	background: url("${pageContext.request.contextPath}/images/items/menu_arrow_right.jpg") center center no-repeat;
}
#slider3 .btnNext:hover {
	background: rgba(255,255,255,0.3) url("${pageContext.request.contextPath}/images/items/menu_arrow_right_hover.jpg") center center no-repeat;
}
</style>
<script src="${pageContext.request.contextPath}/js/min/sly.min.js"></script>
</head>
<body>

配合使用 Sly JavaScript 程式庫: <a href="http://darsa.in/sly">Sly 首頁</a>

<h3>預設樣式</h3>
一般性滑動條列樣式, 已預先定義於 main.css，按 class=binderBar、slider、btnPrev、btnNext 訂定預設的 CSS

<table class="binderBar" id="slider1">
	<tr>
		<td class="btnPrev"></td>
		<td>
			<div class="slider">
				<ul>
					<li><img src="${pageContext.request.contextPath}/images/listexample/1.png"/><br/>1</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/2.png"/><br/>2</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/3.png"/><br/>3</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/4.png"/><br/>4</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/5.png"/><br/>5</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/6.png"/><br/>6</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/7.png"/><br/>7</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/8.png"/><br/>8</li>
				</ul>
			</div>
		</td>
		<td class="btnNext"></td>
	</tr>
</table>
<script>
ta.internal.listSliderInit("slider1");
</script>

<br/>

<table class="binderBar" id="slider2">
	<tr>
		<td class="btnPrev"></td>
		<td>
			<div class="slider">
				<ul>
					<li><img src="${pageContext.request.contextPath}/images/listexample/1.png"/><br/>1</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/2.png"/><br/>2</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/3.png"/><br/>3</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/4.png"/><br/>4</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/5.png"/><br/>5</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/6.png"/><br/>6</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/7.png"/><br/>7</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/8.png"/><br/>8</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/9.png"/><br/>9</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/10.png"/><br/>10</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/11.png"/><br/>11</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/12.png"/><br/>12</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/13.png"/><br/>13</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/14.png"/><br/>14</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/15.png"/><br/>15</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/16.png"/><br/>16</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/17.png"/><br/>17</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/18.png"/><br/>18</li>
				</ul>
			</div>
		</td>
		<td class="btnNext"></td>
	</tr>
</table>
<script>
ta.internal.listSliderInit("slider2");
</script>

<h3>自訂樣式</h3>
自行以 CSS 改變 class=binderBar、slider、btnPrev、btnNext 等的共同設定(見本畫面的原始碼，針對 id=slider3 的組件)
<table class="binderBar" id="slider3">
	<tr>
		<td class="btnPrev"></td>
		<td>
			<div class="slider">
				<ul>
					<li><img src="${pageContext.request.contextPath}/images/listexample/1.png"/><br/>1</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/2.png"/><br/>2</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/3.png"/><br/>3</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/4.png"/><br/>4</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/5.png"/><br/>5</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/6.png"/><br/>6</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/7.png"/><br/>7</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/8.png"/><br/>8</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/9.png"/><br/>9</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/10.png"/><br/>10</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/11.png"/><br/>11</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/12.png"/><br/>12</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/13.png"/><br/>13</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/14.png"/><br/>14</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/15.png"/><br/>15</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/16.png"/><br/>16</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/17.png"/><br/>17</li>
					<li><img src="${pageContext.request.contextPath}/images/listexample/18.png"/><br/>18</li>
				</ul>
			</div>
		</td>
		<td class="btnNext"></td>
	</tr>
</table>
<script>
ta.internal.listSliderInit("slider3");
</script>


<br/>
<h3>寫法</h3>
<ol>
	<li><div style="color:red; font-style:italic;">本組件尚未包裝為 JSP taglib</div>
	<li>在 HTML &lt;head&gt; 區引入 sly 呈式庫，如:
<code>&lt;script src="\${pageContext.request.contextPath}/js/min/<b>sly.min.js</b>"&gt;&lt;/script&gt;
</code>
	<li>寫 HTML 碼:
<code>&lt;table id="容器id" <b>class="binderBar"</b>&gt; &lt;!-- 最外圍的容器 --&gt;
    &lt;tr&gt;
        &lt;td <b>class="btnPrev"</b>&gt;&lt;/td&gt; &lt;!-- 左箭頭(<i>optional</i>) --&gt;
        &lt;td&gt;
            &lt;div <b>class="slider"</b>&gt;
                &lt;ul&gt; &lt;!-- 原始 list 內容 --&gt;
                    &lt;li&gt;... &lt;/li&gt;
                    &lt;li&gt;... &lt;/li&gt;
                    ...
                &lt;/ul&gt;
            &lt;/div&gt;
        &lt;/td&gt;
        &lt;td <b>class="btnNext"</b>&gt;&lt;/td&gt; &lt;!-- 右箭頭(<i>optional</i>) --&gt;
    &lt;/tr&gt;
&lt;/table&gt;
&lt;script&gt;ta.internal.listSliderInit("容器id")&lt;/script&gt;
</code>
	<%--
	<li>確定 JSP 檔頭已存在 &lt;%@ taglib prefix="ta" uri="commons.tatung.com" %&gt; 宣告
	<li>使用 taglib <b>listslide</b>, 並設置 id 屬性：
<code>&lt;<b>t:listslide</b> <b>id</b>="ListSlide 元件的 id"&gt;
	&lt;<b>t:item</b>&gt;...&lt;/<b>t:item</b>&gt;
	&lt;<b>t:item</b>&gt;...&lt;/<b>t:item</b>&gt;
	...
&lt;/<b>t:listslide</b>&gt;
</code>
	--%>
</ol>

<%--
<h3>說明</h3>
<ul>
	<li>taglib <b>&lt;t:listslide&gt;&lt;/t:listslide&gt;</b>
		<div class="enum">
		    <b>&lt;t:listslide&gt;</b> 擁有如下屬性：
		    <ol>
		    	<li><b>id</b>： (string, 必填) 最外圍容器的 id
		    	<li><b><i>cssClass</i></b>： (string) CSS class 名字
				<li><b><i>style</i></b>： (string) CSS style 碼
		    </ol>
	    </div>
	    taglib <b>&lt;t:item&gt;&lt;/t:item&gt;</b> 用來放置列表內各項目的容器
		<div class="enum">
		    <b>&lt;t:item&gt;</b> 擁有如下屬性：
		    <ol>
		    	<li><b><i>id</i></b>： (string)
		    	<li><b><i>cssClass</i></b>： (string) CSS class 名字
				<li><b><i>style</i></b>： (string) CSS style 碼
		    </ol>
	    </div>
	<li>taglib  &lt;<b>t:listslide</b>&gt; 展開後為如下 HTML 型式：
<code>&lt;table id="ListSlide 元件的 id" <b>class="binderBar"</b>&gt; &lt;!-- 最外圍的容器 --&gt;
	&lt;tr&gt;
		&lt;td <b>class="btnPrev"</b>&gt;&lt;/td&gt; &lt;!-- 左箭頭 --&gt;
		&lt;td&gt;
			&lt;div <b>class="listBarContainer"</b>&gt;
				&lt;ul&gt; &lt;!-- 原始 list 內容 --&gt;
					&lt;li&gt;... &lt;/li&gt;
					&lt;li&gt;... &lt;/li&gt;
					...
				&lt;/ul&gt;
			&lt;/div&gt;
		&lt;/td&gt;
		&lt;td <b>class="btnNext"</b>&gt;&lt;/td&gt; &lt;!-- 右箭頭 --&gt;
	&lt;/tr&gt;
&lt;/table&gt;
&lt;script&gt;ta.internal.listSliderInit("ListSlide 元件的 id")&lt;/script&gt;
</code>
	<ul>
		<li>所以欲自行以 CSS 碼改變滑動條列的預設樣式，須使用此最外圍容器的 id 來定位，請參考本畫面的 HTML 碼之 &lt;style&gt; 的作法
		<li>如果以上 taglib 無法滿足特殊需求，如動態産生 ul 內容之類的場合，不妨參考套用以上展開後的 HTML 碼，再呼叫 JavaScript 工具 ta.internal.listSliderInit() 加工
	</ul>
</ul>
--%>

<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002029.jsp.txt" target="_blank">demo002029.jsp</a>
</div>
</body>
</html>