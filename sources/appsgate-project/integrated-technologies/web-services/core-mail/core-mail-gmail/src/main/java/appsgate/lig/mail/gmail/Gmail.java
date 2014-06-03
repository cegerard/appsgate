package appsgate.lig.mail.gmail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.core.object.messages.NotificationMsg;
import appsgate.lig.core.object.spec.CoreObjectBehavior;
import appsgate.lig.core.object.spec.CoreObjectSpec;
import appsgate.lig.mail.Mail;

import com.sun.mail.imap.IMAPFolder;
import org.slf4j.LoggerFactory;

/**
 * Gmail implementation for mail service
 *
 * @author jnascimento
 * @author Cédric Gérard
 *
 */
public class Gmail extends CoreObjectBehavior implements Mail, CoreObjectSpec {

    /**
     * Static class member uses to log what happened in each instances
     */
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(Gmail.class);
    private Timer refreshTimer;

    /*
     * Connection information variables
     */
    private String USER;
    private String PASSWORD;

    /*
     * Object information
     */
    private String serviceId;
    private String userType;
    private String status;

    /*
     * State variables
     */
    private Calendar lastFetchDateTime = null;
    private final Integer refreshRate = -1;
    private final Properties properties;
    private final HashMap<String, Message> messagesCached = new HashMap<String, Message>();

    /*
     * IMAP variables
     */
    private Store store;
    private Session session;
    private IMAPFolder lastIMAPFolder = null;

    private TimerTask refreshtask;

    public Gmail() {
        properties = System.getProperties();
        properties.putAll(GMailConstants.defaultGoogleProperties);

    }

    public void start() {

        try {
            String target = USER + PASSWORD;
            serviceId = String.valueOf(target.hashCode());
            fetch();
        } catch (MessagingException e) {
            throw new RuntimeException("unable to start component. Message " + e.getMessage());
        }

        configureAutoRefreshTask();

    }

    public void stop() {

        refreshtask.cancel();

        release();

        if (lastIMAPFolder != null) {
            try {
                lastIMAPFolder.close(false);
            } catch (MessagingException e) {
                // Error closing the mail, nothing you can do about it, sorry
            }
        }
    }

    public void release() {

        try {
            getStore().close();
        } catch (MessagingException e) {
            logger.warn("failed to release store with the message: {}", e.getMessage());
        }

        session = null;
        store = null;

    }

    /*
     * Established the mail connection with the provider
     */
    private Store getStore() throws MessagingException {

        if (store == null || !store.isConnected()) {

            store = getSession().getStore(GMailConstants.PROTOCOL_VALUE);

            store.connect(GMailConstants.IMAP_SERVER, USER, PASSWORD);
        }

        return store;
    }

    private void configureAutoRefreshTask() {

        refreshTimer = new Timer();

        refreshtask = new TimerTask() {

            @Override
            public void run() {

                try {
                    logger.trace("Refreshing mail data");
                    Gmail.this.fetch();
                } catch (MessagingException e) {
                    logger.warn("Refreshing mail data FAILED with the message: {}", e.getMessage());
                }
            }

        };

        if (refreshRate != null && refreshRate != -1) {
            logger.trace("Configuring auto-refresh to: {} ms", refreshRate);
            refreshTimer.scheduleAtFixedRate(refreshtask, 0, refreshRate.longValue());
        }

    }

