package appsgate.lig.mail;

import java.util.List;

import javax.mail.Message;


import com.sun.mail.imap.IMAPFolder;

public interface Mail {

	public boolean sendMail(Message message);
	public Message getMail(String messageId);
	public List<Message> getMails();
	public IMAPFolder getMailBox(String boxname);
	public void addFolderListener(FolderChangeListener listener);
	
}
