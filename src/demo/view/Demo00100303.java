package demo.view;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.web.widget.TUIGridColumnsHandler;
import com.test.commons.web.widget.TUIGridView;

@BackingBean("/demo00100303")
public class Demo00100303 {
	private static final Logger log = LoggerFactory.getLogger(Demo00100303.class);
	
	@AjaxAction
	public String queryAction() {
		List<String[]> data = queryGridData();
		return new TUIGridView<String[]>(data).items(new TUIGridColumnsHandler<String[]>() {
			@Override
			public Object[] generateColumns(int rowIndex, String[] bean, String rowId) throws Throwable {
				Object[] cols = new Object[4];
				cols[0] = bean[0];
				cols[1] = bean[1];
				cols[2] = bean[2];
				cols[3] = bean[3];
				return cols;
			}
		}).toString();
	}
	
	List<String[]> queryGridData() {
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] { "一", "1", "test1", "測試一" });
		data.add(new String[] { "二", "2", "test2", "測試二" });
		data.add(new String[] { "三", "3", "test3", "測試三" });
		data.add(new String[] { "四", "4", "test4", "測試四" });
		data.add(new String[] { "五", "5", "test5", "測試五" });
		return data;
	}
}
