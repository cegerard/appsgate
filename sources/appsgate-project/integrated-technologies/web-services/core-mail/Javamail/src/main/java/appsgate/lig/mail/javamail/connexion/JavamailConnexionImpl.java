package appsgate.lig.mail.javamail.connexion;

import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPFolder;

public class JavamailConnexionImpl implements JavamailConnexion {
	
	private final static org.slf4j.Logger logger = LoggerFactory
			.getLogger(JavamailConnexionImpl.class);	
	
	
	private Timer refreshTimer;
	
	/*
	 * IMAP variables
	 */
	private Store store = null;
	private Session session = null;
	private IMAPFolder lastIMAPFolder = null;

	private MailConfiguration properties;
	
	MailConnexionListener listener;
	boolean connexionAvailable = false;
	
	public JavamailConnexionImpl(MailConfiguration config, MailConnexionListener listener) {
		this.listener = listener;
		properties = new MailConfiguration();
		properties.putAll(config);
	}	
	
	@Override
	public boolean testConnexion() {
		logger.trace("testConnexion()");
			try {
				IMAPFolder box = getMailBox();
				box.getMessages();
				
				if(!connexionAvailable && listener!=null) {
					listener.connexionAvailable(this);
				}
				connexionAvailable=true;				

			} catch (Exception e) {
				logger.error("Exception during testConnexion, " + e.getStackTrace());
				if(connexionAvailable && listener!=null) {
					listener.connexionBroken(this);
				}
				connexionAvailable=false;
			}
			logger.trace("testConnexion(), returning "+connexionAvailable);
			
			return connexionAvailable;
	}
	
	
	
	@Override
	public int hashCode() {
		String target = properties.getProperty(MailConfiguration.USER)
				+ properties.getProperty(MailConfiguration.PASSWORD);
		return target.hashCode();
	}

	@Override
	public void start() {
		testConnexion();
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
	
	private void configureAutoRefreshTask() {

		refreshTimer = new Timer();

		refreshtask = new TimerTask() {

			@Override
			public void run() {
				testConnexion();
			}

		};

		if (refreshRate != null && refreshRate != -1) {
			logger.trace("Configuring auto-refresh to: {} ms", refreshRate);
			refreshTimer.scheduleAtFixedRate(refreshtask, 0,
					refreshRate.longValue());
		}

	}	
	
	private TimerTask refreshtask;	
	
	private final Integer refreshRate = 1000*60*2;

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
	
	@Override
	public IMAPFolder getMailBox() throws MessagingException {
	 return getMailBox(GMailConstants.DEFAULT_INBOX);
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
	
	@Override
	public String getCurrentUser() {
		return properties.getProperty(MailConfiguration.USER);
	}	
	
	
	

}
