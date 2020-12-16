package demo.view;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tatung.commons.annotation.Action;
import com.tatung.commons.annotation.BackingBean;
import com.tatung.commons.annotation.Input;

@BackingBean("/demo00101101")
public class Demo00101101 {
	private static final Logger log = LoggerFactory.getLogger(Demo00101101.class);
	
	@Action
	public void init(HttpServletRequest request, @Input("value1") String value1) {
		request.setAttribute("value1", value1);
	}
}
