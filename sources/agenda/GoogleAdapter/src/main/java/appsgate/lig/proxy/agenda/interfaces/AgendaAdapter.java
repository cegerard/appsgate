package appsgate.lig.proxy.agenda.interfaces;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Generic interface for agenda service
 * @author jnascimento
 */
public interface AgendaAdapter {
	
	public Calendar getAgenda(String agenda, String account, String password, java.util.Date startDate, java.util.Date endDate);
	public VEvent addEvent(String agenda, String account, String password, VEvent newEvent);
	public boolean delEvent (String agenda, String account, String password, VEvent newEvent);
	public boolean addAlarm(VAlarm newAlarm);
	public boolean delAlarm(VAlarm oldAlarm);

}
