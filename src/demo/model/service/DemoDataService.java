package demo.model.service;

import static com.test.commons.util.SqlAssembler.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import javax.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.test.commons.spring.BeanRowMapper;
import com.test.commons.util.DBSequenceUtil;
import com.test.commons.util.DBUtil;
import com.test.commons.util.DateUtil;
import com.test.commons.util.JSONArray;
import com.test.commons.util.JSONObject;
import com.test.commons.util.SqlAssembler;

import demo.model.vo.Test1;
import demo.model.vo.Test1Arg;
import demo.model.vo.Test1a;

/**
 * 提供測試資料的服務.
 * <p>
 * 測試 db table (以 PostgreSQL 為例, 他種 DB 需視實際狀況調整資料型態):
 * <pre><code> create sequence demo_test1_seq;
 * create table demo_test1 (
 *   id_ bigint primary key,
 *   col1 integer,
 *   col2 varchar(20),
 *   col3 timestamp,
 *   col4 varchar(30)
 * );
 * </code></pre>
 */
@Service("demoDataService")
public class DemoDataService {
    private static final Logger log = LoggerFactory.getLogger(DemoDataService.class);
    
    @Resource(name="jdbcTemplate")
    private JdbcTemplate jdbcTemplate;
    
    /** 依不定的條件, 查詢資料 */
    public List<Test1> findTests(Test1Arg args) {
        //不定條件的查詢
    	SqlAssembler sql = new SqlAssembler("select * from demo_test1").where(
    			eqNN("col1", args.getCol1()) //eqNN(): 如果 args.getCol1() 為 null, 則略去此條件
    			.and(eqNN("col2", args.getCol2())) //如果使用 eq(), 當 args.getCol2() 為 null 時, 將得到 "col2 is null" 條件
    			//.and(conditionNN("date_trunc('day', col3)=?", DateUtil.truncate(args.getCol3(), DateUtil.DATE))) //精確至日 (不好: 動用 db 專屬 function)
    			.and(gtEqNN("col3", DateUtil.getDateLeftBoundForDay(args.getCol3()))) //TIMESTAMP: col3 >= 當天 00:00:00.000
    			.and(ltNN("col3", DateUtil.getDateRightBoundForDay(args.getCol3()))) //TIMESTAMP: col3 < 隔天 00:00:00.000
    			.and(eqNN("col4", args.getCol4())) //col4 以後的欄位條件暫略不用
			).raw("order by id_");
    	log.debug(sql.toString());
        return this.jdbcTemplate.query(sql.getSql(), sql.getValues(), new BeanRowMapper<Test1>(Test1.class));
    	//或如下, 使用標準 JDBC API + DBUtil 工具執行查詢
    	//try (Connection conn = this.jdbcTemplate.getDataSource().getConnection()) { //此寫法適用於 JDK 1.7+
    	//	try (PreparedStatement pstmt = conn.prepareStatement(sql.getSql())) {
    	//		return DBUtil.query(pstmt, Test1.class, sql.getValues());
    	//	} //出了這個區塊, JVM 會自動 pstmt.close()
    	//} //出了這個區塊, JVM 會自動 conn.close()
    }
    
    /** 查詢, 傳回假資料, 不使用 db (方便 demo ap 移轉)。 */
    public List<Test1> findTests2(Test1Arg args) {
    	return fakeData();
    }
    
    /** 根據 demo_test 的 pk 查詢該筆額外的資料 */
    public Test1a findTestExtraByPK(Long pk) {
    	//這裡只是傳回個假資料
    	Test1a vo = new Test1a();
    	vo.setCola("test detail a");
    	vo.setColb("測試父表 b");
    	vo.setColc("zzz");
		return vo;
    }
    
    /** 新增一筆資料. */
    @Transactional
    public void addTest(Test1 vo) {
        //先求本筆資料之 id_ 序號
        vo.setId(DBSequenceUtil.nextKey("demo_test1_seq"));
        
        String sql = "insert into demo_test1 (id_, col1, col2, col3, col4) values (?,?,?,?,?)";
        log.debug("sql=" + sql + "; args={},{},{},{},{}", vo.getId(), vo.getCol1(), vo.getCol2(), vo.getCol3(), vo.getCol4());
        int n = this.jdbcTemplate.update(sql, vo.getId(), vo.getCol1(), vo.getCol2(), vo.getCol3(), vo.getCol4());
        
        if(n == 0)
        	throw new IllegalStateException("新增失敗");
    }
    
    /** 假裝新增一筆資料, 不使用 db (方便 demo ap 移轉). */
    public void addTest2(Test1 vo) {
    	vo.setId(System.currentTimeMillis()); //暫不存入 DB, 新 pk 以系統時刻暫代
    }
    
    /** 根據 PK 修改一筆資料. */
    @Transactional
    public void updateTestByPK(Long id, Test1 vo) {
        if(id == null)
            throw new IllegalArgumentException("資料 id 未指定");
        String sql = "update demo_test1 set col1=?, col2=?, col3=?, col4=? where id_=?";
        log.debug("sql=" + sql + "; args={},{},{},{},{}", vo.getCol1(), vo.getCol2(), vo.getCol3(), vo.getCol4(), id);
        int n = this.jdbcTemplate.update(sql, vo.getCol1(), vo.getCol2(), vo.getCol3(), vo.getCol4(), id);
        
        if(n == 0)
        	throw new IllegalStateException("修改失敗");
    }
    
