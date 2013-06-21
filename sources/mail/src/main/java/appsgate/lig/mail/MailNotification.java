package appsgate.lig.mail;

import javax.mail.Message;

/**
 * Interface for receiving mail notification messages
 * @author jnascimento
 *
 */
public class MailNotification {

	Message msg;
	
	public MailNotification(Message msg){
		this.msg=msg;
	}

	public Message getMessage() {
		return msg;
	}
	
}
