package appsgate.lig.mail.javamail;

import appsgate.lig.core.object.messages.CoreNotificationMsg;
import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.mail.Mail;
import appsgate.lig.mail.javamail.connexion.GMailConstants;
import appsgate.lig.mail.javamail.connexion.JavamailConnexion;
import appsgate.lig.persistence.DBHelper;
import appsgate.lig.persistence.MongoDBConfiguration;

import com.sun.mail.imap.IMAPFolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.mail.*;

import java.util.*;

/**
 * Gmail implementation for mail service
 * 
 * 
 * 
 */
public class MailServiceImpl extends CoreObjectBehavior implements Mail, MailService, CoreObjectSpec {

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private final static org.slf4j.Logger logger = LoggerFactory
			.getLogger(MailServiceImpl.class);

	private MailSender mailSender = null;

	/*
	 * Object information
	 */
	private String serviceId;
	private String userType;
	private String status;
	private String user=null;


	/*
	 * State variables
	 */
	private Calendar lastFetchDateTime = null;
	private final HashMap<String, Message> messagesCached = new HashMap<String, Message>();
	
	
	private JavamailConnexion mailConnexion;


	public MailServiceImpl() {
	}

	@Override
	public void setConnexion(JavamailConnexion mailConnexion) {
		
		if(mailConnexion == null ||mailConnexion.getCurrentUser() == null) {
			logger.error("javamail connexion is null or does not configure an user");
			return;
		}
		
		this.mailConnexion = mailConnexion;
		user=mailConnexion.getCurrentUser();
		serviceId = String.valueOf(mailConnexion.hashCode());
	}
	
	@Override
	public void release() {
			mailConnexion.stop();
	}
	
	@Override
	public void autoRefreshValueChanged(Object newValue) {
		refreshtask.cancel();
		logger.trace("Auto-refresh changed to: {}", refreshRate);
		configureAutoRefreshTask();

	}

	private void configureAutoRefreshTask() {

		refreshTimer = new Timer();

		refreshtask = new TimerTask() {

			@Override
			public void run() {

				try {
					logger.trace("Refreshing mail data");
					fetch();
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
	
	private TimerTask refreshtask;	
	
	private Integer refreshRate;
	private Timer refreshTimer;
	
	

	/*
	 * Check for possible updates in the assigned mail box
	 * 
	 * @see appsgate.lig.mail.Mail#fetch()
	 */
	@Override
	public void fetch() throws MessagingException {
		
		if(mailConnexion == null ||!mailConnexion.testConnexion()) {
			throw new MessagingException("No valid javamail connexion available");
		}

		boolean firstTime = messagesCached.isEmpty();

		try {

			IMAPFolder box = mailConnexion.getMailBox();

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
					mailConnexion.getCurrentUser(),
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
		
		if(mailConnexion == null ||!mailConnexion.testConnexion()) {
			return false;
		}

		try {
			if (mailSender == null) {
				mailSender = new MailSender(mailConnexion.getSession());
			}

			logger.info("Sending mail to: {}", to);

			mailSender.sendMail(subject, body,
					mailConnexion.getCurrentUser(), to);
			
			fireMessageSentNotificationMsg(to);
			return true;

		} catch (Exception e) {
			logger.error("Impossible to send mail from account {} due to '{}'",
					mailConnexion.getCurrentUser(),
					e.getStackTrace());
			return false;
		}

	}
	
	private NotificationMsg fireMessageSentNotificationMsg(String recipient) {
		return new CoreNotificationMsg("mailSent", null, recipient, this);
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
		if(mailConnexion == null ||!mailConnexion.testConnexion()) {
			new ArrayList<Message>();
		}
		
		try {

			IMAPFolder folder = mailConnexion.getMailBox(GMailConstants.DEFAULT_INBOX);

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
					if(mailConnexion == null ) {
						result.put("value", "unknown");
					} else {
						result.put("value", mailConnexion.getCurrentUser());
					}
					

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
	public JSONObject getDescription() throws JSONException {
		JSONObject descr = new JSONObject();

		descr.put("id", serviceId);
		descr.put("type", userType); // 102 for mail
		descr.put("status", status);
		
		if(user!= null && !user.isEmpty()) {
			descr.put("user", user);
		} else 	if(mailConnexion != null  ) {
				descr.put("user", mailConnexion.getCurrentUser());
		} else {
			descr.put("user", "unknown");
		}

		descr.put("favorite-recipients", getFavoriteRecipients());

		return descr;
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
