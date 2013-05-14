package appsgate.lig.agenda.core.impl;

import net.fortuna.ical4j.model.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.proxy.google.agenda.GoogleAdapter;


public class CoreiCalImpl {
	
	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(CoreiCalImpl.class);
	
	/**
	 * The adapter for Google account
	 */
	private GoogleAdapter Adapter;
	
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

}
