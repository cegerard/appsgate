package appsgate.lig.mail.apam.message;

import javax.mail.Message;

/**
 * Interface for receiving mail notification messages
 * 
 * @author jnascimento
 * 
 */
public class ApamMessage {

	Message msg;

	public ApamMessage(Message msg) {
		this.msg = msg;
	}

	public Message getMessage() {
		return msg;
	}

}
