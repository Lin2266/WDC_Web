package demo.view;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.DefaultAction;
import com.test.commons.annotation.Input;
import com.test.commons.util.JSONObject;
import com.test.commons.util.MsgUtil;

import demo.model.vo.Test6;

@BackingBean("/demo00100304")
public class Demo00100304 {
	private static final Logger log = LoggerFactory.getLogger(Demo00100304.class);
	
	@AjaxAction
	public String queryAction(@Input("pk") Long pk) {
		Test6 data = findByPk(pk);

		return new JSONObject().put("pk", data.getPk())
				.put("col1", data.getCol1()).put("col2", data.getCol2())
				.put("col3", data.getCol3()).put("col4", data.getCol4()).toString();
	}
	
	@AjaxAction
	public String updateAction(@Input("pk") Long pk, @Input("col1") String col1, 
			@Input("col2") String col2, @Input("col3") Integer col3, 
			@Input("col4") String col4) {
		//實際 update 動作內容省略
		
		return new JSONObject().put("message", MsgUtil.message("修改成功")).toString();
	}

	//傳回假資料, 沒真的查詢 db
	Test6 findByPk(Long pk) {
		Test6 vo = new Test6();
		vo.setPk(pk);
		vo.setCol1("欄位一值一");
		vo.setCol2("欄位二值一");
		vo.setCol3(2);
		vo.setCol4("欄位四值一");
		return vo;
	}
}
