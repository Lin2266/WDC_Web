<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<title>檔案下載</title>
<script>
function downloadAfileAction2() {
	ta.submit("${pageContext.request.contextPath}/demo002013.ajax?_action=downloadAfile2");
}
function openAfileAction2() {
	ta.submit("${pageContext.request.contextPath}/demo002013.ajax?_action=openAfile2", null, { target: "_blank" });
}
</script>
</head>
<body>

例:
<ul> 
	<li>以 link <a href="${pageContext.request.contextPath}/demo002013.ajax?_action=downloadAfile2">下載一個檔案並另存新檔</a>
	<li>或以 action 發動下載並另存訢檔: <button onclick="downloadAfileAction2(); return false;">下載</button>
	<li>以 link <a href="${pageContext.request.contextPath}/demo002013.ajax?_action=openAfile2" target="_blank">直接開啟一個檔案</a>
	<li>或以 action 發動而開啟檔案: <button onclick="openAfileAction2(); return false;">下載並開啟</button>
	<li>下載另存新檔或直接開啟, 由 server 端 action 程式決定
</ul>

說明:
<ul>
	<li>供瀏覽器端對 &lt;a&gt; link 下載檔案.
	
	<li>中文檔名的檔案在非中文環境的 server 中, 如果以 static link 方式下載(透過網頁 &lt;a&gt; 標籤), <br/>
		檔名恐怕成為亂碼, 故需藉助工具提供檔案下載.
		
	<li>不同瀏覽器對經由 link 下載檔案的處理機制並不一致, 故需藉助工具統一因應各種瀏覽器.
	
	<li>JSP 畫面:<br/>
		link &lt;a&gt; 網址寫成如下樣式:
<code><b>/CONTEXT_PATH/當前畫面JSP的AJAX網址?_action=ACTION_METHOD&參數1=值1&參數2=值2...</b>

例: 當前畫面JSP路徑: /abc/demo002013.jsp
link: &lt;a href="&#36;{pageContext.request.contextPath}<b>/abc/demo002013.ajax</b>?_action=downloadDoc&id=0"&gt;下載&lt;/a&gt;
</code>
		如果網址後<b>參數值</b>中含中文者, 應以 JavaScript encodeURIComponent() 編碼, 如:
<code>&lt;a id="link1" href="#"&gt;下載&lt;/a&gt;

&lt;script&gt;
 document.getElementById("link1").href =
     "&#36;{pageContext.request.contextPath}/demo002013.ajax?_action=downloadDoc?aaa=" +
     <b>encodeURIComponent(</b>"值1"<b>)</b> + "&bbb=" + <b>encodeURIComponent(</b>"值2"<b>)</b>;
&lt;/script&gt;

<div class="warning"><b>NOTE</b>: 只對參數<b>值</b>編碼, 不能把 query 分隔字元 "?", "&amp;" 等也編碼了.
<b>注意</b>: 使用 link &lt;a&gt; 方式會把參數值暴露在網址上</div></code>
      
		或者以 action 方式(即 form submit 方式)發動下載, 如:
<code>&lt;form name="form1" id="form1" method="post"&gt;
    &lt;input type="hidden" name="aaa" value="..."/&gt;
    &lt;input type="hidden" name="bbb" value="..."/&gt;
    &lt;<b>button</b> onclick="downloadDocAction(); return false;"&gt;下載&lt;/<b>button</b>&gt;
&lt;/form&gt;

&lt;script&gt;
function downloadDocAction() {
    ta.submit(
        "&#36;{pageContext.request.contextPath}<b>/abc/demo002013.ajax</b>?_action=downloadDoc",
        "form1");
}
&lt;/script&gt;

<b>或者不透過 form 放置參數, 全部包成參數物件送出</b>:
&lt;script&gt;
function downloadDocAction() {
    ta.submit(
        "&#36;{pageContext.request.contextPath}<b>/abc/demo002013.ajax</b>?_action=downloadDoc",
        <b>{ aaa:"...", bbb:"..." }</b>);
}
&lt;/script&gt;

<div class="warning"><b>NOTE</b>: 以 POST 方式送出 request (ta.submit() 的預設方式),
      參數不需自行編碼, 也不會暴露在網址中</div>
</code>

	<li>Backing bean: <br/>
		使用 JspUtil.clientDownload 或 JspUtil.clientDownloadAndOpen() 工具. 例:
<code>&#64;BackingBean(name="demo002013", path="/abc/demo002013")
public class Demo002013 {
    ...
    
    <b>&#64;AjaxAction</b>
    public void downloadDoc(<b>HttpServletResponse response</b>,
            @Input("aaa") String aaa, @Input("bbb") String bbb) {
        ...
        final File file = ...;
        ...
        
        //下載並另存新檔
        <b>JspUtil.clientDownload</b>(response, file, "測試.doc");
        //或下載後直接以對應的應用程式開啟(要指明檔案的 MIME-TYPE):
        <b>JspUtil.clientDownloadAndOpen</b>(response, file, "測試.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        
<div class="warning"><b>NOTE</b>: JspUtil.clientDownload()/clientDownloadAndOpen() 之前不能有 response.getWriter() 或 response.getOutputStream() 之類的動作.
      "下載並直接開啟" 要有正確的 MIME-TYPE 值, 可搜尋關鍵字: "mime-type ext".
      未指定 MIME-TYPE 者, 恐怕瀏覽器只會當作一般的檔案下載而另存新檔.
</div></code>
</ul>


<div class="footer">
	<hr/>
	原始碼：<a href="${pageContext.request.contextPath}/sourceview/web/demo002013.jsp.txt" target="_blank">demo002013.jsp</a>
	&nbsp; &nbsp; &nbsp;
	<a href="${pageContext.request.contextPath}/sourceview/src/demo/view/Demo002013.java.txt" target="_blank">Demo002013.java</a>
	&nbsp; &nbsp; &nbsp;
</div>

</body>
</html>