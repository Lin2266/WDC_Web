<%@page import="java.util.List,java.util.Date" buffer="1kb" autoFlush="true" session="true"%>
<!--charset給前端看的編碼，pageEncoding後端-->
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" info="Hello jsp"%>
<!DOCTYPE html>
<!--在網頁直接存檔修改，存檔完要改延伸檔名html改jsp-->
<!--page要記得加，不然中文字會變亂碼-->
<%!
    int i = 10000; //member variable,會在類別建立屬性

    private String welcome = "您好";

    public void jspInit() {
        System.out.println(this.getClass().getName() + " init...");
        String welcome = this.getInitParameter("welcome");
        if (welcome != null) {
            this.welcome = welcome;
        }
    }

//                public void jspDestroy(){
//                    System.out.println(this.getClass().getName() + " destroy");
//                }
%>
<html>
    <head>
        <meta charset="UTF-8">
        <!--getServletInfo()是< %@page的info標頭跟標題顯示一樣-->
        <title><%= this.getServletInfo()%></title>
    </head>
    <body>
        <h1><%= this.getServletInfo()%></h1>
        <p>拜訪人次: <%= application.getAttribute("app.visitors")%></p>
        <p><%=welcome%>
            <%
                int i = 100; //local variable
                //out.println(1D/0);
                out.println(new Date());
                List list;
            %>
        <p><%="i:" + i%></p>
        <hr>
        <!--12-22 request, response, session, application, config 變數-->
        
        <h2>requset</h2>
        <!--request可以取得客戶端的資料，取得使用者的系統版本-->
        <p>user-agent: <%= request.getHeader("user-agent")%>
            <!--請求的方法型態為get-->
        <p>method type:<%= request.getMethod()%></p><!--GET-->
        <!--取得現在頁面的網址專案目錄-->
        <p>uri: <%= request.getRequestURI()%></p><!--/totalbuy/zh-tw/hello.jsp-->
        <!--取得現在頁面的網址-->
        <p>url: <%= request.getRequestURL()%></p><!--http://localhost:8180/totalbuy/zh-tw/hello.jsp-->
        <!--取得網站專案名稱路徑-->
        <p>request.context path: <%= request.getContextPath()%></p><!-- /totalbuy -->
        <hr>
        
        <h2>reponse</h2>
        <!--使用者畫面的編碼-->
        <p>reponse.contentType: <%= response.getContentType()%><!-- text/html;charset=big5  -->
        <hr>
        
        <h2>session:</h2>
        <!--session使用者連線，比喻有兩個身份-->
        <%
            out.flush();
            Thread.sleep(1000);
        %>
        <p>session: <%= session.getId()%>
        <hr>
        
        <h2>application</h2>
        <!--application共用資料用，取得網站專案路徑-->
        <p>application.context path: <%= application.getContextPath()%><!--  /totalbuy   -->
        <hr>
        
        <h2>config</h2>
        <!--config(配置)(用this.就可以抓到了), pageContext, page, exception-->
        <!--抓servlet裡的welcome-->
        <p>welcome param: <%= config.getInitParameter("welcome")%></p>
        <p><%=this.getInitParameter("welcome")%></p>
        <p>Servlet/JSP Name: <%= this.getServletName()%></p>
        <hr>
        
        <h2>pageContext</h2>
        <!--取得網址做轉型，取得現在頁面的完整路徑,/totalbuy/zh-tw/hello.jsp-->
        <p>pageContext->request <%= ((HttpServletRequest) pageContext.getRequest()).getRequestURI()%></p>
        <hr>
        
        <h2>page</h2>
        <!--抓這類別的指定變數-->
        <p>this.i: <%= this.i%></p>
        <p>this.hashCode(): <%= this.hashCode()%></p>
        <p>page.hashCode(): <%= page.hashCode()%></p>
        <%--       
                <hr>
        <!--錯誤畫面，後端(error.jsp)先跑前端(divide_by_zero)畫面才跑的出來-->
                <h2>exception</h2>
                <p>exception: <%= exception %></p>
        --%>
    </p>
</body>
</html>