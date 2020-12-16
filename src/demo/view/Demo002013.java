package demo.view;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.util.FileUtil;
import com.test.commons.util.JspUtil;
import com.test.commons.util.OutputStreamHandler;

@BackingBean("/demo002013")
public class Demo002013 {
	private static final Logger log = LoggerFactory.getLogger(Demo002013.class);
	
	@AjaxAction
	public void downloadAfile2(HttpServletResponse response) throws IOException {
		final File file = new File(JspUtil.getContextRealPath(), "/images/tatung.gif");
		final String filename = "大同.gif";
		
		//JspUtil.clientDownload(response, file, filename);
		//或:
		JspUtil.clientDownload(response, new OutputStreamHandler() {
			@Override public long execute(OutputStream out) throws IOException {
				return FileUtil.dump(file, out);
			}}, filename, null);
	}
	
	@AjaxAction
	public void openAfile2(HttpServletResponse response) throws IOException {
		final File file = new File(JspUtil.getContextRealPath(), "/images/tatung.gif");
		final String filename = "大同.gif";
		
		JspUtil.clientDownloadAndOpen(response, file, filename, "image/gif");
	}
}
