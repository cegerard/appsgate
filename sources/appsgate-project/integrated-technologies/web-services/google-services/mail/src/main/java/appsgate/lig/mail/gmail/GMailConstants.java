package appsgate.lig.mail.gmail;

import java.util.HashMap;
import java.util.Map;

public class GMailConstants {

	public static final String DEFAULT_INBOX = "inbox";
	public static final String IMAP_SERVER = "imap.googlemail.com";
	public static final String PROTOCOL_VALUE="imaps";
	
	public static final Map<String, String> defaultGoogleProperties = new HashMap<String, String>() {
		{
			put("mail.smtp.auth", "true");
			put("mail.smtp.starttls.enable", "true");
			put("mail.smtp.host", "smtp.gmail.com");
			put("mail.smtp.port", "587");
			put("mail.store.protocol", PROTOCOL_VALUE);
		}
	};
	
}
