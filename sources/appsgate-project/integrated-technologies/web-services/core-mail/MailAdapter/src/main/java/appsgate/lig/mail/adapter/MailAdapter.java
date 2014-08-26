package appsgate.lig.mail.adapter;

import appsgate.lig.mail.gmail.GMailConstants;
import appsgate.lig.mail.gmail.Gmail;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class MailAdapter {

    Set<Gmail> mailInstances;

    public void createMailInst(String mailProvider, String login, String passwd) {
        try {
            Implementation mailImpl = CST.componentBroker.getImpl("GmailImpl");

            Map<String,String> configuration = new Hashtable<String,String>();


            Gmail impl = (Gmail) mailImpl.createInstance(null, configuration).getServiceObject();
//            if(mailProvider.equals("GMail")) {
                impl.setConfiguration(new GMailConstants());
//            }
            impl.setAccount(login, passwd);
            impl.start();

        } catch( Exception exc) {
            System.err.println("Exception when creating Mail for "+" : "+exc.getMessage());
            exc.printStackTrace();
        }

    }

    public void start() {
        createMailInst("Gmail","smarthome.inria@gmail.com", "smarthome2014");
    }
    
    public void stop() {
        
    }
			
}
