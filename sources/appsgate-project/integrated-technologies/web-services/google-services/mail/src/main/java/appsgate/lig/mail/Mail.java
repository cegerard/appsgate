package appsgate.lig.mail;

import java.util.Calendar;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import com.sun.mail.imap.IMAPFolder;

/**
 * Main mail interface to access the mailbox
 * @author jnascimento
 *
 */
public interface Mail {

	public Session getSession();
	public IMAPFolder getMailBox(String boxname) throws MessagingException;
	public void fetch() throws MessagingException;
	public List<Message> getMails();
	public List<Message> getMails(int size);
	public Message getMail(String messageId);
	public boolean sendMailSimple(String to,String subject,String body);
	public boolean sendMail(Message message);
	public void addFolderListener(FolderChangeListener listener);
	public void removeFolderListener(FolderChangeListener listener);
	public Calendar getLastFetchDateTime();
	
}
