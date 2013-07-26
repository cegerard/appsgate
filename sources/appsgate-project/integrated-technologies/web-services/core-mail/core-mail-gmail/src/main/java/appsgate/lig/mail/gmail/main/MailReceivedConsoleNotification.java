package appsgate.lig.mail.gmail.main;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.felix.ipojo.annotations.Requires;

import appsgate.lig.mail.Mail;
import appsgate.lig.mail.apam.message.ApamMessage;
import fr.imag.adele.apam.Apam;

/**
 * Dummie class for testing the mail service
 * 
 * @author jnascimento
 * 
 */
public class MailReceivedConsoleNotification {

	private Mail mailService;
	
	@Requires
	private Apam apam;

	public void start() throws AddressException, MessagingException {

		System.out.println("Gmail notifier ready to go. Initial size:"
				+ mailService.getMails().size() + " apam "+apam);

	}

	public void stop() {

	}

	public void messageReceived(ApamMessage message)
			throws MessagingException {

		System.out.println("APAM: Mail received, subject:"
				+ message.getMessage().getSubject());

	}
}
