package jdcs3c000;

import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.sql.*;
import dcstools.*;
import dcstools.cherry.*;
import dcstools.chunwei.*;
import tools.DateUtil;
import xprint.*;

public class jdcs3c000_05T extends HttpServlet {
  private static final Log log = LogFactory.getLog(jdcs3c000_05T.class);
  private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request,response);
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Connection conn= null;
    Statement pstmt= null;
    PreparedStatement stmt= null;
    PreparedStatement stmt2= null;
    ResultSet rs= null;
    ResultSet rs2= null;
    response.setContentType(CONTENT_TYPE);String sql="",tstr="";
    
    try {
      dcstools.ConnSession cs= new dcstools.ConnSession(request, "jdcs30000");
      conn= ConnPool.getInstance().getConnection();
      String cmd= "use " + cs.getUserdb();
      pstmt= conn.createStatement();
      pstmt.executeUpdate(cmd);

      jdcs3c000Bean f= new jdcs3c000Bean();
      Enumeration o=request.getParameterNames();
      while (o.hasMoreElements()){
        String tmpNAME=o.nextElement().toString();
        tstr= request.getParameter(tmpNAME);
        f.set(tmpNAME, tstr);
      }
      String sys_time="",sys_no="";
      DateToCdate st=new DateToCdate();
      
      sql="select getdate() from dcs0_org_par";
      log.debug("sql=" + sql);
      stmt= conn.prepareStatement(sql);
      rs= stmt.executeQuery();
      if (rs.next()) {
        sys_time=st.DateToCdate(rs.getString(1));
        sys_no=st.getNo();
      }
      rs.close();rs=null;stmt.close();stmt=null;
      
      sql="select ct_flmyr from dcs0_cat where ct_cat="+f.getS("cat");
      log.debug("sql=" + sql);
      stmt= conn.prepareStatement(sql);
      rs= stmt.executeQuery();
      if(rs.next()){
        ToFlmyr fy=new ToFlmyr(rs.getString("ct_flmyr"));
        f.set("flmyr", fy.getFlmyr());
      }
      rs.close();rs=null;stmt.close();stmt=null;
      
      sql="select distinct tf_type,tf_sno,tf_word, convert(date, tf_sndt),tf_secur,im_subj,dp_sname,ps_name,tf_type, convert(date, tf_date),tf_sndt,tf_cat"+
          " from dcs0_to_file " +
          " join dcs1_in_mast on tf_sno=im_grsno " +
          " left outer join dcs0_dept on tf_dpid=dp_id " +
          " left outer join dcs0_personel on tf_psid=ps_id " +
          " where tf_psid='"+cs.getUserid()+"'"+
          " and (tf_batno is null or tf_batno = '')"+
          " and (tf_date is null or tf_date='')"+
          " and tf_type='收'";
      if(!f.get("cat").equals("")) sql+=" and tf_cat ="+f.getS("cat");
      sql+=" union " +
      	  "select distinct tf_type,tf_sno,tf_word, convert(date, tf_sndt),tf_secur,om_subj,dp_sname,ps_name,tf_type, convert(date, tf_date),tf_sndt,tf_cat"+
          " from dcs0_to_file " +
          " join dcs2_out_mast on (tf_word=om_word and tf_sno=om_sno) " +
          " left outer join dcs0_dept on tf_dpid=dp_id " +
          " left outer join dcs0_personel on tf_psid=ps_id " +
          " where tf_psid='"+cs.getUserid()+"'"+
          " and (tf_batno is null or tf_batno = '')"+
          " and (tf_date is null or tf_date='')"+
          " and tf_type='發'";
      if(!f.get("cat").equals("")) sql+=" and tf_cat ="+f.getS("cat");
      sql+=" order by 7,12,11,1,2";
      log.debug("sql=" + sql);
      stmt= conn.prepareStatement(sql);
      rs= stmt.executeQuery();
      int row=-1;
      int h = 8;
      int page = 0;

      Vector list = new Vector();
      DateToCdate dt=new DateToCdate();
      String cat="",dpid="";
      int page_tol=0;
      int no=0;
      while (rs.next()) {
        jdcs3c000Bean b= new jdcs3c000Bean();
        b.set("rstp", rs.getString(1));
        b.set("sno", rs.getString(2));
        b.set("word", rs.getString(3));
        b.set("sndt", dt.DateToCdate(rs.getString(4)));
        b.set("secur", rs.getString(5));
        b.set("subj", rs.getString(6));
        b.set("dpid", rs.getString(7));
        b.set("psid", rs.getString(8));
        b.set("rstp", rs.getString(9));
        b.set("tfdt", dt.DateToCdate(rs.getString(10)));
        b.set("cat", rs.getString(12));
        list.add(b);
        if(row>=16||row==-1||!cat.equals(b.get("cat"))||!dpid.equals(b.get("dpid"))){
          page_tol++;
          row=0;
        }
        cat=b.get("cat");
        dpid=b.get("dpid");
        row++;
        no++;
      }
      rs.close();rs=null;stmt.close();stmt=null;
      
      if(no==0) throw new Exception("無相關資料可供列印");

      final String filename = "jdcs3c000_05-" + DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + ".pdf";
      try(XprintPDF print_3c000 = new XprintPDF("jdcs3c002.txt",filename);){
          int rno=0;
          row=-1;
          Iterator it= list.iterator();
          jdcs3c000Bean b2= new jdcs3c000Bean();
          cat="";
          dpid="";
          while (it.hasNext()) {
            b2=(jdcs3c000Bean) it.next();
            if(row==-1||row>=16||!cat.equals(b2.get("cat"))||!dpid.equals(b2.get("dpid"))){
              print_3c000.newPage();
              print_3c000.add("分類號",b2.get("cat"),0,0);
              print_3c000.add("承辦單位",b2.get("dpid"),0,0);
              print_3c000.add("承辦人員",b2.get("psid"),0,0);
              print_3c000.add("共幾頁",Integer.toString(page_tol),0,0);
              print_3c000.add("第幾頁",Integer.toString(++page),0,0);
              print_3c000.add("製表時間",sys_time,0,0);
              print_3c000.add("標題",cs.getOrgname()+"待歸檔清單",0,0);
              row=0;
            }
            cat=b2.get("cat");
            dpid=b2.get("dpid");
            print_3c000.add("序號",""+(++rno),0,row*h);
            if(b2.get("rstp").equals("收"))
              print_3c000.add("文號",b2.get("sno"),0,row*h);
            else{
              print_3c000.add("文號", b2.get("sno"), 25, row * h);
              
              sql="select om_grsno from dcs2_out_mast"+
                  " where om_word="+b2.getS("word")+
                  " and om_sno="+b2.getS("sno");
              log.debug("sql=" + sql);
              stmt= conn.prepareStatement(sql);
              rs= stmt.executeQuery();
              if (rs.next()) {
                b2.set("grsno", rs.getString(1));
                print_3c000.add("文號","("+b2.get("grsno")+")",0,row*h);
              }
              rs.close();rs=null;stmt.close();stmt=null;
            }
            ToSecur s=new ToSecur(b2.get("secur"));
            print_3c000.add("密等",s.getSecur(),0,row*h);
            print_3c000.add("主旨",b2.get("subj"),0,row*h);
            print_3c000.add("辦結日期",b2.get("sndt"),0,row*h);
            row++;
          }
          print_3c000.endFile();
      }
      
      request.setAttribute("filename", filename);
      conn.close();conn=null;
      String target= "/jdcsrptlink.jsp";
      RequestDispatcher rd;
      rd= getServletContext().getRequestDispatcher(target);
      rd.forward(request, response);
    } catch (Exception e){
      String exstr= "javax.servlet.jsp.jspException";
      String target= "/ErrPage.jsp";
      request.setAttribute(exstr, e); request.setAttribute("sql", sql);
      RequestDispatcher rd=getServletContext().getRequestDispatcher(target);
//      rd.forward(request, response);
      if(e instanceof ServletException) throw (ServletException)e; else throw new ServletException(e.getMessage(), e);
    } finally {
      if(rs!=null){try{rs.close();}catch (Exception ex){} rs=null;}
      if(rs2!=null){try{rs2.close();}catch (Exception ex){} rs2=null;}
      if(stmt2!=null){try{stmt2.close();}catch (Exception ex){} stmt2=null;}
      if(stmt!=null){try{stmt.close();}catch (Exception ex){} stmt=null;}
      if(pstmt!=null){try{pstmt.close();}catch (Exception ex){} pstmt=null;}
      if(conn!=null){try{conn.close();}catch (Exception ex){} conn=null;}
    }
  }
  public void destroy() {
  }
}
