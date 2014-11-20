package appsgate.lig.mail.javamail.connexion;

public interface MailConnexionListener {
	
	void connexionAvailable(JavamailConnexion connexion);
	
	void connexionBroken(JavamailConnexion connexion);
	
}
