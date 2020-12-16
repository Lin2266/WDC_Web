package demo.view;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.Input;
import com.test.commons.util.JSONObject;
import com.test.commons.web.widget.TUIGridColumnsHandler;
import com.test.commons.web.widget.TUIGridView;

import demo.model.vo.Test3;
import demo.model.vo.Test7;

@BackingBean("/demo002020")
public class Demo002020 {
	private static final Logger log = LoggerFactory.getLogger(Demo002020.class);

	@AjaxAction
	public JSONObject preLoadGridAction() {
		List<Test3> data = queryGridData();
		return new TUIGridView<Test3>(data).items(new TUIGridColumnsHandler<Test3>() {
			@Override
			public Object[] generateColumns(int rowIndex, Test3 bean, String rowId) throws Throwable {
				Object[] cols = new Object[2];
				cols[0] = bean.getCol1();
				cols[1] = bean.getCol2();
				return cols;
			}
		});
	}
	
	@AjaxAction
	public JSONObject preLoadGrid2Action() {
		List<Test7> data = queryGridData2();
		return new TUIGridView<Test7>(data).items(new TUIGridColumnsHandler<Test7>() {
			@Override
			public Object[] generateColumns(int rowIndex, Test7 bean, String rowId) throws Throwable {
				Object[] cols = new Object[6];
				cols[0] = bean.getCol1();
				cols[1] = bean.getCol2();
				cols[2] = bean.getCol3();
				cols[3] = bean.getCol4();
				cols[4] = bean.getCol5();
				cols[5] = bean.getCol6();
				return cols;
			}
		});
	}
	
	@AjaxAction
	public JSONObject preLoadGrid5Action(@Input("_page") Integer pageNo, @Input("_rows") Integer rowsPerPage) {
		List<Test7> data = queryGridData2();
		return new TUIGridView<Test7>(data, rowsPerPage, pageNo).items(new TUIGridColumnsHandler<Test7>() {
			@Override
			public Object[] generateColumns(int rowIndex, Test7 bean, String rowId) throws Throwable {
				Object[] cols = new Object[6];
				cols[0] = bean.getCol1();
				cols[1] = bean.getCol2();
				cols[2] = bean.getCol3();
				cols[3] = bean.getCol4();
				cols[4] = bean.getCol5();
				cols[5] = bean.getCol6();
				return cols;
			}
		});
	}
	
	List<Test3> queryGridData() {
		List<Test3> data = new ArrayList<Test3>();
		data.add(new Test3("1", "第一筆"));
		data.add(new Test3("2", "第二筆"));
		data.add(new Test3("3", "第三筆"));
		data.add(new Test3("4", "第四筆"));
		data.add(new Test3("5", "第五筆"));
		data.add(new Test3("6", "第六筆"));
		data.add(new Test3("7", "第七筆"));
		data.add(new Test3("8", "第八筆"));
		data.add(new Test3("9", "第九筆"));
		data.add(new Test3("10", "第十筆"));
		return data;
	}
	
	List<Test7> queryGridData2() {
		List<Test7> data = new ArrayList<Test7>();
		data.add(new Test7("1", "一", "test1", "00001", "100001", "第一筆"));
		data.add(new Test7("2", "二", "test2", "00002", "100002", "第二筆"));
		data.add(new Test7("3", "三", "test3", "00003", "100003", "第三筆"));
		data.add(new Test7("4", "四", "test4", "00004", "100004", "第四筆"));
		data.add(new Test7("5", "五", "test5", "00005", "100005", "第五筆"));
		data.add(new Test7("6", "六", "test6", "00006", "100006", "第六筆"));
		data.add(new Test7("7", "七", "test7", "00007", "100007", "第七筆"));
		return data;
	}
}
