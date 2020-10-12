package jdcs3c000;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.*;
import dcstools.*;
import dcstools.cherry.*;

public class jdcs3c000_01T extends HttpServlet {
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

      Vector list1= new Vector();
      Vector list2= new Vector();
      int tmpRET_NO=0;
      String tmpMSG="";

      jdcs3c000Bean f= new jdcs3c000Bean();
      Enumeration o=request.getParameterNames();
      while (o.hasMoreElements()){
        String tmpNAME=o.nextElement().toString();
        tstr= request.getParameter(tmpNAME);
        f.set(tmpNAME, tstr);
      }
      if(f.get("cat").equals(""))throw new ServletException("請輸入分類號");
      sql="select rowid,tf_type,tf_sno,tf_word,tf_sndt"+
          " from dcs0_to_file"+
          " where tf_psid='"+cs.getUserid()+"'"+
          " and tf_cat="+f.getS("cat")+
          " and (tf_batno is null or tf_batno = '')"+
          " and (tf_date is null or tf_date='')";
      sql+=" order by tf_sno";
      stmt= conn.prepareStatement(sql);
      rs= stmt.executeQuery();
      CutExp c=new CutExp();
      while (rs.next()) {
        jdcs3c000Bean b= new jdcs3c000Bean();
        b.set("rstp", rs.getString(2));
        b.set("sno", rs.getString(3));
        b.set("word", rs.getString(4));
        if(b.get("rstp").equals("發")){
          sql="select om_subj from dcs2_out_mast"+
              " where om_word="+b.getS("word")+
              " and om_sno="+b.getS("sno");
          stmt2= conn.prepareStatement(sql);
          rs2= stmt2.executeQuery();
          if (rs2.next()) {
            b.set("subj", EncodingHelper.convertAstarString(conn, rs2.getString(1), cs.getUserdb()));
          }else b.set("subj","");
          rs2.close();rs2=null;stmt2.close();stmt2=null;
        }else{
          sql="select im_subj from dcs1_in_mast"+
              " where im_grsno="+b.getS("sno");
          stmt2= conn.prepareStatement(sql);
          rs2= stmt2.executeQuery();
          if (rs2.next()) {
            b.set("subj", EncodingHelper.convertAstarString(conn, rs2.getString(1), cs.getUserdb()));
          }else b.set("subj","");
          rs2.close();rs2=null;stmt2.close();stmt2=null;
        }
        
        b.set("rowid",rs.getString("rowid"));
        b.set("name",c.CutSF(rs.getString("tf_type"),7)+c.CutSF(rs.getString("tf_word"),14)+c.CutSF(rs.getString("tf_sno"),11)+b.get("subj"));
        if(f.get("sno").equals(rs.getString("tf_sno"))){
          b.set("sel","selected");
          list1.add(0,b);
        }else{
          b.set("sel","");
          list1.add(b);
        }
      }
      rs.close();rs=null;stmt.close();stmt=null;
      f.set("mod","0");
      String msg = "";
      if("Y".equals(request.getParameter("isOneSno"))){
          msg = "分類號：" + f.get("cat");
      }
      request.setAttribute("tmpMSG", msg);
      request.setAttribute("list", f);
      request.setAttribute("list1", list1);
      conn.close();conn=null;
      String target= "/jdcs3c000/jdcs3c000.jsp";
      RequestDispatcher rd;
      rd= getServletContext().getRequestDispatcher(target);
      rd.forward(request, response);
    } catch (Exception e){
      if(conn!=null){try{conn.close();}catch (Exception ex){} conn=null;}
      String exstr= "javax.servlet.jsp.jspException";
      String target= "/ErrPage.jsp";
      request.setAttribute(exstr, e); request.setAttribute("sql", sql);
      RequestDispatcher rd=getServletContext().getRequestDispatcher(target);
//      rd.forward(request, response);
      if(e instanceof ServletException) throw (ServletException)e; else throw new ServletException(e.getMessage(), e);
    } finally {
      if(rs!=null){try{rs.close();}catch (Exception ex){} rs=null;}
      if(rs2!=null){try{rs2.close();}catch (Exception ex){} rs2=null;}
      if(stmt!=null){try{stmt.close();}catch (Exception ex){} stmt=null;}
      if(stmt2!=null){try{stmt2.close();}catch (Exception ex){} stmt2=null;}
      if(pstmt!=null){try{pstmt.close();}catch (Exception ex){} pstmt=null;}
      if(conn!=null){try{conn.close();}catch (Exception ex){} conn=null;}
    }
  }
  public void destroy() {
  }
}
