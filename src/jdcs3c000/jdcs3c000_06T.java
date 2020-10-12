package jdcs3c000;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.*;
import dcstools.*;
import tools.JSONObject;
import tools.StrUtil;

public class jdcs3c000_06T extends HttpServlet {
	private static final Log log = LogFactory.getLog(jdcs3c000_06T.class);
	private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			final String action = StrUtil.print(request.getParameter("action"));
			if("QUERY_TO_FILE_DATA".equals(action)) { 
				queryToFileData(request, response);
			} else {
				throw new IllegalArgumentException("unknown action");
			}
		} catch(Throwable t) {
			throw new ServletException(t.getMessage(), t);
		}
	}
	
	public void queryToFileData(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		final Account account = Account.getCurrentAccount(request);
		final String dbname = account.getUserDb();

		int count = 0;
		String batno = "";
		String cat = "";
		
		String sno = StrUtil.print(request.getParameter("sno"));
		if("".equals(sno)){
			count = -1;//未輸入文號
		}else{
			String sql = "select tf_batno, tf_cat"
					+ " from " + dbname + "..dcs0_to_file"
					+ " where tf_psid=? and tf_sno=?";
			log.debug("sql=" + sql);
			try (Connection conn = ConnPool.getInstance().getConnection();
					PreparedStatement pstmt = conn.prepareStatement(sql);){
				pstmt.setString(1, account.getUserId());
				pstmt.setString(2, sno);
				try (ResultSet rs = pstmt.executeQuery()){
					if(rs.next()){
						count = 1;
						batno = StrUtil.print(rs.getString("tf_batno"));
						cat = StrUtil.print(rs.getString("tf_cat"));
					}else{
						count = 0;
					}
				}
			}
		}
		
		response.setContentType(CONTENT_TYPE);
		response.getWriter().write(new JSONObject().put("count", count).put("batno", batno).put("cat", cat).toString()); //AJAX response
	}
	
	public void destroy() {
	}
}
