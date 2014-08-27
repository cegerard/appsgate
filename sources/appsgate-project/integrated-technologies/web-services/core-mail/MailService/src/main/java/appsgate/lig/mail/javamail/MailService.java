package appsgate.lig.mail.javamail;

/**
 * Interface for the implementation control of the MailService
 * (configuration, starting, removing,...)
 */
public interface MailService {

    public void setAccount(String user, String password);

    public void setConfiguration(MailConfiguration config);

    public void release();

    public void autoRefreshValueChanged(Object newValue);

    public void start();

    public void stop();
}