package course.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;


@WebListener
public class WebStartListener implements ServletContextListener {
	public static final String DB = "DB";

    public void contextInitialized(ServletContextEvent event)  { 
        ServletContext servletContext = event.getServletContext();
        //create connection pool
        BasicDataSource ds = new BasicDataSource();
        //ds.setDriverClassName("org.apache.derby.jdbc.clientDriver");//java DB
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername("root");
        ds.setPassword("123456");
        //ds.setUrl("jdbc:derby://localhost:1527/myDB");
        ds.setUrl("jdbc:mysql://localhost:3306/totalbuy?zeroDateTimeBehavior=convertToNull&characterEncoding=utf-8\"");
        ds.setInitialSize(10);
        //set connection pool in ServletContext
        servletContext.setAttribute(DB,ds);
        //web.xml也要設定
        
   }
	

    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Null implementation
    }

}
