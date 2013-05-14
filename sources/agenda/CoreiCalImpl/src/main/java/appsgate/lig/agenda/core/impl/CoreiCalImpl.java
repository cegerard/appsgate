package appsgate.lig.agenda.core.impl;

import net.fortuna.ical4j.model.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.agenda.core.messages.AlarmNotificationMsg;
import appsgate.lig.agenda.core.messages.EventNotificationMsg;
import appsgate.lig.logical.object.messages.NotificationMsg;
import appsgate.lig.proxy.agenda.interfaces.AgendaAdapter;

/**
 * This class is used as a core agenda. An instance of this class will match a remote agenda on
 * the cloud
 * 
 * @author Cédric Gérard
 * @since May 14, 2013
 * @version 0.0.1
 * 
 */
public class CoreiCalImpl {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(CoreiCalImpl.class);
	
	/**
	 * The adapter for Google account
	 */
	private AgendaAdapter Adapter;
	
	/**
	 * The name of the corresponding remote agenda
	 */
	private String agendaName;
	
	/**
	 * Remote account credentials
	 */
	private String account;
	private String pswd;
	
	/**
	 * The iCal local representation of the remote agenda
	 */
	Calendar calendar;
	
	/**
	 * Called by APAM when an instance of this implementation is created
	 */
	public void newInst() {
		logger.info("New core agenda instanciated, "+agendaName);
		calendar = Adapter.getAgenda(agendaName, account, pswd);
		logger.debug("URL for private access to data:"+calendar.getProperty("URL").getValue());
		logger.debug("Name of remote agenda: "+calendar.getProperty("NAME").getValue());
	}

	/**
	 * Called by APAM when an instance of this implementation is removed
	 */
	public void deleteInst() {
		logger.info("A core agenda instance desapeared, "+agendaName);
	}
		
	/**
	 * This method uses the ApAM message model. Each call produce a
	 * AlarmNotificationMsg or EventNotificationMsg object, that notify ApAM that a new message has
	 * been released.
	 * 
	 * @return nothing, it just notifies ApAM that a new message has been
	 *         posted.
	 */
	public NotificationMsg notifyEventAlarm(Byte type) {
		if(type == 0) {
			return new EventNotificationMsg();
		} else {
			return new AlarmNotificationMsg();
		}
	}


}
