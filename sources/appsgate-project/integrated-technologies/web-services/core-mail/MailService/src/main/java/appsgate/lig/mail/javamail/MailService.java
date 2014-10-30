package appsgate.lig.mail.javamail;

import appsgate.lig.mail.javamail.connexion.JavamailConnexion;

/**
 * Interface for the implementation control of the MailService
 * (configuration, starting, removing,...)
 */
public interface MailService {

	public void setConnexion(JavamailConnexion mailConnexion);

    public void release();

    public void autoRefreshValueChanged(Object newValue);

}