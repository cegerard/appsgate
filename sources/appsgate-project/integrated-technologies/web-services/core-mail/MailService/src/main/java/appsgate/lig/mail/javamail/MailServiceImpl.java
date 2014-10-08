package appsgate.lig.mail.javamail;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.mail.Mail;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfiguration;

import com.sun.mail.imap.IMAPFolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.swing.DebugGraphics;

import java.util.*;

/**
 * Gmail implementation for mail service
 * 
 * 
 * 
 */
public class MailServiceImpl extends CoreObjectBehavior implements Mail,
		MailService, CoreObjectSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private final static org.slf4j.Logger logger = LoggerFactory
			.getLogger(MailServiceImpl.class);
	private Timer refreshTimer;

	private MailSender mailSender = null;

	/*
	 * Object information
	 */
	private String serviceId;
	private String userType;
	private String status;

	private String user;

	/*
	 * State variables
	 */
	private Calendar lastFetchDateTime = null;
	private final Integer refreshRate = -1;
	private MailConfiguration properties;
	private final HashMap<String, Message> messagesCached = new HashMap<String, Message>();

	/*
	 * IMAP variables
	 */
	private Store store = null;
	private Session session = null;
	private IMAPFolder lastIMAPFolder = null;

	private TimerTask refreshtask;

	public MailServiceImpl() {
		properties = new MailConfiguration();
	}

	@Override
	public void setConfiguration(MailConfiguration config) {
		properties = new MailConfiguration();
		properties.putAll(config);
	}

	@Override
	public void setAccount(String user, String password) {
		this.user = user;
		properties.put(MailConfiguration.USER, user);
		properties.put(MailConfiguration.FROM, user);
		properties.put(MailConfiguration.PASSWORD, password);
	}

	@Override
	public void start() {

		try {
			String target = properties.getProperty(MailConfiguration.USER)
					+ properties.getProperty(MailConfiguration.PASSWORD);
			serviceId = String.valueOf(target.hashCode());
			fetch();
		} catch (MessagingException e) {
			throw new RuntimeException("unable to start component. Message "
					+ e.getMessage());
		}

		configureAutoRefreshTask();

	}

	@Override
	public void stop() {

		refreshtask.cancel();

		release();

		if (lastIMAPFolder != null) {
			try {
				lastIMAPFolder.close(false);
			} catch (MessagingException e) {
				// Error closing the mail, nothing you can do about it, sorry
			}
		}
	}

	@Override
	public void release() {

		try {
			getStore().close();
		} catch (MessagingException e) {
			logger.warn("failed to release store with the message: {}",
					e.getMessage());
		}

		session = null;
		store = null;

	}

	/*
	 * Established the mail connection with the provider
	 */
	private Store getStore() throws MessagingException {

		if (store == null || !store.isConnected()) {

			try {
				// Set the socket factory to trust all hosts

				// Get the store
				store = getSession().getStore(
						properties
								.getProperty(MailConfiguration.STORE_PROTOCOL));
				logger.debug("Establishing connection with IMAP server.");

				store.connect(
						properties.getProperty(MailConfiguration.STORE_HOST),
						properties.getProperty(MailConfiguration.USER),
						properties.getProperty(MailConfiguration.PASSWORD));
				logger.debug("Connection established with IMAP server, "
						+ "store : " + store);

			} catch (Exception exc) {
				logger.error("Exception when connecting to IMAP Server "
						+ exc.getStackTrace());
			}

			// store = getSession().getStore(GMailConstants.PROTOCOL_VALUE);

			// store.connect(GMailConstants.IMAP_SERVER,GMailConstants.IMAP_PORT,
			// USER, PASSWORD);
		}

		return store;
	}

	private void configureAutoRefreshTask() {

		refreshTimer = new Timer();

		refreshtask = new TimerTask() {

			@Override
			public void run() {

				try {
					logger.trace("Refreshing mail data");
					MailServiceImpl.this.fetch();
				} catch (MessagingException e) {
					logger.warn(
							"Refreshing mail data FAILED with the message: {}",
							e.getMessage());
				}
			}

		};

		if (refreshRate != null && refreshRate != -1) {
			logger.trace("Configuring auto-refresh to: {} ms", refreshRate);
			refreshTimer.scheduleAtFixedRate(refreshtask, 0,
					refreshRate.longValue());
		}

	}

	@Override
	public Session getSession() {

		if (session == null) {

			// This method will openup a browser (pc/mobile) to verify the user
			// auth in case of auth3
			// session=Session.getDefaultInstance(properties, null);
			// create the properties for the Session
			properties.setProperty("mail.imaps.socketFactory.class",
					"appsgate.lig.mail.gmail.utils.AppsGateSSLSocketFactory");

			session = Session.getInstance(properties,
					new javax.mail.Authenticator() {

						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(
									properties
											.getProperty(MailConfiguration.USER),
									properties
											.getProperty(MailConfiguration.PASSWORD));
						}
					});
		}

		return session;
	}

	/*
	 * Check for possible updates in the assigned mail box
	 * 
	 * @see appsgate.lig.mail.Mail#fetch()
	 */
	@Override
	public void fetch() throws MessagingException {

		boolean firstTime = messagesCached.isEmpty();

		try {

			IMAPFolder box = getMailBox(properties
					.getProperty(GMailConstants.DEFAULT_FOLDER));

			for (Message message : box.getMessages()) {

				String messageId = message.getHeader("message-id")[0];

				boolean isPresent = messagesCached.keySet().contains(messageId);

				if (!isPresent || firstTime) {
					messagesCached.put(messageId, message);

				}

				if (!isPresent && !firstTime) {
					mailReceivedNotification(message);
					// fireListeners(message);
				}
			}
			lastFetchDateTime = Calendar.getInstance();

		} catch (Exception e) {
			logger.error("Exception during fetch, " + e.getStackTrace());
		} finally {
			// release();
		}

	}

	/*
	 * Sends an email with a Message type, which allows to add specialized
	 * modification in the mailcontent, like add an attachment. Example of
	 * usage:
	 * 
	 * Message message = new MimeMessage(mailService.getSession());
	 * message.setFrom(new InternetAddress("from-email@gmail.com"));
	 * message.setRecipients(Message.RecipientType.TO,
	 * InternetAddress.parse("jbotnascimento@gmail.com"));
	 * message.setSubject("Testing sender"); message.setText("Message body!");
	 * mailService.sendMail(message);
	 * mailService.sendMailSimple("jbotnascimento@gmail.com",
	 * "ping","ping body"); mailService.addFolderListener(listener);
	 * 
	 * @see appsgate.lig.mail.Mail#sendMail(javax.mail.Message)
	 */
	@Override
	public boolean sendMail(Message message) {

		/*
		 * Example
		 */
		try {
			sendMailSimple(message.getAllRecipients()[0].toString(),
					message.getSubject(), message.getContent().toString());
		} catch (Exception exc) {
			logger.error("Impossible to send mail from account {} due to '{}'",
					properties.getProperty(MailConfiguration.USER),
					exc.getStackTrace());

			return false;
		}

		return true;
	}

	/*
	 * Send an email with a recipient, subject and body
	 * 
	 * @see appsgate.lig.mail.Mail#sendMailSimple(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public boolean sendMailSimple(String to, String subject, String body) {

		try {
			if (mailSender == null) {
				mailSender = new MailSender(getSession());
			}

			logger.info("Sending mail to: {}", to);

			mailSender.sendMail(subject, body,
					properties.getProperty(MailConfiguration.USER), to);

			return true;

		} catch (Exception e) {
			logger.error("Impossible to send mail from account {} due to '{}'",
					properties.getProperty(MailConfiguration.USER),
					e.getStackTrace());
			return false;
		}

	}

	@Override
	public Message getMail(String messageId) {

		if (messagesCached != null) {
			return messagesCached.get(messageId);
		}

		return null;
	}

	@Override
	public List<Message> getMails(int size) {
		try {

			IMAPFolder folder = getMailBox(GMailConstants.DEFAULT_INBOX);

			Message[] messages;
			int totalSize = folder.getMessageCount();
			if (size > totalSize) {
				size = totalSize;
			}

			if (size == 0) {
				messages = folder.getMessages();
			} else {
				messages = folder.getMessages(folder.getMessageCount() - size
						+ 1, folder.getMessageCount());
			}

			List<Message> imapMessages = Arrays.asList(messages);

			Collections.reverse(imapMessages);

			return imapMessages;

		} catch (MessagingException e) {
			logger.warn("Impossible to get Mails");
		}

		return new ArrayList<Message>();
	}

	@Override
	public List<Message> getMails() {

		return getMails(0);

	}

	@Override
	public IMAPFolder getMailBox(String storeString) throws MessagingException {

		if (lastIMAPFolder != null
				&& lastIMAPFolder.getName().equals(storeString)) {
			return lastIMAPFolder;
		} else if (lastIMAPFolder != null) {
			lastIMAPFolder.close(false);
		}

		logger.debug("storeString : " + storeString);
		lastIMAPFolder = (IMAPFolder) getStore().getFolder(storeString);

		if (!lastIMAPFolder.isOpen()) {
			lastIMAPFolder.open(Folder.READ_ONLY);// READ_WRITE
		}
		return lastIMAPFolder;

	}

	private NotificationMsg mailReceivedNotification(final Message msg) {
		return new NotificationMsg() {

			@Override
			public CoreObjectSpec getSource() {
				return null;
			}

			@Override
			public String getNewValue() {
				return null;
			}

			@Override
			public String getOldValue() {
				return null;
			}

			@Override
			public String getVarName() {
				return null;
			}

			@SuppressWarnings({ "rawtypes", "finally" })
			@Override
			public JSONObject JSONize() {

				JSONObject result = new JSONObject();

				try {

					result.put("objectId", serviceId);
					result.put("varName", "newMail");
					result.put("value", properties.get(MailConfiguration.USER));

					result.put("subject", msg.getSubject());
					result.put("from", msg.getFrom());

					result.put("message-id", msg.getHeader("message-id")[0]);

					List<String> list = new ArrayList<String>();

					Enumeration enume = msg.getAllHeaders();

					while (enume.hasMoreElements()) {
						String param = (String) enume.nextElement();
						System.out.println(param);
						list.add(param);
					}

					result.put("headers", list);

				} catch (MessagingException e) {
					e.printStackTrace();
				} finally {
					return result;
				}

			}
		};
	}

	@Override
	public Calendar getLastFetchDateTime() {
		return lastFetchDateTime;
	}

	@Override
	public void autoRefreshValueChanged(Object newValue) {
		refreshtask.cancel();
		logger.trace("Auto-refresh changed to: {}", refreshRate);
		configureAutoRefreshTask();

	}

	@Override
	public String getAbstractObjectId() {
		return serviceId;
	}

	@Override
	public String getUserType() {
		return userType;
	}

	@Override
	public int getObjectStatus() {
		return Integer.parseInt(status);
	}

	@Override
	public String getPictureId() {
		return "";
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();

		descr.put("id", serviceId);
		descr.put("type", userType); // 102 for mail
		descr.put("status", status);
		descr.put("user", properties.getProperty(MailConfiguration.USER));
		descr.put("refreshRate", refreshRate);

		descr.put("favorite-recipients", getFavoriteRecipients());

		return descr;
	}

	@Override
	public void setPictureId(String pictureId) {
	}

	@Override
	public CORE_TYPE getCoreType() {
		return CORE_TYPE.SERVICE;
	}

	Set<String> recipients = new HashSet<String>();

	@Override
	public boolean addFavoriteRecipient(String recipientMail) {
		logger.trace("addFavoriteRecipient(String recipientMail : "
				+ recipientMail + ")");
		if (!recipients.contains(recipientMail)) {
			recipients.add(recipientMail);
			logger.trace("addFavoriteRecipient(...), recipient successfully added locally");
			if (!startupSynchro) {
				synchroDB(true);
			}
			DBHelper dbHelper = getDBHelper();
			dbHelper.insertSimpleObject(recipientMail);
			logger.trace("addFavoriteRecipient(...), recipient successfully added in the DB");
			fireMessage("recipient-added", null, recipientMail);
			logger.trace("removeFavoriteRecipient(...), CoreNotificationMessage sent");
			
			
			return true;
		} else {
			logger.trace("addFavoriteRecipient(...), recipient already in the favorites");
			return false;
		}

	}

    private NotificationMsg fireMessage(String varName, String oldValue, String newValue) {
        return new CoreNotificationMsg(varName, oldValue, newValue, this);
    }	
	
	@Override
	public boolean removeFavoriteRecipient(String recipientMail) {
		logger.trace("removeFavoriteRecipient(String recipientMail : "
				+ recipientMail + ")");
		if (recipients.contains(recipientMail)) {
			recipients.remove(recipientMail);
			logger.trace("removeFavoriteRecipient(...), recipient successfully removed");
			
			DBHelper dbHelper = getDBHelper();
			dbHelper.remove(recipientMail);
			logger.trace("removeFavoriteRecipient(...), recipient successfully remove from the DB");
			
			fireMessage("recipient-removed", null, recipientMail);
			logger.trace("removeFavoriteRecipient(...), CoreNotificationMessage sent");
			
			return true;
		} else {
			logger.trace("removeFavoriteRecipient(...), recipient not existing in the favorites");
			return false;
		}
	}

	@Override
	public JSONArray getFavoriteRecipients() {
		logger.trace("getFavoriteRecipients()");
		JSONArray array = new JSONArray();
		synchroDB(true);
		for(String str : recipients) {
			array.put(new JSONObject().put("mail", str));
		}
		return array;
	}

	MongoDBConfiguration dbConfig = null;
	boolean startupSynchro = false;

	String dbName = DBNAME_DEFAULT;

	String dbCollectionName = MailServiceImpl.class.getSimpleName();

	/**
	 * Synchronize Favorite recipients list with DataBase
	 * 
	 * @param dbToService
	 *            true indicates that the local favorites recipients are erased
	 *            and replaced by those in the DB, false indicates that the
	 *            entries in the db must be erased and replaced by those locals
	 */
	private void synchroDB(boolean dbToService) {
		logger.trace("synchroDB()");
		DBHelper dbHelper = getDBHelper();
		if (dbHelper == null) {
			logger.warn("synchroDB(), error when accessing the DB, "
					+ "cannot retrieve a DBHelper for DB : " + dbName
					+ ", collection : " + dbCollectionName);
			return;
		}

		if (dbToService) {
			recipients = new HashSet<String>();
			for (Object obj : dbHelper.getSimpleObjectEntries()) {
				recipients.add((String) obj);
			}
			logger.trace("synchroDB(), recipents list synchronized FROM the DB");
		} else {
			dbHelper.dropAndInsertSetString(recipients);
			logger.trace("synchroDB(), recipents list synchronized TO the DB");
		}
		startupSynchro = true;

	}

	private DBHelper getDBHelper() {
		logger.trace("synchroDB()");
		if (dbConfig != null) {
			return dbConfig.getDBHelper(dbName, dbCollectionName);
		} else {
			logger.warn("synchroDB(), no database bound");
			return null;
		}
	}

}
