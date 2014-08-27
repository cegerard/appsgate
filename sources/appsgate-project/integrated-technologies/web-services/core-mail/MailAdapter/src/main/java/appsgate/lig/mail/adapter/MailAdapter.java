package appsgate.lig.mail.adapter;

import appsgate.lig.mail.javamail.GMailConstants;
import appsgate.lig.mail.javamail.MailService;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class MailAdapter {

    Set<MailService> mailInstances;

    public void createMailInst(String mailProvider, String login, String passwd) {
        try {
            Implementation mailImpl = CST.componentBroker.getImpl("MailService");

            Map<String,String> configuration = new Hashtable<String,String>();


            MailService impl = (MailService) mailImpl.createInstance(null, configuration).getServiceObject();
            if(GMailConstants.PROVIDER.equals(mailProvider)) {
                impl.setConfiguration(new GMailConstants());
            }
            impl.setAccount(login, passwd);
            impl.start();

        } catch( Exception exc) {
            System.err.println("Exception when creating Mail for "+" : "+exc.getMessage());
            exc.printStackTrace();
        }

    }

    public void start() {
        createMailInst(GMailConstants.PROVIDER,"smarthome.inria@gmail.com", "smarthome2014");
        createMailInst(GMailConstants.PROVIDER,"smarthome.adele@gmail.com", "smarthome2014");
    }
    
    public void stop() {
        
    }
			
}