    @Override
    public Session getSession() {

        if (session == null) {

            //This method will openup a browser (pc/mobile) to verify the user auth in case of auth3
            //session=Session.getDefaultInstance(properties, null);
            session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(Gmail.this.USER, Gmail.this.PASSWORD);
                        }
                    });
        }

        return session;
    }

    /*
     * Check for possible updates in the assigned mail box
     * @see appsgate.lig.mail.Mail#fetch()
     */
    @Override
    public void fetch() throws MessagingException {

        boolean firstTime = messagesCached.isEmpty();

        try {

            IMAPFolder box = getMailBox(GMailConstants.DEFAULT_INBOX);

            for (Message message : box.getMessages()) {

                String messageId = message.getHeader("message-id")[0];

                boolean isPresent = messagesCached.keySet().contains(messageId);

                if (!isPresent || firstTime) {
                    messagesCached.put(messageId, message);

                }

                if (!isPresent && !firstTime) {
                    mailReceivedNotification(message);
                    //fireListeners(message);
                }
            }
            lastFetchDateTime = Calendar.getInstance();

        } catch (MessagingException e) {
            e.printStackTrace();
        } finally {
            //release();
        }

    }

    /*
     * Sends an email with a Message type, which allows to add specialized modification in the mailcontent, like add an attachment. Example of usage:
     * 
     *	Message message = new MimeMessage(mailService.getSession());
     *	message.setFrom(new InternetAddress("from-email@gmail.com"));
     *	message.setRecipients(Message.RecipientType.TO,
     *	InternetAddress.parse("jbotnascimento@gmail.com"));
     *	message.setSubject("Testing sender");
     *	message.setText("Message body!");
     *	mailService.sendMail(message);
     *	mailService.sendMailSimple("jbotnascimento@gmail.com", "ping","ping body");
     *	mailService.addFolderListener(listener);
     *	
     * @see appsgate.lig.mail.Mail#sendMail(javax.mail.Message)
     */
    @Override
    public boolean sendMail(Message message) {

        /*
         * Example
         */
        try {
            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Impossible to send mail from account {} due to '{}'", USER, e.getMessage());
            return false;
        }

        return true;
    }

    /*
     * Send an email with a recipient, subject and body 
     * @see appsgate.lig.mail.Mail#sendMailSimple(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean sendMailSimple(String to, String subject, String body) {

        try {

            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(USER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            return sendMail(message);

        } catch (MessagingException e) {
            logger.error("Impossible to send mail from account {} due to '{}'", USER, e.getMessage());
            return false;
        }

    }

    @Override
    public Message getMail(String messageId) {

        if (messagesCached != null) {
            return messagesCached.get(messageId);
        }

        return null;
    }

    @Override
    public List<Message> getMails(int size) {
        try {

            IMAPFolder folder = getMailBox(GMailConstants.DEFAULT_INBOX);

            Message[] messages;

            if (size == 0) {
                messages = folder.getMessages();
            } else {
                messages = folder.getMessages(folder.getMessageCount() - size + 1, folder.getMessageCount());
            }

            List<Message> imapMessages = Arrays.asList(messages);

            Collections.reverse(imapMessages);

            return imapMessages;

        } catch (MessagingException e) {
            logger.warn("Impossible to get Mails");
        }

        return new ArrayList<Message>();
    }

    @Override
    public List<Message> getMails() {

        return getMails(0);

    }

    @Override
    public IMAPFolder getMailBox(String storeString) throws MessagingException {

        if (lastIMAPFolder != null && lastIMAPFolder.getName().equals(storeString)) {
            return lastIMAPFolder;
        } else if (lastIMAPFolder != null) {
            lastIMAPFolder.close(false);
        }

        lastIMAPFolder = (IMAPFolder) getStore().getFolder(storeString);

        if (!lastIMAPFolder.isOpen()) {
            lastIMAPFolder.open(Folder.READ_ONLY);//READ_WRITE
        }
        return lastIMAPFolder;

    }

    private NotificationMsg mailReceivedNotification(final Message msg) {
        return new NotificationMsg() {

            @Override
            public CoreObjectSpec getSource() {
                return null;
            }

            @Override
            public String getNewValue() {
                return null;
            }

            @SuppressWarnings({"rawtypes", "finally"})
            @Override
            public JSONObject JSONize() {

                JSONObject result = new JSONObject();

                try {

                    result.put("objectId", serviceId);
                    result.put("varName", "newMail");
                    result.put("value", USER);

                    result.put("subject", msg.getSubject());
                    result.put("from", msg.getFrom());

                    result.put("message-id", msg.getHeader("message-id")[0]);

                    List<String> list = new ArrayList<String>();

                    Enumeration enume = msg.getAllHeaders();

                    while (enume.hasMoreElements()) {
                        String param = (String) enume.nextElement();
                        System.out.println(param);
                        list.add(param);
                    }

                    result.put("headers", list);

                } catch (MessagingException e) {
                    e.printStackTrace();
                } finally {
                    return result;
                }

            }
        };
    }

    @Override
    public Calendar getLastFetchDateTime() {
        return lastFetchDateTime;
    }

    public void autoRefreshValueChanged(Object newValue) {
        refreshtask.cancel();
        logger.trace("Auto-refresh changed to: {}", refreshRate);
        configureAutoRefreshTask();

    }

    @Override
    public String getAbstractObjectId() {
        return serviceId;
    }

    @Override
    public String getUserType() {
        return userType;
    }

    @Override
    public int getObjectStatus() {
        return Integer.parseInt(status);
    }

    @Override
    public String getPictureId() {
        return "";
    }

    @Override
    public JSONObject getDescription() throws JSONException {
        JSONObject descr = new JSONObject();

        descr.put("id", serviceId);
        descr.put("type", userType); //102 for mail
        descr.put("status", status);
        descr.put("user", USER);
        descr.put("refreshRate", refreshRate);

        return descr;
    }

    @Override
    public void setPictureId(String pictureId) {
    }

    @Override
    public CORE_TYPE getCoreType() {
        return CORE_TYPE.SERVICE;
    }

}
