package appsgate.lig.agenda.core.command;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import appsgate.lig.proxy.agenda.interfaces.AgendaAdapter;

/**
 * Universal shell to test agenda manager 
 * @author jnascimento
 *
 */
@Instantiate
@Component(public_factory = false, immediate = true, name = "apam.universal.shell")
@Provides(specifications = AgendaShell.class)
public class AgendaShell {

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.id", value = "apam")
	String universalShell_groupID;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.group.name", value = "Calendar commands")
	String universalShell_groupName;

	@ServiceProperty(name = "org.knowhowlab.osgi.shell.commands", value = "{}")
	String[] universalShell_groupCommands = new String[] { "createEvent#sintax: AgendaName Summary 'ddMMyyyy HH:mm' 'ddMMyyyy HH:mm'",
		"deleteEvent#sintax: AgendaName Summary 'ddMMyyyy HH:mm' 'ddMMyyyy HH:mm'"};

	@Requires
	AgendaAdapter agenda;

	public void createEvent(PrintWriter out, String... args) {

		try {
		
		java.util.Calendar calbegin = java.util.Calendar.getInstance();
		java.util.Calendar calend = java.util.Calendar.getInstance();
		
		String summary=null;
		String agendaName=null;
		
		if(args.length>0){
			agendaName=args[0];
		}else {
			out.println("invalid argument");
			return;
		}
		
		if(args.length>1){
			summary=args[1];
		}else {
			out.println("arguments should be given");
			return;
		}
		
		if(args.length>2){
			String dateStartString=args[2];
			DateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm");
			
			Date d = df.parse(dateStartString);
			
			calbegin.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
			calbegin.setTime(d);
		}
			
		if(args.length>3){
			String dateStartString=args[3];
			DateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm");
			
			Date d = df.parse(dateStartString);
			
			calend.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
			calend.setTime(d);
		}else {
			calend.setTime(calbegin.getTime());
			calend.add(Calendar.HOUR, 1);
		}
		
		out.println(String.format("Creating '%s' from %s to %s",summary,calbegin,calend));
		
		DateTime inicio = new DateTime(calbegin.getTimeInMillis());
		DateTime fim = new DateTime(calend.getTimeInMillis());
		VEvent event = new VEvent(inicio, fim, summary);
		
		
		agenda.addEvent(agendaName, "smarthome.inria@gmail.com",
				"smarthome2012", event);

		} catch (ParseException e) {
			out.println("Error with the message:"+e.getMessage());
		}
		
	}

	public void deleteEvent(PrintWriter out, String... args) {

		try {
		
		java.util.Calendar calbegin = java.util.Calendar.getInstance();
		java.util.Calendar calend = java.util.Calendar.getInstance();
		
		String summary=null;
		String agendaName=null;
		
		if(args.length>0){
			agendaName=args[0];
		}else {
			out.println("invalid argument");
			return;
		}
		
		if(args.length>1){
			summary=args[1];
		}else {
			out.println("arguments should be given");
			return;
		}
		
		if(args.length>2){
			String dateStartString=args[2];
			DateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm");
			
			Date d = df.parse(dateStartString);
			
			calbegin.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
			calbegin.setTime(d);
		}
			
		if(args.length>3){
			String dateStartString=args[3];
			DateFormat df = new SimpleDateFormat("ddMMyyyy HH:mm");
			
			Date d = df.parse(dateStartString);
			
			calend.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
			calend.setTime(d);
		}else {
			calend.setTime(calbegin.getTime());
			calend.add(Calendar.HOUR, 1);
		}
		
		out.println(String.format("Creating '%s' from %s to %s",summary,calbegin,calend));
		
		DateTime inicio = new DateTime(calbegin.getTimeInMillis());
		DateTime fim = new DateTime(calend.getTimeInMillis());
		VEvent event = new VEvent(inicio, fim, summary);
		
		agenda.delEvent(agendaName, "smarthome.inria@gmail.com",
				"smarthome2012", event);

		} catch (ParseException e) {
			out.println("Error with the message:"+e.getMessage());
		}
		
	}		
		
}
