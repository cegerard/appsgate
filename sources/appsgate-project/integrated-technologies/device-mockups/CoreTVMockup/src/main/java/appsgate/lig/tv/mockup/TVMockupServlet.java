package appsgate.lig.tv.mockup;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.tv.pace.PaceTVImpl;

public class TVMockupServlet extends HttpServlet {	
	
	private final static Logger logger = LoggerFactory
			.getLogger(TVMockupServlet.class);
	
	public String getCurrentStatus() {
		return currentStatus;
	}

	public int getCurrentChannel() {
		return currentChannel;
	}

	public String getLatestNotificationSender() {
		return currentSender;
	}

	public String getLatestNotificationMessage() {
		return currentMessage;
	}

	public String getCurrentScreen() {
		return currentScreen;
	}
	
	public String getLatestCommand() {
		return latestCommand;
	}	

	String currentStatus = "Unknown";
	int currentChannel = 0;
	String currentSender;
	String currentMessage;
	String currentScreen;
	String latestCommand = "Unknown";
	
	  @Override
	  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		  logger.trace("doGet(HttpServletRequest req : {}, HttpServletResponse resp : {})"
				  , req, resp);
		  
		  if(req==null) {
			  logger.warn("doGet(...), missing request");
			  resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			  return;
		  }

		  
		  String currentCommand =req.getParameter(PaceTVImpl.COMMAND_PARAM); 
		  
		  if(currentCommand == null) {
			  logger.warn("doGet(...), missing command parameter");
			  resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			  return;
		  }

		  String id=req.getParameter(PaceTVImpl.ID_PARAM);
		  
		  for(Object param : req.getParameterMap().keySet()) {
			  logger.trace("doGet(...), parameter  {} = {} ",param, req.getParameter(param.toString())
					  +", req.getRequestURI() : "+req.getRequestURI());
		  }
		  
		  if(req.getRequestURI().endsWith(PaceTVImpl.VIDEO)) {
			  
			  logger.trace("doGet(...), receveived a video service request");

			  if (currentCommand.equals(PaceTVImpl.COMMAND_RESUME)){
				  latestCommand=currentCommand;
				  logger.trace("doGet(...), received a resume command, with id ="+id);
				  currentStatus = PaceTVImpl.COMMAND_RESUME;
	
			  } else if (currentCommand.equals(PaceTVImpl.COMMAND_PAUSE)){
				  latestCommand=currentCommand;
				  logger.trace("doGet(...), received a pause command, with id ="+id);
				  currentStatus = PaceTVImpl.COMMAND_PAUSE;
	
			  } else if (currentCommand.equals(PaceTVImpl.COMMAND_STOP)){
				  latestCommand=currentCommand;
				  logger.trace("doGet(...), received a stop command, with id ="+id);
				  currentStatus = PaceTVImpl.COMMAND_STOP;
	
			  } else if (currentCommand.equals(PaceTVImpl.COMMAND_CHANNELUP)){
				  latestCommand=currentCommand;
				  logger.trace("doGet(...), received a channel up command, with id ="+id);
				  currentChannel++;
				  
			  } else if (currentCommand.equals(PaceTVImpl.COMMAND_CHANNELDOWN)){
				  latestCommand=currentCommand;
				  logger.trace("doGet(...), received a channel down command, with id ="+id);
				  currentChannel--;
				  
			  } else if (currentCommand.equals(PaceTVImpl.COMMAND_RESIZE)){
				  latestCommand=currentCommand;
				  currentScreen=req.getParameter(PaceTVImpl.SCREEN_PARAM);
				  logger.trace("doGet(...), received a resize command,"
							+ " screen = "+currentScreen
							+ " with id ="+id);
			  } else if (currentCommand.equals(CoreTVMockupAdapter.MOCKUP_REQUEST)){
				  logger.trace("doGet(...), received a mockup status request (not part of Pace specification),"
							+ " with id ="+id);
				  JSONObject status = new JSONObject();
				  status.put("status",currentStatus);
				  status.put("channel", currentChannel);
				  status.put("screen", currentScreen);
				  status.put("latestCommand", latestCommand);
				  status.put("latestMessage", currentMessage);
				  status.put("latestSender", currentSender);
				  resp.getWriter().write(status.toString());
				  
			  } else {
				  logger.warn("doGet(...), unrecognized command for video service : "+currentCommand);
				  resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				  return;			  
			  }

		  } else if (req.getRequestURI().endsWith(PaceTVImpl.OSD) ) {
		  
			  if (currentCommand.equals(PaceTVImpl.COMMAND_NOTIFY)){
				  latestCommand=currentCommand;
				  currentSender=req.getParameter(PaceTVImpl.SENDER_PARAM);
				  currentMessage=req.getParameter(PaceTVImpl.MESSAGE_PARAM);  
				  logger.trace("doGet(...), received a notify command,"
							+ " sender = "+currentSender
							+ ", message = "+currentMessage
							+ " with id ="+id);
			  } else {
				  logger.warn("doGet(...), unrecognized command for osd service : "+currentCommand);
				  resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				  return;			 
			  }
			  
		  } else if (req.getRequestURI().endsWith(PaceTVImpl.SYSTEM) ) {

			  if (currentCommand.equals(PaceTVImpl.COMMAND_ISALIVE)){
				  latestCommand=currentCommand;
				  logger.trace("doGet(...), received a isAlive command");
				  
	
			  } else {
				  logger.warn("doGet(...), doGet(...), unrecognized command for system service : "+currentCommand);
				  resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				  return;			  
			  }
		  }
		  
		  else {
			  logger.warn("doGet(...), unrecognized request (missing video/system or command parameter)");
			  resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			  return;
		  }
		  
		  resp.setStatus(HttpServletResponse.SC_OK);
	  }	

}
