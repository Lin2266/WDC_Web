package demo.view;

import java.io.*;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.Action;
import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.Input;

@BackingBean("/demo002010")
public class Demo002010 {
    private static final Logger log = LoggerFactory.getLogger(Demo002010.class);

    @AjaxAction
    public String uploadAction(@Input("testField") String testField, @Input("fileField3") File[] fileField3) {
        String message = "";
        if(testField != null)
            message += "一般欄位 testField=" + testField + "<br/>";
        
        if(fileField3 != null) {
        	message += "已上傳檔案:<br/>";
        	for(File f : fileField3)
        		message += f.getName() + "<br/>"; //只秀出檔名, 不宜隨便把在 server 端的完整路徑秀到前端去
        }
        return message;
    }
    
    @Action
    public void testAction(HttpServletRequest request, @Input("testField") String testField, 
    		@Input("fileField1") File[] fileField1, @Input("fileField2") File[] fileField2) {
        StringBuilder message = new StringBuilder();
        if(testField != null)
            message.append("一般欄位 testField=").append(testField).append("<br/>");
        
        if(fileField1 != null || fileField2 != null) {
        	message.append("已上傳檔案:<br/>");
        	
	        if(fileField1 != null) {
	        	for(File f : fileField1)
	        		message.append(f.getName()).append("<br/>"); //這裡只秀出檔名, 別隨便把在 server 端的完整秀到前端去
	        }
	        
	        if(fileField2 != null) {
	        	for(File f : fileField2)
	        		message.append(f.getName()).append("<br/>"); //這裡只秀出檔名, 別隨便把在 server 端的完整秀到前端去
	        }
        }
        
        request.setAttribute("message", message.toString());
    }
    
    @AjaxAction
    public String testAction2(@Input("testField") String testField, 
    		@Input("fileField1") File[] fileField1, @Input("fileField2") File[] fileField2) {
        StringBuilder message = new StringBuilder();
        if(testField != null)
            message.append("一般欄位 testField=").append(testField).append("<br/>");
        
        if(fileField1 != null || fileField2 != null) {
        	message.append("已上傳檔案:<br/>");
        	
	        if(fileField1 != null) {
	        	for(File f : fileField1)
	        		message.append(f.getName()).append("<br/>"); //這裡只秀出檔名, 別隨便把在 server 端的完整秀到前端去
	        }
	        
	        if(fileField2 != null) {
	        	for(File f : fileField2)
	        		message.append(f.getName()).append("<br/>"); //這裡只秀出檔名, 別隨便把在 server 端的完整秀到前端去
	        }
        }
        
        return message.toString();
    }
}
