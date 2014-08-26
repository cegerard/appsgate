package appsgate.lig.mail.javamail;

import appsgate.lig.mail.javamail.utils.JSSEProvider;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;

/**
 * Created by thibaud on 21/07/2014.
 */
public class MailSender extends javax.mail.Authenticator {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(MailSender.class);

    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(Session session) {
        logger.debug("new MailSender(...)");

        this.session = session;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(session.getProperty(MailConfiguration.USER),
                session.getProperty(MailConfiguration.PASSWORD));
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        logger.debug("sendMail(String subject: {}, String body: {}, String sender: {}, String recipients: {})"
                ,subject,body,sender, recipients);
        try{
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
            logger.debug("sendMail(...), mail successfully sent");
        }catch(Exception e){
            logger.error("Error sending mail : "+e.getMessage());
        }
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}
