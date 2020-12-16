package demo.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.commons.annotation.AjaxAction;
import com.test.commons.annotation.BackingBean;
import com.test.commons.annotation.Input;
import com.test.commons.util.JSONObject;
import com.test.commons.util.MailSender;

@BackingBean("/demo002012")
public class Demo002012 {
	private static final Logger log = LoggerFactory.getLogger(Demo002012.class);
	
	@AjaxAction
	public JSONObject sendAction(@Input("subject") String subject, @Input("from") String from, 
			@Input("to") String[] to, @Input("content") String content, 
			@Input("smtpUser") String smtpUser, @Input("smtpPassword") String smtpPassword, 
			@Input("smtpServer") String smtpServer, @Input("smtpPort") Integer smtpPort) {
		if(smtpServer == null)
			throw new IllegalArgumentException("smtp server not specified");
		MailSender sender = new MailSender(smtpServer);
		sender.setSubject(subject);
		sender.setFrom(from);
		sender.setTos(to);
		sender.setText(content);
		if(smtpUser != null) {
			sender.setUsername(smtpUser);
			sender.setPassword(smtpPassword);
		}
		if(smtpPort != null)
			sender.setPort(smtpPort);
		log.debug("mail object: " + sender);
		
		sender.send();
		return new JSONObject().put("status", "發送完畢");
	}
}
