package appsgate.lig.mail.javamail.connexion;

import javax.mail.MessagingException;
import javax.mail.Session;

import com.sun.mail.imap.IMAPFolder;

public interface JavamailConnexion {
	
	public void start();
	public void stop();
	
	
	/**
	 * Get the Session used by this service, avoid to retrieve a new imap connections
	 * @return
	 */
	public Session getSession();
	
	/**
	 * Gets the imap folder used by this service 
	 * @param boxname
	 * @return
	 * @throws MessagingException
	 */
	public IMAPFolder getMailBox(String boxname) throws MessagingException;	
	
	/**
	 * Use default INBOX
	 * @return
	 * @throws MessagingException
	 */
	public IMAPFolder getMailBox() throws MessagingException;	
	
	
	public String getCurrentUser();
	
	/**
	 * @return return true if the mail configuration is valid and mail can be fetch/send
	 */
	public boolean testConnexion();

}
