package appsgate.lig.mail;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import com.sun.mail.imap.IMAPFolder;

public interface Mail {

	public Session getSession();
	public IMAPFolder getMailBox(String boxname) throws MessagingException;
	public List<Message> getMails();
	public Message getMail(String messageId);
	public boolean sendMailSimple(String to,String subject,String body);
	public boolean sendMail(Message message);
	public void addFolderListener(FolderChangeListener listener);
	public void removeFolderListener(FolderChangeListener listener);
	
}
