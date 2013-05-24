package appsgate.lig.mail.gmail.main;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import appsgate.lig.mail.FolderChangeListener;
import appsgate.lig.mail.Mail;

public class GMailTest {

	private Mail mailService;
	
	private FolderChangeListener listener=new FolderChangeListener() {
		
		public void mailReceivedNotification(Folder folder, Message message) {
			try {
				System.out.println("Folder changed... mail received:"+message.getSubject());
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};
	
	public void start() throws AddressException, MessagingException {
		
		System.out.println("----->"+mailService.getMails().size());
		
//		Message message = new MimeMessage(mailService.getSession());
//		message.setFrom(new InternetAddress("from-email@gmail.com"));
//		message.setRecipients(Message.RecipientType.TO,
//			InternetAddress.parse("jbotnascimento@gmail.com"));
//		message.setSubject("Testing sender");
//		message.setText("Message body!");
//		
//		mailService.sendMail(message);
		
		mailService.sendMailSimple("jbotnascimento@gmail.com", "ping", "ping body");
		
		mailService.addFolderListener(listener);
		
	}

	public void stop() {
		
		if(mailService!=null)
			mailService.removeFolderListener(listener);
	}
	
}