    /** 假裝修改一筆資料, 不使用 db (方便 demo ap 移轉). */
    public void updateTestByPK2(Long id, Test1 vo) {
    	//啥都沒做
    }
    
    /** 根據 PK 刪除一筆資料. */
    @Transactional
    public void deleteTestByPK(Long id) {
        if(id == null)
            throw new IllegalArgumentException("資料 id 未指定");
        String sql = "delete from demo_test1 where id_=?";
        log.debug("sql=" + sql + "; args={}", id);
        int n = this.jdbcTemplate.update(sql, id);
        
        if(n == 0)
        	throw new IllegalStateException("刪除失敗");
    }
    
    /** 假裝刪除一筆資料, 不使用 db (方便 demo ap 移轉). */
    public void deleteTestByPK2(Long id) {
    	//啥都沒做
    }
    
    public JSONArray findDummyMembers() {
    	JSONArray ret = new JSONArray();
    	ret.add(new JSONObject().put("id", "test_id_1").put("name", "名字一").put("ip", "192.168.0.2").put("birth_year", 1990).put("isAlive", true));
    	ret.add(new JSONObject().put("id", "test_id_2").put("name", "名字二").put("ip", "192.168.0.3").put("birth_year", 1992).put("isAlive", false));
    	ret.add(new JSONObject().put("id", "test_id_3").put("name", "名字三").put("ip", (String)null).put("birth_year", 1993).put("isAlive", true));
    	ret.add(new JSONObject().put("id", "test_id_4").put("name", "名字四").put("ip", "192.168.0.4").put("birth_year", 1994).put("isAlive", true));
    	ret.add(new JSONObject().put("id", "test_id_5").put("name", "名字五").put("ip", "192.168.0.5").put("birth_year", 1995).put("isAlive", true));
    	return ret;
    }
    
    public JSONObject findDummyMemberById(String id) {
    	JSONArray all = findDummyMembers();
    	for(Iterator<Object> i = all.iterator(); i.hasNext(); ) {
    		JSONObject member = (JSONObject)i.next();
    		if(member.getAsString("id").equals(id))
    			return member;
    	}
    	return null;
    }
    
    public String findDummyMemberAsXmlById(String id) {
    	JSONArray all = findDummyMembers();
    	for(Iterator<Object> i = all.iterator(); i.hasNext(); ) {
    		JSONObject member = (JSONObject)i.next();
    		if(member.getAsString("id").equals(id))
    			return dummyMemberJsonToXml(member);
    	}
    	return null;
    }
    
    //for test
    String dummyMemberJsonToXml(JSONObject m) {
    	StringBuilder ret = new StringBuilder();
    	ret.append("<member>")
    		.append("<id>").append(m.getAsString("id")).append("</id>")
    		.append("<name>").append(m.getAsString("name")).append("</name>")
    		.append("<ip>").append(m.getAsString("ip")).append("</ip>")
    		.append("<birth_year>").append(m.getAsString("birth_year")).append("</birth_year>")
    		.append("<isAlive>").append(m.getAsString("isAlive")).append("</isAlive>")
    		.append("</member>");
    	return ret.toString();
    }
    
    //for test
    List<Test1> fakeData() {
    	List<Test1> data = new ArrayList<Test1>();
    	data.add(new Test1(0L, 0, "test1", new Date(), "測試一", new String[] { "a", "b" }, "x", new Integer[] { 1, 2 }));
    	data.add(new Test1(1L, 1, "test2", new Date(), "測試二", new String[] { "c", "d" }, "y", new Integer[] { 3, 4 }));
    	data.add(new Test1(2L, 2, "test3", new Date(), "測試三", new String[] { "a", "c" }, "z", new Integer[] { 1, 3 }));
    	data.add(new Test1(3L, 3, "test4", new Date(), "測試四", new String[] { "b", "d" }, "x", new Integer[] { 2, 4 }));
    	data.add(new Test1(4L, 4, "test5", new Date(), "測試五", new String[] { "a", "d" }, "y", new Integer[] { 4, 3 }));
    	data.add(new Test1(5L, 5, "test6", new Date(), "測試六", new String[] { "b", "c" }, "x", new Integer[] { 2, 1 }));
    	data.add(new Test1(6L, 6, "test7", new Date(), "測試七", new String[] { "a", "b" }, "z", new Integer[] { 4, 1 }));
    	data.add(new Test1(7L, 7, "test8", new Date(), "測試八", new String[] { "c", "d" }, "x", new Integer[] { 2, 3 }));
    	data.add(new Test1(8L, 8, "test9", new Date(), "測試九", new String[] { "a", "c" }, "y", new Integer[] { 1, 4 }));
    	data.add(new Test1(9L, 9, "test10", new Date(), "測試十", new String[] { "b", "d" }, "z", new Integer[] { 2, 3 }));
    	data.add(new Test1(10L, 10, "test11", new Date(), "測試十一", new String[] { "a", "b" }, "x", new Integer[] { 1, 2 }));
    	data.add(new Test1(11L, 11, "test12", new Date(), "測試十二", new String[] { "c", "d" }, "y", new Integer[] { 1, 3 }));
    	data.add(new Test1(12L, 12, "test13", new Date(), "測試十三", new String[] { "a", "d" }, "z", new Integer[] { 1, 4 }));
    	
    	//for(int i = 0; i < 10000; i++) 
    	//	data.add(new Test1((long)i, i, "test" + i, new Date(), "測試測試測試測試測試測試測試測試測試" + i, new String[] { "a", "b" }, "z", new int[] { 1, 4 }));
    	
    	return data;
    }
}
