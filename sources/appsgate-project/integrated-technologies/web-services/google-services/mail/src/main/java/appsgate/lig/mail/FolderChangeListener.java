package appsgate.lig.mail;

import javax.mail.Message;

/**
 * Listener pattern interface to notify changed in the mailbox
 * @author jnascimento
 *
 */
public interface FolderChangeListener {

	public void mailReceivedNotification(Message message);

}
