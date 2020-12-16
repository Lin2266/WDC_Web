package demo.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.Action;
import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.Input;
import com.test.commons.util.JSONObject;
import com.test.commons.util.JspUtil;
import com.test.commons.web.FlashScope;

@BackingBean("/demo001010")
public class Demo001010 {
	private static final Logger log = LoggerFactory.getLogger(Demo001010.class);
	
	@AjaxAction
	public String doSomethingAction(@Input("target1") Integer target1, @Input("col1") String col1, @Input("col2") String col2) {
		if(target1 == null)
			throw new IllegalArgumentException("欄位三未指定值");
		
		return new JSONObject().put("target1", target1).put("col1", col1).put("col2", col2).toString();
	}
	
	@Action
	public String doSomething2Action(@Input("target1") Integer target1, @Input("col1") String col1, @Input("col2") String col2) {
		if(target1 == null)
			throw new IllegalArgumentException("欄位三未指定值");
		
		//示範用 FlashScope 傳遞參數給下一頁
		FlashScope flashScope = JspUtil.flashScope();
		flashScope.put("col1", col1);
		flashScope.put("col2", col2);
		flashScope.put("target1", target1);
		
		//在這裡直接 forward 到目的頁去了
		if(target1 < 4)
			return "forward:/demo001010_1.jsp";
		return "forward:/demo001010_2.jsp";
	}
}
