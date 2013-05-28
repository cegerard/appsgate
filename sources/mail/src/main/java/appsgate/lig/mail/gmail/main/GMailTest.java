package appsgate.lig.mail.gmail.main;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import appsgate.lig.mail.FolderChangeListener;
import appsgate.lig.mail.Mail;

public class GMailTest {

	private Mail mailService;
		
	private FolderChangeListener listener=new FolderChangeListener() {
		
		public void mailReceivedNotification(Message message) {
			try {
				System.out.println("Mail received, subject:"+message.getSubject());
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			
		}
	};
	
	public void start() throws AddressException, MessagingException {
		
		System.out.println("----->Total of emails in the inbox:"+mailService.getMails().size());
		
//		Message message = new MimeMessage(mailService.getSession());
//		message.setFrom(new InternetAddress("from-email@gmail.com"));
//		message.setRecipients(Message.RecipientType.TO,
//			InternetAddress.parse("jbotnascimento@gmail.com"));
//		message.setSubject("Testing sender");
//		message.setText("Message body!");
//		
//		mailService.sendMail(message);
		
//		mailService.sendMailSimple("jbotnascimento@gmail.com", "ping", "ping body");
		
		mailService.addFolderListener(listener);
		
	}

	public void stop() {
		
		if(mailService!=null)
			mailService.removeFolderListener(listener);
	}
	
}
