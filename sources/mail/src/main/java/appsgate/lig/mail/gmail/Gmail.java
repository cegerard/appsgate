package appsgate.lig.mail.gmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import appsgate.lig.mail.FolderChangeListener;
import appsgate.lig.mail.Mail;

import com.sun.mail.imap.IMAPFolder;

public class Gmail implements Mail{

	private final String BOX = "inbox";
	private final String PROTOCOL_KEY = "mail.store.protocol";
	private final String PROTOCOL_VALUE = "imaps";
	private final String IMAP_SERVER = "imap.googlemail.com";
	private String USER = "smarthome.inria@gmail.com";
	private String PASSWORD = "smarthome2012";
	private final Integer refreshRate=0;

	HashMap<String, Message> messagesCached = new HashMap<String, Message>();
	private Set<FolderChangeListener> folderListener = new HashSet<FolderChangeListener>();

	private Store store;
	private Session session;
	private Properties properties;

	public Gmail() {
		properties = System.getProperties();
		properties.setProperty(PROTOCOL_KEY, PROTOCOL_VALUE);
	}

	private Session getSession() {

		if (session == null) {
			session = Session.getDefaultInstance(properties, null);
		}

		return session;
	}

	private Store getStore() {

		if (store == null) {

			try {
				store = getSession().getStore(PROTOCOL_VALUE);
				store = session.getStore(PROTOCOL_VALUE);
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

	public Set<FolderChangeListener> getFolderListener() {
		return folderListener;
	}

	public void fetch() {
		Folder folder = getMailBox(BOX);
		boolean firstTime = messagesCached.size() == 0 ? true : false;

		try {

			for (Message message : folder.getMessages()) {

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
		throw new UnsupportedOperationException();
	}

	public Message getMail(String messageId) {
		throw new UnsupportedOperationException();
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
	
	public void release(){
		
		try {
			this.store.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
	}

	public void start() {
		
	}

	public void stop() {
		
	}
	
}
