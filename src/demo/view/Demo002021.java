package demo.view;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.Input;
import com.test.commons.web.widget.TUISelect;

@BackingBean("/demo002021")
public class Demo002021 {
	private static final Logger log = LoggerFactory.getLogger(Demo002021.class);
	
	@AjaxAction
	public String drawSelect1Action() {
		return new TUISelect<String[]>(queryData1Service()).options().toString();
	}
	
	@AjaxAction
	public String drawSelect2Action() {
		return new TUISelect<String[]>(queryData1Service()).options().toString();
	}
	
	@AjaxAction
	public String drawSelect3Action() {
		return new TUISelect<String[]>(queryData1Service()).options().toString();
	}
	
	@AjaxAction
	public String drawSelect4Action() {
		return new TUISelect<String[]>(queryData1Service())
				.setSelectedValues("value1").options().toString();
	}
	
	/** 因應 select4 選單受選項而填入對應的 select4a 選單內容 */
	@AjaxAction
	public String renderSelect4a(@Input("select4") String select4) {
		List<String[]> data = queryList4aData(select4);
		return new TUISelect<String[]>(data).options().toString();
	}
	
	/** 因應 select4a 選單受選項而填入對應的 select4b 選單內容 */
	@AjaxAction
	public String renderSelect4b(@Input("select4a") String select4a) {
		List<String[]> data = queryList4bData(select4a);
		return new TUISelect<String[]>(data).options().toString();
	}
	
	@AjaxAction
	public String renderSelect5() {
		return new TUISelect<String[]>(queryData1Service()).options().toString();
	}
	
	@AjaxAction
	public String renderSelect6a() {
		return new TUISelect<String[]>(queryData1Service()).options().toString();
	}
	
	private List<String[]> queryData1Service() {
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "value1", "郭大同" });
		data.add(new String[] { "value2", "黄小異" });
		data.add(new String[] { "value3", "洪毛毛" });
		data.add(new String[] { "value4", "陳東東" });
		data.add(new String[] { "value5", "蔡花花" });
		return data;
	}
	
	private List<String[]> queryList4aData(String v) {
		List<String[]> data = new ArrayList<String[]>();
		if("value1".equals(v)) {
			data.add(new String[] { "0", "員工" });
			data.add(new String[] { "1", "處長" });
		} else if("value2".equals(v)) {
			data.add(new String[] { "0", "員工" });
			data.add(new String[] { "2", "主任" });
		} else if("value3".equals(v)) {
			data.add(new String[] { "0", "員工" });
			data.add(new String[] { "3", "大帥" });
		} else if("value4".equals(v)) {
			data.add(new String[] { "0", "員工" });
		} else if("value5".equals(v)) {
			data.add(new String[] { "0", "員工" });
		}
		return data;
	}
	
	private List<String[]> queryList4bData(String v) {
		List<String[]> data = new ArrayList<String[]>();
		if("0".equals(v)) {
			data.add(new String[] { "a", "新進" });
			data.add(new String[] { "b", "一般" });
			data.add(new String[] { "c", "資深" });
		} else if("1".equals(v)) {
			data.add(new String[] { "d", "一級" });
			data.add(new String[] { "e", "二級" });
			data.add(new String[] { "f", "VIP" });
		} else if("2".equals(v)) {
			data.add(new String[] { "d", "一級" });
			data.add(new String[] { "e", "二級" });
			data.add(new String[] { "f", "VIP" });
		} else if("3".equals(v)) {
			data.add(new String[] { "g", "..." });
		}
		return data;
	}
}
