package appsgate.lig.mail.gmail.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import appsgate.lig.mail.FolderChangeListener;

import com.sun.mail.imap.IMAPFolder;

public class GmailStatic {

	private final String PROTOCOL = "imaps";
	private final String USER = "smarthome.inria@gmail.com";
	private final String PASSWORD = "smarthome2012";

	HashMap<String,Message> messagesCached=new HashMap<String,Message>();
	private Set<FolderChangeListener> folderListener=new HashSet<FolderChangeListener>();
	
	private Store store;
	private Session session;
	private Properties properties;

	public GmailStatic() {
		properties = System.getProperties();
		properties.setProperty("mail.store.protocol", "imaps");
	}

	private Session getSession() {

		if (session == null) {
			session = Session.getDefaultInstance(properties, null);
		}

		return session;
	}

	private Store getStore() {

		if(store == null){
		
			try {
				store = getSession().getStore(PROTOCOL);
				store = session.getStore("imaps");
				store.connect("imap.googlemail.com", USER, PASSWORD);

			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
		}

		return store;
	}

	public void addFolderListener(FolderChangeListener listener){
		this.folderListener.add(listener);
	}
	
	public Set<FolderChangeListener> getFolderListener(){
		return folderListener;
	}
	
	private void fireListeners(Folder folder,Message message){
		for(FolderChangeListener listener:this.folderListener){
			listener.mailReceivedNotification(folder, message);
		}
	}
	
	public void checkChange(){
		Folder folder=getMailBox("inbox");
		boolean firstTime=messagesCached.size()==0?true:false;
			
		try {
			
			for(Message message:folder.getMessages()){
				
				String messageId=message.getHeader("message-id")[0];
				
				boolean isPresent=messagesCached.keySet().contains(messageId);
				
				if(!isPresent||firstTime){
					messagesCached.put(messageId, message);
					
				}
				
				if(!isPresent&&!firstTime){
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

	public Message getMail() {
		return null;
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

	public void listMessages() throws MessagingException, IOException {

		IMAPFolder folder = getMailBox("inbox");

		Message[] messages = folder.getMessages();
		System.out.println("No of Messages : " + folder.getMessageCount());
		System.out.println("No of Unread Messages : "
				+ folder.getUnreadMessageCount());
		System.out.println(messages.length);
		for (int i = 0; i < messages.length; i++) {

			System.out
					.println("*****************************************************************************");
			System.out.println("MESSAGE " + (i + 1) + ":");
			Message msg = messages[i];
			// System.out.println(msg.getMessageNumber());
			// Object String;
			// System.out.println(folder.getUID(msg)

			String subject = msg.getSubject();

			System.out.println("Subject: " + subject);
			System.out.println("From: " + msg.getFrom()[0]);
			System.out.println("To: " + msg.getAllRecipients()[0]);
			System.out.println("Date: " + msg.getReceivedDate());
			System.out.println("Size: " + msg.getSize());
			System.out.println(msg.getFlags());
			System.out.println("Body: \n" + msg.getContent());
			System.out.println(msg.getContentType());

		}
	}

	public static void main(String[] args) throws MessagingException,
			IOException, InterruptedException {
		GmailStatic m = new GmailStatic();
		
		m.addFolderListener(new FolderChangeListener() {
			
			public void mailReceivedNotification(Folder folder, Message message) {
				try {
					System.out.println("--- changed, message:"+message.getSubject());
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
		});
		
		m.listMessages();
		
		while(true){
			m.checkChange();
			Thread.sleep(5000);
		}
		
	}

	public static void main2(String[] args) throws MessagingException,
			IOException {
		IMAPFolder folder = null;
		Store store = null;
		String subject = null;
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");

			Session session = Session.getDefaultInstance(props, null);

			store = session.getStore("imaps");

			store.connect("imap.googlemail.com", "smarthome.inria@gmail.com",
					"smarthome2012");

			folder = (IMAPFolder) store.getFolder("inbox");

			if (!folder.isOpen())
				folder.open(Folder.READ_WRITE);
			Message[] messages = folder.getMessages();
			System.out.println("No of Messages : " + folder.getMessageCount());
			System.out.println("No of Unread Messages : "
					+ folder.getUnreadMessageCount());
			System.out.println(messages.length);
			for (int i = 0; i < messages.length; i++) {

				System.out
						.println("*****************************************************************************");
				System.out.println("MESSAGE " + (i + 1) + ":");
				Message msg = messages[i];
				// System.out.println(msg.getMessageNumber());
				// Object String;
				// System.out.println(folder.getUID(msg)

				subject = msg.getSubject();

				System.out.println("Subject: " + subject);
				System.out.println("From: " + msg.getFrom()[0]);
				System.out.println("To: " + msg.getAllRecipients()[0]);
				System.out.println("Date: " + msg.getReceivedDate());
				System.out.println("Size: " + msg.getSize());
				System.out.println(msg.getFlags());
				System.out.println("Body: \n" + msg.getContent());
				System.out.println(msg.getContentType());

			}
		} finally {
			if (folder != null && folder.isOpen()) {
				folder.close(true);
			}
			if (store != null) {
				store.close();
			}
		}

	}

	public List<Message> getMails() {
		// TODO Auto-generated method stub
		return null;
	}

}
