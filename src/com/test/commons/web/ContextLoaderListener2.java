package com.test.commons.web;

import java.io.*;
import java.net.URL;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.context.ContextLoader;

import com.test.commons.exception.ErrorConfigurationException;
import com.test.commons.spring.AnnotationSupportedPropertyPlaceholderConfigurer;
import com.test.commons.util.FileUtil;
import com.test.commons.util.JspUtil;

/**
 * AP (Servlet 類之 AP)啟動時, 負責載入 Spring context, 並進行必要的初始化動作.
 * <p/>改寫自 org.springframework.web.context.ContextLoaderListener:<br/>
 * This listener should be registered after org.springframework.web.util.Log4jConfigListener
 * in <code>web.xml</code>, if the latter is used.
 */
public final class ContextLoaderListener2 extends ContextLoader implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(ContextLoaderListener2.class);
            
    /** 本 AP 之主設定檔, 假設為 UTF8 編碼(包裝後的路徑應置於虛擬路徑之根目錄下) */
    public static final String MAIN_PROPERTY_FILE_NAME = "/main.properties";
    
	/**
     * Initialize the root web application context
     */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        //求 context-path 下之根目錄的實體路徑
        final String rootPath = findRootRealPath(); //根目錄實體路徑
        final File tmpDir = new File(rootPath, "tmp");
        tmpDir.mkdir(); //確保 tmp 目錄存在(在 Eclipse 之 pulish 目錄下不會把空目錄 tmp 給 publish 上來, 在此自行建立)
        JspUtil2.setContextRealPath(rootPath); //JspUtil 作一次性的設定, 供其他程式取用
        FileUtil2.setTempDir(tmpDir); //FileUtil 作一次性的設定, 供其他程式取用
        
        //載入 Spring 容器
        final ServletContext context = event.getServletContext();
        final ApplicationContext springContext = this.initWebApplicationContext(context);
        
        //如果已配置 Xprint/PDF 元件者, 設定其必要的目錄
        try {
	        final com.test.commons.pdf.Xprint xprint = springContext.getBean(com.test.commons.pdf.Xprint.class); //old Xprint
	        if(xprint != null) {
	        	com.test.commons.pdf.Xprint.setStaticallyRptDir(rootPath + "/rpt");
	        	com.test.commons.pdf.Xprint.setStaticallyOutputDir(rootPath + "/tmp");
	        	com.test.commons.pdf.Xprint.setStaticallyImgDir(rootPath + "/images");
	        }
        } catch(org.springframework.beans.factory.NoSuchBeanDefinitionException ne) {
        	log.info("com.tatung.commons.pdf.Xprint not registered in Spring context, skip it.");
        }
        try {
        	final com.test.commons.pdf2.XprintBuilderBean bean = springContext.getBean(com.test.commons.pdf2.XprintBuilderBean.class);
        	if(bean != null) {
        		bean.setDefaultOutputBaseDir(rootPath + "/tmp"); //輸出基準目錄
        		bean.setDefaultImageFileBaseDir(rootPath + "/images"); //圖檔目錄
        		bean.setDefaultFormatFileBaseDir(rootPath + "/rpt"); //格式檔目錄
        		log.debug("init com.tatung.commons.pdf2.XprintBuilderBean: {}", bean);
        	}
        } catch(org.springframework.beans.factory.NoSuchBeanDefinitionException ne) {
        	log.info("com.tatung.commons.pdf2.XprintBuilderBean not registered in Spring context, skip it.");
        }
        
        //如果已配置 Ars2Print 元件者, 設定其必要目錄
        try {
        	final com.test.commons.ars2.ArsPrintBuilderBean bean = springContext.getBean(com.test.commons.ars2.ArsPrintBuilderBean.class);
        	if(bean != null) {
        		bean.setDefaultBaseTempDir(rootPath + "/tmp"); //輸出基準目錄
        		log.debug("init com.tatung.commons.ars2.ArsPrintBuilderBean: {}", bean);
        	}
        } catch(org.springframework.beans.factory.NoSuchBeanDefinitionException ne) {
        	log.info("com.tatung.commons.ars2.ArsPrintBuilderBean not registered in Spring context, skip it.");
        }
        
        //debug main configuration
        //final AnnotationSupportedPropertyPlaceholderConfigurer config = springContext.getBean(AnnotationSupportedPropertyPlaceholderConfigurer.class);
        final PropertySourcesPlaceholderConfigurer config = springContext.getBean(PropertySourcesPlaceholderConfigurer.class);
    	log.debug("main config: {}", config);
    }

	/**
     * Close the root web application context
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.closeWebApplicationContext(event.getServletContext());
        
        //ContextCleanupListener.cleanupAttributes(event.getServletContext()); //default scope method 無法在此被呼叫, 直接抄錄其內容:
        ServletContext sc = event.getServletContext();
        Enumeration<String> attrNames = sc.getAttributeNames();
        while(attrNames.hasMoreElements()) {
            String attrName = attrNames.nextElement();
            if(attrName.startsWith("org.springframework.")) {
                Object attrValue = sc.getAttribute(attrName);
                if(attrValue instanceof DisposableBean) {
                    try {
                        ((DisposableBean)attrValue).destroy();
                    } catch(Throwable ex) {
                        log.error("Couldn't invoke destroy method of attribute with name '" + attrName + "'", ex);
                    }
                }
            }
        }
    }

    //求 context 根目錄之實體路徑(以主設定檔位置來定位)
    //注意: this.getServletContext().getRealPath(""); 不適用 web-app deploy 後不解開 war 檔的 ap server (如 WebLogic)
    private String findRootRealPath() {
    	URL url = this.getClass().getResource(MAIN_PROPERTY_FILE_NAME);
    	if(url == null)
    		throw new ErrorConfigurationException(MAIN_PROPERTY_FILE_NAME + " not found in this ap's CLASSPATH");
        String mainPropertyPath = url.getPath(); //主設定檔路徑, 置於存在於 CLASSPATH 中的目錄或 jar 中, 通常是 WEB-INF/classes/ 下
        String rootPath = mainPropertyPath.substring(0, mainPropertyPath.lastIndexOf("WEB-INF") - 1); //截取 WEB-INFO 之前的路徑
        log.info("found application root real-path: {}", rootPath);
        return rootPath;
    }

    private static class JspUtil2 extends JspUtil {
    	public static void setContextRealPath(String contextRealPath) {
    		JspUtil.setContextRealPath(contextRealPath);
    	}
    }
    
    private static class FileUtil2 extends FileUtil {
    	public static void setTempDir(File tempDir) {
    		FileUtil.setTempDir(tempDir);
    	}
    }
}
