package appsgate.lig.mail.javamail;

import java.util.Properties;

/**
 * Created by thibaud on 25/08/2014.
 */
public class MailConfiguration extends Properties {

    public static String PROVIDER="";

    // Those constants are compatible with JavaMail API
    public final static String MAIL_PREFIX = "mail.";
    public final static String HOST_SUFFIX = ".host";
    public final static String PORT_SUFFIX = ".port";
    public final static String USER_SUFFIX = ".user";
    public final static String CLASS_SUFFIX = ".class";
    public final static String FROM = "mail.from";
    public final static String USER = "mail.user";
    public final static String DEFAULT_FOLDER = "mail.folder";


    public final static String STORE_PROTOCOL = "mail.store.protocol";
    public final static String TRANSPORT_PROTOCOL = "mail.transport.protocol";

    //Those constants are specific to Appsgate
    public final static String STORE_HOST = "store.host";
    public final static String STORE_PORT = "store.port";

    public final static String TRANSPORT_HOST = "transport.host";
    public final static String TRANSPORT_PORT = "transport.port";
    public final static String PASSWORD = "mail.password";

}
