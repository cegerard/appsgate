package appsgate.lig.mail.gmail;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Instance;

@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "appsgate.universal.shell")
@Provides(specifications = GmailGogoCommand.class)
/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author jnascimento
 */
public class GmailGogoCommand {

	@Requires
	Apam apam;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "mail")
	String universalShell_groupID;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "mail commands")
	String universalShell_groupName;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
	String[] universalShell_groupCommands = new String[] {
			"mailShow#show last 5 mails", 
			"mailSend#send mail", 
			"mailFetch#fetch mail from the mail server",
			"mailInfo#shows information about mail service"};

	public void mailShow(PrintWriter out, String... args)
			throws MessagingException {

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("mail-service-specification"))
					continue;
				instances.add(instance);
			}
		}

		for (Instance inst : instances) {

			Mail mail = (Mail) inst.getServiceObject();

			out.println(mail.getMails().size());

			out.println("-- Mailbox (only last 10 are shown up) --");

			Iterator it = mail.getMails().iterator();
			int size = 10;

			for (; it.hasNext(); size--) {

				if (size == 0)
					break;

				Message message = (Message) it.next();
				List froms = Arrays.asList(message.getFrom());
				out.println("> From :" + froms);
				out.println("> Subject :" + message.getSubject());
				out.println("----");

			}

			out.println("-- /Mailbox --");

		}

	}

	public void mailSend(PrintWriter out, String... args)
			throws AddressException, MessagingException {

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("mail-service-specification"))
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

		}

	}
	
	public void mailFetch(PrintWriter out, String... args)
			throws AddressException, MessagingException {

		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("mail-service-specification"))
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

	public void mailInfo(PrintWriter out, String... args){
		
		Set<Instance> instances = new HashSet<Instance>();

		String instanceParam = getArgumentValue("-instance", args);

		if (instanceParam != null) {
			Instance instance = CST.componentBroker.getInst(instanceParam);
			instances.add(instance);
		} else {
			for (Instance instance : CST.componentBroker.getInsts()) {
				if (!instance.getSpec().getName()
						.equals("mail-service-specification"))
					continue;
				instances.add(instance);
			}
		}
		
		for (Instance instance : instances) {

			Mail mailService = (Mail) instance.getServiceObject();
			
			out.println("-- Mail Info --");
			out.println(String.format("\tLast fetch was done at %1$te/%1$tm/%1$tY %1$tH:%1$tM:%1$tS",mailService.getLastFetchDateTime()));
			out.println(String.format("\tUsername %s",instance.getProperty("user")));
			out.println(String.format("\tAuto-refresh every %s ms",instance.getProperty("auto-refresh")));
			out.println("-- /Mail Info --");
		}
		
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
