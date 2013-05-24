package appsgate.lig.mail.gmail;

import java.util.ArrayList;
import java.util.Arrays;
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
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import appsgate.lig.mail.FolderChangeListener;
import appsgate.lig.mail.Mail;

import com.sun.mail.imap.IMAPFolder;

public class Gmail implements Mail {
	
	private Logger logger = Logger.getLogger(Gmail.class.getSimpleName());
	private Timer refreshTimer = new Timer();
	private TimerTask refreshtask = new TimerTask(){
		@Override
		public void run() {
			logger.info("Refreshing mail data");
			Gmail.this.fetch();
		}
	};
	
	private Store store;
	private Session session;
	private Properties properties;
	
	private final String BOX = "inbox";
	private final String PROTOCOL_KEY = "mail.store.protocol";
	private final String PROTOCOL_VALUE = "imaps";
	private final String IMAP_SERVER = "imap.googlemail.com";
	private String USER = "smarthome.inria@gmail.com";
	private String PASSWORD = "smarthome2012";
	private Integer refreshRate = -1;

	private HashMap<String, Message> messagesCached = new HashMap<String, Message>();
	private Set<FolderChangeListener> folderListener = new HashSet<FolderChangeListener>();
	
	
	
	private Map<String,String> defaultGoogleProperties= new HashMap<String, String>(){{
		put("mail.smtp.auth", "true");
		put("mail.smtp.starttls.enable", "true");
		put("mail.smtp.host", "smtp.gmail.com");
		put("mail.smtp.port", "587");
		put(PROTOCOL_KEY, PROTOCOL_VALUE);
	}};

	public Gmail() {
		properties = System.getProperties();
		properties.setProperty(PROTOCOL_KEY, PROTOCOL_VALUE);
		properties.putAll(defaultGoogleProperties);

	}

	private Store getStore() {

		if (store == null) {

			try {
				store = getSession().getStore(PROTOCOL_VALUE);
			
				store.connect(IMAP_SERVER, USER, PASSWORD);

			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}

		}

		return store;
	}

	private void fireListeners(Folder folder, Message message) {
		for (FolderChangeListener listener : this.folderListener) {
			listener.mailReceivedNotification(folder, message);
		}
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

	public Session getSession() {

		if (session == null) {
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
	
	/**
	 * Check for possible updates in the assigned mail box
	 */
	public void fetch() {
		Folder folder = getMailBox(BOX);
		boolean firstTime = messagesCached.size() == 0 ? true : false;

		try {

			for (Message message : getMailBox(BOX).getMessages()) {

				String messageId = message.getHeader("message-id")[0];

				boolean isPresent = messagesCached.keySet().contains(messageId);

				if (!isPresent || firstTime) {
					messagesCached.put(messageId, message);

				}

				if (!isPresent && !firstTime) {
					fireListeners(folder, message);
				}

			}

		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	public boolean sendMail(Message message) {

		try {
			Transport.send(message);
		} catch (MessagingException e) {
			return false;
		}

		return true;
	}

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
			logger.log(Level.SEVERE,String.format("Impossible to send mail from account %s due to '%s'",USER,e.getMessage()));
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

	public IMAPFolder getMailBox(String storeString) {

		try {
			IMAPFolder folder = (IMAPFolder) getStore().getFolder(storeString);

			if (!folder.isOpen())
				folder.open(Folder.READ_WRITE);

			return folder;

		} catch (MessagingException e) {
			return null;
		}

	}

	public void release() {

		try {
			getStore().close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		refreshtask.cancel();

	}

	public void start() {

		fetch();
		
		if(refreshRate!=null && refreshRate!=-1){
			logger.info("Configuring auto-refresh for :" + refreshRate);
			refreshTimer.scheduleAtFixedRate(refreshtask, 0, refreshRate.longValue());
		}
		
	}

	public void stop() {
		release();
	}



}
