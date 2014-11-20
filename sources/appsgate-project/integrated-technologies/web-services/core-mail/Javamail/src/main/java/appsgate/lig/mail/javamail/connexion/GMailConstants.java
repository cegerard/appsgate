package appsgate.lig.mail.javamail.connexion;



public class GMailConstants extends MailConfiguration{

    public static String PROVIDER="GMail";


    public static final String DEFAULT_INBOX = "inbox";

    public static final String IMAP_SERVER = "imap.gmail.com";
    public static final int IMAP_PORT = 993;

    public static final String SMTP_SERVER = "smtp.gmail.com";
    public static final int SMTP_PORT = 465;

    public static final String STORE_PROTOCOL_VALUE="imaps";

    public static final String TRANSPORT_PROTOCOL_VALUE="smtp";


    //props.setProperty("mail.host", mailhost);

        public GMailConstants() {

            // Generic properties applicable to gmails
            this.put(STORE_PROTOCOL, STORE_PROTOCOL_VALUE);
            this.put(STORE_HOST, IMAP_SERVER);
            this.put(STORE_PORT, String.valueOf(IMAP_PORT));

            this.put(TRANSPORT_PROTOCOL, TRANSPORT_PROTOCOL_VALUE);
            this.put(TRANSPORT_HOST, SMTP_SERVER);
            this.put(TRANSPORT_PORT, String.valueOf(SMTP_PORT));

            this.put(MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+HOST_SUFFIX, SMTP_SERVER);
            this.put(MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+PORT_SUFFIX, String.valueOf(SMTP_PORT));

            this.put(MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+".auth", "true");
            this.put(MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+".starttls.enable", "true");

            // This one allows to connect
            this.put( MAIL_PREFIX+STORE_PROTOCOL_VALUE+".socketFactory.class",
                    "appsgate.lig.mail.gmail.utils.AppsGateSSLSocketFactory" );

            this.put( MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+".socketFactory.port",
                    SMTP_PORT );
            this.put( MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+".socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory" );
            this.put( MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+".socketFactory.fallback",
                    "false" );
            this.put( MAIL_PREFIX+TRANSPORT_PROTOCOL_VALUE+".quitwait",
                    "false" );

            this.put(DEFAULT_FOLDER, DEFAULT_INBOX);
		}

}
