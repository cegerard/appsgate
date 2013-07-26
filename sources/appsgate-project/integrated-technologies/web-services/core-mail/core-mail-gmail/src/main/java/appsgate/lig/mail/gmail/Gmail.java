package appsgate.lig.mail.gmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import appsgate.lig.mail.Mail;
import appsgate.lig.mail.apam.message.ApamMessage;

import com.sun.mail.imap.IMAPFolder;

import fr.imag.adele.apam.Apam;

/**
 * Gmail implementation for mail service
 * @author jnascimento
 *
 */
public class Gmail implements Mail {

	
	private Apam apam;
	
	private Logger logger = Logger.getLogger(Gmail.class.getSimpleName());
	private Timer refreshTimer;
	
	/*
	 * Connection information variables
	 */
	private String USER;
	private String PASSWORD;
	
	/*
	 * State variables
	 */
	private Calendar lastFetchDateTime = null;
	private Integer refreshRate = -1;
	private Properties properties;
	private HashMap<String, Message> messagesCached = new HashMap<String, Message>();
	
	/*
	 * IMAP variables
	 */
	private Store store;
	private Session session;
	private IMAPFolder lastIMAPFolder=null;

	private TimerTask refreshtask;

	public Gmail() {
		properties = System.getProperties();
		properties.putAll(GMailConstants.defaultGoogleProperties);

	}

	public void start() {

		try {
			fetch();
		} catch (MessagingException e) {
			throw new RuntimeException("unable to start component. Message "+e.getMessage());
		}

		configureAutoRefreshTask();
		
	}

	public void stop() {
		
		refreshtask.cancel();
		
		release();
		
		if(lastIMAPFolder!=null)
			try {
				lastIMAPFolder.close(false);
			} catch (MessagingException e) {
				// Error closing the mail, nothing you can do about it, sorry
			}
	}
	
	public void release() {

		try {
			getStore().close();
		} catch (MessagingException e) {
			logger.log(Level.WARNING,"failed to release store with the message:"+e.getMessage());
		}

		session = null;
		store = null;

	}
	
	/*
	 * Established the mail connection with the provider
	 */
	private Store getStore() throws MessagingException {

		if (store == null || !store.isConnected()) {
			
				store = getSession().getStore(GMailConstants.PROTOCOL_VALUE);

				store.connect(GMailConstants.IMAP_SERVER, USER, PASSWORD);
		}

		return store;
	}

	private void configureAutoRefreshTask(){
		
		refreshTimer = new Timer();
		
		refreshtask = new TimerTask() {
			
			@Override
			public void run() {
				
				try {
					logger.log(Level.FINE,"Refreshing mail data");
					Gmail.this.fetch();
				} catch (MessagingException e) {
					logger.log(Level.WARNING,"Refreshing mail data FAILED with the message "+e.getMessage());
				}
			}
			
		};
		
		if (refreshRate != null && refreshRate != -1) {
			logger.fine("Configuring auto-refresh to:" + refreshRate + "ms");
			refreshTimer.scheduleAtFixedRate(refreshtask, 0,
					refreshRate.longValue());
		}
		
	}
	
	public Session getSession() {

		if (session == null ) {
			
			//This method will openup a browser (pc/mobile) to verify the user auth in case of auth3
			//session=Session.getDefaultInstance(properties, null);
			
			session = Session.getInstance(properties,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(Gmail.this.USER,
									Gmail.this.PASSWORD);
						}
					});
		}

		return session;
	}

	/*
	 * Check for possible updates in the assigned mail box
	 * @see appsgate.lig.mail.Mail#fetch()
	 */
	public void fetch() throws MessagingException {

		boolean firstTime = messagesCached.size() == 0 ? true : false;
		
		try {

			IMAPFolder box=getMailBox(GMailConstants.DEFAULT_INBOX);
			
			for (Message message : box.getMessages()) {

				String messageId = message.getHeader("message-id")[0];

				boolean isPresent = messagesCached.keySet().contains(messageId);

				if (!isPresent || firstTime ) {
					messagesCached.put(messageId, message);

				}

				if (!isPresent && !firstTime) {
					mailReceivedNotification(message);
					//fireListeners(message);
				}

			}

			lastFetchDateTime=Calendar.getInstance();
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			//release();
		}

	}

	/*
	 * Sends an email with a Message type, which allows to add specialized modification in the mailcontent, like add an attachment. Example of usage:
	 * 
	 *	Message message = new MimeMessage(mailService.getSession());
	 *	message.setFrom(new InternetAddress("from-email@gmail.com"));
	 *	message.setRecipients(Message.RecipientType.TO,
	 *	InternetAddress.parse("jbotnascimento@gmail.com"));
	 *	message.setSubject("Testing sender");
	 *	message.setText("Message body!");
	 *	mailService.sendMail(message);
	 *	mailService.sendMailSimple("jbotnascimento@gmail.com", "ping","ping body");
	 *	mailService.addFolderListener(listener);
	 *	
	 * @see appsgate.lig.mail.Mail#sendMail(javax.mail.Message)
	 */
	public boolean sendMail(Message message) {

		/*
		 * Example
		 */
		
		try {
			Transport.send(message);
		} catch (MessagingException e) {
			return false;
		}

		return true;
	}

	/*
	 * Send an email with a recipient, subject and body 
	 * @see appsgate.lig.mail.Mail#sendMailSimple(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean sendMailSimple(String to, String subject, String body) {

		try {

			Message message = new MimeMessage(getSession());
			message.setFrom(new InternetAddress(USER));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(body);

			sendMail(message);

		} catch (MessagingException e) {
			logger.log(Level.SEVERE, String.format(
					"Impossible to send mail from account %s due to '%s'",
					USER, e.getMessage()));
			return false;
		}

		return true;
	}

	public Message getMail(String messageId) {

		if (messagesCached != null) {
			return messagesCached.get(messageId);
		}

		return null;
	}

	public List<Message> getMails(int size){
		try {

			IMAPFolder folder = getMailBox(GMailConstants.DEFAULT_INBOX);

			Message[] messages;
			
			if(size==0)
				messages = folder.getMessages();
			else
				messages = folder.getMessages(folder.getMessageCount()-size+1,folder.getMessageCount());

			List<Message> imapMessages=Arrays.asList(messages);
			
			Collections.reverse(imapMessages);
			
			return imapMessages;

		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return new ArrayList<Message>();
	}
	
	public List<Message> getMails() {

		return getMails(0);

	}

	public IMAPFolder getMailBox(String storeString) throws MessagingException {

		if(lastIMAPFolder!=null && lastIMAPFolder.getName().equals(storeString)){
			return lastIMAPFolder;
		}else if (lastIMAPFolder!=null){
			lastIMAPFolder.close(false);
		}
			
		lastIMAPFolder = (IMAPFolder) getStore().getFolder(storeString);

		if (!lastIMAPFolder.isOpen())
			lastIMAPFolder.open(Folder.READ_ONLY);//READ_WRITE

		return lastIMAPFolder;

	}

	private ApamMessage mailReceivedNotification(Message msg){
		return new ApamMessage(msg);
	}
	
	public Calendar getLastFetchDateTime() {
		return lastFetchDateTime;
	}
	
	public void autoRefreshValueChanged(String newValue) {
		
		refreshtask.cancel();
		
		System.out.println("Auto-refresh changed to:"+refreshRate);
		
		configureAutoRefreshTask();
		
	}
	
}
