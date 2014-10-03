package appsgate.lig.mail.adapter;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.mail.Mail;
import appsgate.lig.mail.javamail.MailConfiguration;
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;

import org.apache.felix.service.command.Descriptor;

/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author thibaud
 */
@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true)
@Provides(specifications = MailCommand.class)
public class MailCommand {

	@Requires
	Apam apam;

	@ServiceProperty(name = "osgi.command.scope", value = "mail commands")
	String universalShell_groupName;

	@ServiceProperty(name = "osgi.command.function", value = "{}")
	String[] universalShell_groupCommands = new String[] {
        "addMail",		
            "mailShow",
            "mailSend",
            "mailFetch",
            "mailInfo"
    };

    PrintStream out = System.out;
    
    @Descriptor("create a new mail instance using configuration property file")
    public void addMail(@Descriptor("[fileName]") String... args) throws  MessagingException{

		String fileName=null;
    	
		if(args != null && args.length == 1) {
			fileName=args[0];
		}
		
		if(fileName==null) {
			out.println("No configuration file to open, aborting !");
		} else {
			out.println("Trying to create mail account using "+fileName);
			MailAdapter.loadAndCreateMail(fileName);
		}
		
	}
    

    @Descriptor("show last 5 mails")
    public void mailShow(@Descriptor("none") String... args) throws  MessagingException{

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);
		
		Integer size=Integer.parseInt(getArgumentValueDefault("-size","5", args));

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("CoreMailSpec"))
					continue;
				instances.add(instance);
			}
		}

		for (Instance inst : instances) {

			Mail mail = (Mail) inst.getServiceObject();

			out.println(String.format("-- Mailbox (only last %d are shown up) --",size));

			for (Message message:mail.getMails(size)) {
				
				List froms = Arrays.asList(message.getFrom());
				out.println("Start Message from :" + froms);
				out.println("\t> Subject :" + message.getSubject());
				out.println(String.format("\t> Sent: %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS",message.getSentDate()));

			}

			out.println("-- /Mailbox --");

		}

	}

    @Descriptor("send mail")
    public void mailSend(@Descriptor("none") String... args)
			throws AddressException, MessagingException {

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("CoreMailSpec"))
					continue;
				instances.add(instance);
			}
		}

		for (Instance inst : instances) {

			Mail mailService = (Mail) inst.getServiceObject();

			String to = getArgumentValue("-to", args);
			String subject = getArgumentValue("-subject", args);
			String body = getArgumentValue("-body", args);

			if (to == null) {
				out.println("'-to' argument should be specified");
				return;
			}
			if (subject == null) {
				out.println("'-subject' argument should be specified");
				return;
			}

			mailService.sendMailSimple(to, subject, body);
            out.println("Message successfully sent");

		}

	}

    @Descriptor("fetch mail from the mail server")
    public void mailFetch(@Descriptor("none") String... args)
			throws AddressException, MessagingException {

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("CoreMailSpec"))
					continue;
				instances.add(instance);
			}
		}

		for (Instance inst : instances) {

			Mail mailService = (Mail) inst.getServiceObject();

			mailService.fetch();
			
			out.println("fetch performed.");

		}

	}

    @Descriptor("shows information about mail service")
    public void mailInfo(@Descriptor("none") String... args) {

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("CoreMailSpec"))
					continue;
				instances.add(instance);
			}
		}
		
		for (Instance instance : instances) {

			Mail mailService = (Mail) instance.getServiceObject();
			
			out.println("-- Mail Info --");
			out.println(String.format("\tApam instance name %s",instance.getName()));
			out.println(String.format("\tLast fetch was done at %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS",mailService.getLastFetchDateTime()));
			out.println(String.format("\tUsername %s",instance.getProperty("user")));
			out.println(String.format("\tAuto-refresh every %s ms",instance.getProperty("auto-refresh")));
			out.println("-- /Mail Info --");
		}
		
	}
	
	private String getArgumentValueDefault(String option, String defaults, String... params){
		
		String value=getArgumentValue(option,params);
		
		return value==null?defaults:value;
		
	}
	
	private String getArgumentValue(String option, String... params) {

		boolean found = false;
		String value = null;

		for (int i = 0; i < params.length; i++) {
			if (i < (params.length - 1) && params[i].equals(option)) {
				found = true;
				value = params[i + 1];
				break;
			}
		}

		if (found)
			return value;

		return null;
	}

}
