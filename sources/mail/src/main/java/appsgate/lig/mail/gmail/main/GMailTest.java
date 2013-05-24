package appsgate.lig.mail.gmail.main;

import appsgate.lig.mail.Mail;

public class GMailTest {

	Mail mailService;
	
	public void start() {
		
		System.out.println("----->"+mailService.getMails().size());
		
	}

	public void stop() {
		
	}
	
}
