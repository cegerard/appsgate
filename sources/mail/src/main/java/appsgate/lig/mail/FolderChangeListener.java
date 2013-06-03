package appsgate.lig.mail;

import javax.mail.Message;

public interface FolderChangeListener {

	public void mailReceivedNotification(Message message);

}
