package demo.view;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.Input;
import com.test.commons.web.widget.TUIGridColumnsHandler;
import com.test.commons.web.widget.TUIGridView;

import demo.model.vo.Test4;
import demo.model.vo.Test5;

@BackingBean("/demo001002")
public class Demo001002 {
	private static final Logger log = LoggerFactory.getLogger(Demo001002.class);
	
	@AjaxAction
	public String queryAction1() {
		List<Test4> data = fakeQuery();
		return new TUIGridView<Test4>(data).items(new TUIGridColumnsHandler<Test4>() {
			@Override
			public Object[] generateColumns(int rowIndex, Test4 bean, String rowId) throws Throwable {
				Object[] cols = new Object[4];
				cols[0] = bean.getPk();
				cols[1] = bean.getCol1();
				cols[2] = bean.getCol2();
				cols[3] = bean.getCol3();
				return cols;
			}
		}).toString();
	}
	
	@AjaxAction
	public String queryDetailAction(@Input("pk") Long pk) {
		List<Test5> data = fakeQuery2(pk);
		return new TUIGridView<Test5>(data).items(new TUIGridColumnsHandler<Test5>() {
			@Override
			public Object[] generateColumns(int rowIndex, Test5 bean, String rowId) throws Throwable {
				Object[] cols = new Object[3];
				cols[0] = bean.getPk();
				cols[1] = bean.getCola();
				cols[2] = bean.getColb();
				return cols;
			}
		}).toString();
	}
	
	List<Test4> fakeQuery() {
		List<Test4> data = new ArrayList<Test4>();
		data.add(new Test4(1000L, "column 1", "欄位一", "測 test 1"));
		data.add(new Test4(1001L, "column 2", "欄位二", "測 test 2"));
		data.add(new Test4(1002L, "column 3", "欄位三", "測 test 3"));
		data.add(new Test4(1003L, "column 4", "欄位四", "測 test 4"));
		return data;
	}
	
	List<Test5> fakeQuery2(Long masterPK) {
		List<Test5> data = new ArrayList<Test5>();
		if(masterPK == 1000L) {
			data.add(new Test5(100L, "column a1", "欄位A1"));
			data.add(new Test5(101L, "column a2", "欄位A2"));
		} else if(masterPK == 1001L) {
			data.add(new Test5(102L, "column a3", "欄位A3"));
		} else if(masterPK == 1002L) {
			data.add(new Test5(103L, "column a4", "欄位A4"));
			data.add(new Test5(104L, "column a5", "欄位A5"));
		} else if(masterPK == 1003L) {
			data.add(new Test5(105L, "column a6", "欄位A6"));
			data.add(new Test5(106L, "column a7", "欄位A7"));
			data.add(new Test5(107L, "column a8", "欄位A8"));
		}
		return data;
	}
}
