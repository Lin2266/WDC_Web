package demo.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.*;

import com.test.commons.annotation.Action;
import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.DefaultAction;
import com.test.commons.annotation.Input;
import com.test.commons.util.DateUtil;
import com.test.commons.util.JSONObject;
import com.test.commons.util.StrUtil;
import com.test.commons.web.widget.TUIGridColumnsHandler;
import com.test.commons.web.widget.TUIGridView;

import demo.model.service.DemoDataService;
import demo.model.vo.Test1;
import demo.model.vo.Test1Arg;
import demo.model.vo.Test1a;

@BackingBean("/demo001000")
public class Demo001000 {
    private static final Logger log = LoggerFactory.getLogger(Demo001000.class);
    
    @Resource(name="demoDataService")
    private DemoDataService demoDataService;

	@DefaultAction
	public void init() {
		log.debug("default action called");
	}
	
	@AjaxAction
    public JSONObject queryAction(@Input(name="COL1") Integer param1, @Input(name="col2") String param2,
    		@Input(name="col3") Date param3, @Input(name="col4") String param4,
    		@Input("col5") String[] param5, @Input("col6") String param6, @Input("col7") Integer[] param7,
    		String dummy) {
		
		//查詢條件
		Test1Arg args = new Test1Arg(param1, param2, param3, param4, param5, param6, param7);
		
		//呼叫 service 查詢(先查出全部資料再轉成 grid)
        List<Test1> data = this.demoDataService.findTests(args);
//        List<Test1> data = this.demoDataService.findTests2(args); //for test, no db
        return new TUIGridView<Test1>(data).items(new TUIGridColumnsHandler<Test1>() {
			@Override
			public Object[] generateColumns(int rowIndex, Test1 bean, String rowId) throws Throwable {
				Object[] cols = new Object[8];
                cols[0] = bean.getId();
                
                cols[1] = bean.getCol1();
                cols[2] = bean.getCol2();
                cols[3] = DateUtil.format(bean.getCol3(), "yyyy-MM-dd");
                cols[4] = bean.getCol4();
                cols[5] = StrUtil.join(",", bean.getCol5()); //對應畫面的 checkbox 群組, 多個值用 "," 字元分隔
                cols[6] = bean.getCol6();
                cols[7] = StrUtil.join(",", bean.getCol7()); //對應畫面的多選選單, 多個值用 "," 字元分隔
                return cols;
			}
		});
		
		//以下展示: 以 JSONArray 為輸入 data
		//JSONArray data = this.demoDataService.findTests(args); //得到的事 JSONArray type (list of JSONObject)
		//return new TUIGridView<JSONObject>(data).items(new TUIGridColumnsHandler<JSONObject>() {
		//	@Override
		//	public Object[] generateColumns(int rowIndex, JSONObject bean, String rowId) throws Throwable {
		//		Object[] cols = new Object[9];
		//		cols[0] = bean.get("id");
		//		cols[1] = bean.get("col1");
		//		cols[2] = bean.get("col2");
		//		cols[3] = bean.get("col3");
		//		cols[4] = bean.get("col4");
		//		cols[5] = null;
		//		cols[6] = null;
		//		cols[7] = null;
		//		return cols;
		//	}
		//});
    }
	
	@AjaxAction
	public String queryDetailAction(@Input("pk") Long pk) {
		Test1a data = this.demoDataService.findTestExtraByPK(pk);
		return new JSONObject().put("cola", data.getCola()).put("colb", data.getColb()).put("colc", data.getColc()).toString();
	}
    
    @AjaxAction
    public JSONObject addAction(@Input("COL1") Integer param1, @Input("col2") String param2,
    		@Input("col3") Date param3, @Input("col4") String param4,
    		@Input("col5") String[] param5, @Input("col6") String param6, 
    		@Input("col7") Integer[] param7) {
    	//欲新增的一筆資料
        Test1 vo = new Test1();
        vo.setCol1(param1);
        vo.setCol2(param2);
        vo.setCol3(param3);
        vo.setCol4(param4);
        vo.setCol5(param5);
        vo.setCol6(param6);
        vo.setCol7(param7);

        //呼叫 service 新增一筆
        this.demoDataService.addTest(vo);
//        this.demoDataService.addTest2(vo); //for test, no db
        
        log.debug("新增了一筆: " + vo.toString());
        return new JSONObject().put("pk", vo.getId()).put("COL1", vo.getCol1()).put("col2", vo.getCol2())
        		.put("col3", DateUtil.format(vo.getCol3(), "yyyy-MM-dd")).put("col4", vo.getCol4());
    }
    
    @AjaxAction
    public JSONObject updateAction(@Input("pk") Long pk, 
    		@Input("COL1") Integer param1, @Input("col2") String param2,
    		@Input("col3") Date param3, @Input("col4") String param4,
    		@Input("col5") String[] param5, @Input("col6") String param6, 
    		@Input("col7") Integer[] param7) {
    	//該筆被修改後的資料
        Test1 vo = new Test1();
        vo.setId(pk);
        vo.setCol1(param1);
        vo.setCol2(param2);
        vo.setCol3(param3);
        vo.setCol4(param4);
        vo.setCol5(param5);
        vo.setCol6(param6);
        vo.setCol7(param7);
        
        //呼叫 service 依據 primary key 進行修改動作
    	this.demoDataService.updateTestByPK(vo.getId(), vo);
//        this.demoDataService.updateTestByPK2(vo.getId(), vo); //for test, no db
        log.debug("修改了一筆: " + vo.toString());
        
        return new JSONObject().put("pk", vo.getId()).put("COL1", vo.getCol1()).put("col2", vo.getCol2())
        		.put("col3", DateUtil.format(vo.getCol3(), "yyyy-MM-dd")).put("col4", vo.getCol4()).put("col5", StrUtil.join(",", vo.getCol5()))
        		.put("col6", vo.getCol6()).put("col7", StrUtil.join(",", vo.getCol7()));
    }
    
    @AjaxAction
    public String deleteAction(@Input("pk") Long pk) {
        this.demoDataService.deleteTestByPK(pk);
//    	this.demoDataService.deleteTestByPK2(pk);
    	log.debug("刪除了一筆, primary key of table: " + pk); //for test, no db
        return new JSONObject().put("status", "0").toString();
    }
    
    @Action
    public void directOutputAction(HttpServletResponse response) throws IOException {
    	PrintWriter out = response.getWriter();
    	out.println("測試直接從 response 輸出內容");
    }
}
