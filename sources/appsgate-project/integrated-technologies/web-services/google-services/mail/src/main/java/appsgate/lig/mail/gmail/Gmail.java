package appsgate.lig.mail.gmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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

import appsgate.lig.mail.FolderChangeListener;
import appsgate.lig.mail.Mail;
import appsgate.lig.mail.MailNotification;

import com.sun.mail.imap.IMAPFolder;

/**
 * Gmail implementation for mail service
 * @author jnascimento
 *
 */
public class Gmail implements Mail {

	private Logger logger = Logger.getLogger(Gmail.class.getSimpleName());
	private Timer refreshTimer = new Timer();
	private Calendar lastFetchDateTime = null;

	private Store store;
	private Session session;
	private Properties properties;
	
	private String USER;
	private String PASSWORD;
	private Integer refreshRate = -1;
	
	//Last list of messages cached (so we can findout when there are new messages in the mailbox)
	private HashMap<String, Message> messagesCached = new HashMap<String, Message>();
	
	private Set<FolderChangeListener> folderListener = new HashSet<FolderChangeListener>();

	private final String BOX = "inbox";
	private final String PROTOCOL_KEY = "mail.store.protocol";
	private final String PROTOCOL_VALUE = "imaps";
	private final String IMAP_SERVER = "imap.googlemail.com";

	private TimerTask refreshtask = new TimerTask() {
		
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
	
	private Map<String, String> defaultGoogleProperties = new HashMap<String, String>() {
		{
			put("mail.smtp.auth", "true");
			put("mail.smtp.starttls.enable", "true");
			put("mail.smtp.host", "smtp.gmail.com");
			put("mail.smtp.port", "587");
			put(PROTOCOL_KEY, PROTOCOL_VALUE);
		}
	};

	public Gmail() {
		properties = System.getProperties();
		properties.setProperty(PROTOCOL_KEY, PROTOCOL_VALUE);
		properties.putAll(defaultGoogleProperties);

	}

	public void start() {

		try {
			fetch();
		} catch (MessagingException e) {
			throw new RuntimeException("unable to start component. Message "+e.getMessage());
		}

		if (refreshRate != null && refreshRate != -1) {
			logger.fine("Configuring auto-refresh to:" + refreshRate + "ms");
			refreshTimer.scheduleAtFixedRate(refreshtask, 0,
					refreshRate.longValue());
		}

	}

	public void stop() {
		release();
		refreshtask.cancel();
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
			
				store = getSession().getStore(PROTOCOL_VALUE);

				store.connect(IMAP_SERVER, USER, PASSWORD);
		}

		return store;
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

			for (Message message : getMailBox(BOX).getMessages()) {

				String messageId = message.getHeader("message-id")[0];

				boolean isPresent = messagesCached.keySet().contains(messageId);

				if (!isPresent || firstTime) {
					messagesCached.put(messageId, message);

				}

				if (!isPresent && !firstTime) {
					fireListeners(message);
				}

			}

			lastFetchDateTime=Calendar.getInstance();
			
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			release();
		}

	}

	/*
	 * Sends an email with a Message type, which allows to add specialized modification in the mailcontent, like add an attachment
	 * @see appsgate.lig.mail.Mail#sendMail(javax.mail.Message)
	 */
	public boolean sendMail(Message message) {

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

	public List<Message> getMails() {

		try {

			IMAPFolder folder = getMailBox(BOX);

			Message[] messages = folder.getMessages();

			return Arrays.asList(messages);

		} catch (MessagingException e) {
			e.printStackTrace();
		}

		return new ArrayList<Message>();

	}

	public IMAPFolder getMailBox(String storeString) throws MessagingException {

		IMAPFolder folder = (IMAPFolder) getStore().getFolder(storeString);

		if (!folder.isOpen())
			folder.open(Folder.READ_ONLY);//READ_WRITE

		return folder;

	}

	/** Message related methods **/
	private void fireListeners(Message message) {
		for (FolderChangeListener listener : this.folderListener) {
			mailReceivedNotification(listener,message);
		}
	}

	private MailNotification mailReceivedNotification(FolderChangeListener listener, Message msg){
		listener.mailReceivedNotification(msg);
		return new MailNotification(msg);
	}
	
	public void addFolderListener(FolderChangeListener listener) {
		this.folderListener.add(listener);
	}

	public void removeFolderListener(FolderChangeListener listener) {
		folderListener.remove(listener);
	}

	public Set<FolderChangeListener> getFolderListener() {
		return folderListener;
	}

	/** END:Message related methods **/
	
	public Calendar getLastFetchDateTime() {
		return lastFetchDateTime;
	}
	
}
