package course.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/*多用途網際網路郵件擴展(MIME,Multipurpose Internet Mil Extensions)
  JAVA EE6使用了新的機制簡化使用multipart表單上傳資料的麻煩, 主要使用3個元件
  1.javax.servlet.http.HttpServletRequest新增getPart(String)可以取得表單上傳的部分內容
  2.javax.servlet.annotation.MultipartConfig提供多種屬性:
  @MultipartConfig(location="F:/JAVA_C/uploaded",maxFileSize=1024*1024*200)
  
  	location:檔案儲存位置，可搭配Part的write()方法，Part p2 = request.getPart("data");p2.write(getFileName(p2));
  	maxFileSize:檔案大小上限
 */
@WebServlet("/UploadServlet")
@MultipartConfig(location="F:/JAVA_C/uploaded",maxFileSize=1024*1024*200)
public class UploadServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("UTF-8");				
		Part p1 = request.getPart("desc");//input的name
		BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
		String desc = br.readLine();
		request.setAttribute("desc",desc);
		
		//Part指定名稱取出瀏覽器表單上傳的「部分內容(part)」，一般文字或檔案皆可
		//Parts將表單所有內容以集合物件的形式取得，支援迭代(iterate)取出所有成員
		Part p2 = request.getPart("data");
		p2.write(getFileName(p2));
		
		RequestDispatcher rd = request.getRequestDispatcher("/acknowledge.jsp");
		rd.forward(request, response);
		
		
	}
	
	private String getFileName(final Part part) {
		for(String content : part.getHeader("content-disposition").split(";")) {
			if(content.trim().startsWith("filename")) {
				Path filePath = Paths.get(content.substring(content.indexOf('=')+1).trim().replace("\"", ""));
				return filePath.getFileName().toString();
			}
		}
		return null;
	}

}
