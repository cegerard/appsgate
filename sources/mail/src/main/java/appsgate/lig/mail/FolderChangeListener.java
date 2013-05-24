package appsgate.lig.mail;

import javax.mail.Folder;
import javax.mail.Message;

public interface FolderChangeListener {

	public void mailReceivedNotification(Folder folder, Message message);
	
}
