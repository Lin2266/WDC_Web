package jdcs3c000;

import static tools.SqlAssembler.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import beans.BatMst;
import dcstools.Account;
import dcstools.ConnPool;
import tools.BeanRowMapper;
import tools.DBUtil;
import tools.DateUtil;
import tools.JSONObject;
import tools.SqlAssembler;
import tools.StrUtil;
import tools.annotation.Action;
import tools.annotation.Param;
import tools.ui.TUIGridColumnsHandler;
import tools.ui.TUIGridView;

public class jdcs3c000_04T extends HttpServlet {
	private static final Log log = LogFactory.getLog(jdcs3c000_04T.class);
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	@Action
	public String query(HttpServletRequest request, @Param("isOneSno") String isOneSno, 
			@Param("refg") String refg, @Param("cat") String cat, @Param("batno") String batno, @Param("subj") String subj) throws SQLException {
		final Account account = Account.getCurrentAccount(request);
		final String dbname = account.getUserDb();
		
		List<BatMst> data = null;
		try (Connection conn = ConnPool.getInstance().getConnection()) {
			SqlAssembler sql = new SqlAssembler("select * from " + dbname + "..dcs3_bat_mst");
			SqlAssembler.Condition where = eq("bm_psid", account.getUserId());
			if("1".equals(refg)) { //退件未收
				where.and(eq("bm_refg", refg));
			} else { //修改查詢
				where.and(eqNN("bm_cat", cat)).and(eqNN("bm_batno", batno)).and(eqNN("bm_subj", subj));
			}
			sql.where(where).raw("order by bm_batno desc");
			log.debug("sql=" + sql);
			data = DBUtil.query(conn, sql.getSql(), new BeanRowMapper<>(BatMst.class), sql.getValues());
		}
		
		if(data.size() == 0) {
			request.setAttribute("tmpMSG", "查無相關資料");
			return "/jdcs3c000/jdcs3c000.jsp";
		}
		
		if(data.size() == 1) {
			String msg = StrUtil.print("處理狀態：", data.get(0).getRefgTxt());
			if("Y".equals(isOneSno)){
				msg = "分類號：" + cat;
			}
			request.setAttribute("tmpMSG", msg);
			return "/servlet/jdcs3c000.jdcs3c000_03T?batno=" + data.get(0).getBatno();
		}
		
		final JSONObject gridData = new TUIGridView<BatMst>(data).items(new TUIGridColumnsHandler<BatMst>() {
			@Override public Object[] generateColumns(int rowIndex, BatMst vo, String gridRowid) throws Exception {
				final Object[] cols = new Object[8];
				cols[0] = vo.getRowid();
				cols[1] = vo.getRefg();

				cols[2] = vo.getBatno();
				cols[3] = vo.getCat();
				cols[4] = vo.getSubj();
				cols[5] = DateUtil.formatTW(vo.getDate(), "yyy/MM/dd HH:mm:ss");
				cols[6] = DateUtil.formatTW(vo.getRdate(), "yyy/MM/dd HH:mm:ss");
				cols[7] = vo.getRefgTxt();
				return cols;
			}
		});
		
		request.setAttribute("gridData", gridData.toString());
		return "/jdcs3c000/jdcs3c000frame.jsp";
	}
}
