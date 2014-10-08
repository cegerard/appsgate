package appsgate.lig.mail;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.json.JSONArray;

import com.sun.mail.imap.IMAPFolder;

/**
 * Main mail interface to access the mailbox
 * @author jnascimento
 *
 */
public interface Mail {

	/**
	 * Force email default inbox to be checked 
	 * @throws MessagingException
	 */
	public void fetch() throws MessagingException;
	
	/**
	 * Return the Calendar from the last fetch done
	 * @return
	 */
	public Calendar getLastFetchDateTime();
	
	/**
	 * Return all messages in the default inbox
	 * @return
	 */
	public List<Message> getMails();
	
	/**
	 * Return a given number (size) of Messages stored in the default inbox
	 * @param size number of messages to be returned 
	 * @return
	 */
	public List<Message> getMails(int size);
	
	/**
	 * Retrieve a imap message based on its id
	 * @param messageId
	 * @return
	 */
	public Message getMail(String messageId);
	
	/**
	 * Sends a mail from the account in which the the service is connected to
	 * @param to
	 * @param subject
	 * @param body
	 * @return
	 */
	public boolean sendMailSimple(String to,String subject,String body);
	
	/**
	 * Send mail based on Message class from javax.mail API
	 * @param message
	 * @return
	 */
	public boolean sendMail(Message message);
	
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
	 * Add a mail of recipient to our favorites
	 * @param recipientMail a valid mail
	 * @return true if the mail was successfully added
	 */
	public boolean addFavoriteRecipient(String recipientMail);

	/**
	 * Try to remove a mail of recipient from our favorites
	 * @param recipientMail a valid mail
	 * @return true if the mail was existing and successfully removed
	 */
	public boolean removeFavoriteRecipient(String recipientMail);
	
	/**
	 * Retrieves the full list of recipients
	 * @return the current list of recipients
	 */	
	public JSONArray getFavoriteRecipients();
	
}
