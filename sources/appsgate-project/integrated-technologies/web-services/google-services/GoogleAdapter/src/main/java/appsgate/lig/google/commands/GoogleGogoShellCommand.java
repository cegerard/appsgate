package appsgate.lig.google.commands;

import java.io.File;
import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.service.command.Descriptor;
import org.json.JSONObject;

import appsgate.lig.google.helpers.GoogleOpenAuthent;
import appsgate.lig.google.helpers.GooglePropertiesHolder;
import appsgate.lig.google.services.GoogleAdapter;


@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true, name = "appsgate.google.shell")
@Provides(specifications = GoogleGogoShellCommand.class)
/**
 * Gogo command shell to configure googleWebServices
 * @author Thibaud
 */
public class GoogleGogoShellCommand {

	@ServiceProperty(name = "osgi.command.scope", value = "google")
	String gogoShell_groupName;

	@ServiceProperty(name = "osgi.command.function", value = "{}")
	String[] gogoShell_groupCommands = new String[] { "getDeviceAuthCode",
			"authDevice", "getAccessCode", "loadConfiguration", "storeConfiguration", "showConfiguration" };


	PrintStream out = System.out;

	@Descriptor("get a user code and a verification url for the current application (the end-user must uses these in a web browser prior to authDevice)"
			+ "\n in order to use several google services (according to the service scopes, for instance )")
	public void getDeviceAuthCode(@Descriptor("scope1 scope2 ... scopeN") String... args) {
		
		if(!GooglePropertiesHolder.getProperties().containsKey(GoogleOpenAuthent.PARAM_CLIENTID)) {
			out.println("Error, No property defined for "+GoogleOpenAuthent.PARAM_CLIENTID);
			return;
		}
		
		if(args==null || args.length==0) {
			out.println("Error, No scopes provided for applications, there is no need for authorization ");
			return;
		}
		
		String scopes = new String();

		for(String s:args) {
			scopes+=" "+s;
		}
		
		JSONObject result = GoogleOpenAuthent.getDeviceCode(
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTID),
				scopes);
		
		if(result!=null) {
			if(result.has(GoogleOpenAuthent.RESP_ERROR)) {
				out.println("Error, Error occured :"+result.optString(GoogleOpenAuthent.RESP_ERROR));
			} else {
				out.println("Request success, response : "+result.toString());
				out.println("Please connect to : "+result.optString(GoogleOpenAuthent.RESP_VERIFICATIONURL));
				out.println("and enter user code : "+result.optString(GoogleOpenAuthent.RESP_USERCODE));
				
				out.println("When Authorization granted, use device code : "
						+result.optString(GoogleOpenAuthent.RESP_DEVICECODE)
						+" to get an access code");
			}
		} else {
			out.println("Error, Result of the request is null");
		}
	}

	@Descriptor("use an authorized device code to get an access token and a refresh token")
	public void authDevice(@Descriptor("device_code") String... args) {
		
		if(!GooglePropertiesHolder.getProperties().containsKey(GoogleOpenAuthent.PARAM_CLIENTID)) {
			out.println("Error, No property defined for "+GoogleOpenAuthent.PARAM_CLIENTID);
			return;
		}
		
		if(!GooglePropertiesHolder.getProperties().containsKey(GoogleOpenAuthent.PARAM_CLIENTSECRET)) {
			out.println("Error, No property defined for "+GoogleOpenAuthent.PARAM_CLIENTSECRET);
			return;
		}
		
		if(args==null || args.length==0) {
			out.println("Error, No device code provided");
			return;
		}
		
		JSONObject result = GoogleOpenAuthent.authDevice(
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTID),
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTSECRET),
				args[0]);		
		
		if(result!=null) {
			if(result.has(GoogleOpenAuthent.RESP_ERROR)) {
				out.println("Error, Error occured :"+result.optString(GoogleOpenAuthent.RESP_ERROR));
			} else {
				out.println("Request success, response : "+result.toString());
				out.println("A new access token has been generated, type : "
						+result.optString(GoogleOpenAuthent.RESP_TOKENTYPE));
				GooglePropertiesHolder.getProperties().put(
						GoogleOpenAuthent.RESP_TOKENTYPE, 
						result.optString(GoogleOpenAuthent.RESP_TOKENTYPE));
				out.println("Refresh token : "
						+result.optString(GoogleOpenAuthent.RESP_REFRESHTOKEN));
				GooglePropertiesHolder.getProperties().put(
						GoogleOpenAuthent.RESP_REFRESHTOKEN, 
						result.optString(GoogleOpenAuthent.RESP_REFRESHTOKEN));
				out.println("Access token value : "
						+result.optString(GoogleOpenAuthent.RESP_ACCESSTOKEN));
				out.println("Expires in : "
						+result.optString(GoogleOpenAuthent.RESP_EXPIREIN));				
			}
		} else {
			out.println("Error, Result of the request is null");
		}
	}

	@Descriptor("use an refresh token to get a new access token")
	public void getAccessCode(@Descriptor("refresh_token") String... args) {
		
		if(!GooglePropertiesHolder.getProperties().containsKey(GoogleOpenAuthent.PARAM_CLIENTID)) {
			out.println("Error, No property defined for "+GoogleOpenAuthent.PARAM_CLIENTID);
			return;
		}
		
		if(!GooglePropertiesHolder.getProperties().containsKey(GoogleOpenAuthent.PARAM_CLIENTSECRET)) {
			out.println("Error, No property defined for "+GoogleOpenAuthent.PARAM_CLIENTSECRET);
			return;
		}
		

		String refreshToken=null;
		
		if(args==null || args.length==0) {
			out.println("No refresh token provided, trying to get an already configured one");
			if(!GooglePropertiesHolder.getProperties().containsKey(GoogleOpenAuthent.PARAM_REFRESHTOKEN)) {
				out.println("No refresh token aleardy configured");
			} else {
				refreshToken = GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_REFRESHTOKEN);
			}
		} else {
			refreshToken = args[0];
		}
		
		if(refreshToken==null) {
			out.println("No refresh token found, aborting !");
			return;
		}
		JSONObject result = GoogleOpenAuthent.getAccessToken(
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTID),
				GooglePropertiesHolder.getProperties().getProperty(GoogleOpenAuthent.PARAM_CLIENTSECRET),
				refreshToken);		
		
		if(result!=null) {
			if(result.has(GoogleOpenAuthent.RESP_ERROR)) {
				out.println("Error, Error occured :"+result.optString(GoogleOpenAuthent.RESP_ERROR));
			} else {
				out.println("Request success, response : "+result.toString());
				out.println("A new access token has been generated, type : "
						+result.optString(GoogleOpenAuthent.RESP_TOKENTYPE));
				out.println("Access token value : "
						+result.optString(GoogleOpenAuthent.RESP_ACCESSTOKEN));
				out.println("Expires in : "
						+result.optString(GoogleOpenAuthent.RESP_EXPIREIN));				
			}
		} else {
			out.println("Error, Result of the request is null");
		}
	}
	
	

	
	@Descriptor("load or reload a configuration file "
			+ "(if no filename it will use  the predefined property : '"+GooglePropertiesHolder.CONFIGURATION_FILE+"')")
	public void loadConfiguration(@Descriptor("[fileName]") String... args) {
		
		String fileName=getFileName(args);
		
		if(fileName==null) {
			out.println("No file to open, aborting !");
			return;
		}
		GooglePropertiesHolder.loadFromFile(fileName);
	}	
	
	@Descriptor("store a configuration file "
			+ "(if no filename it will use  the predefined property : '"+GooglePropertiesHolder.CONFIGURATION_FILE+"')")
	public void storeConfiguration(@Descriptor("[fileName]") String... args) {
		
		String fileName=getFileName(args);
		
		if(fileName==null) {
			out.println("No file to open, aborting !");
			return;
		}
		GooglePropertiesHolder.storeToFile(fileName);
	}	
	
	@Descriptor("show current configuration for Google WebServices")
	public void showConfiguration(@Descriptor("none") String... args) {
		for(Object key:GooglePropertiesHolder.getProperties().keySet()) {
			out.println(key.toString()+" = "+GooglePropertiesHolder.getProperties().get(key));
		}
		if(GooglePropertiesHolder.isInitialConfig()) {
			out.println("Initial configuration properties are defined (but may be not valid)");
		} else {
			out.println("Missing initial configuration properties");
		}		
		
		if(GooglePropertiesHolder.isCompleteConfig()) {
			out.println("Configuration should be complete (but may be not valid)");
		} else {
			out.println("Configuration is not complete (missing refresh token ?)");
		}

	}	
	
	private String getFileName(String... args) {
		String fileName=null;
		if(args==null || args.length==0) {
			out.println("no fileName specified, trying to get property "+ GooglePropertiesHolder.CONFIGURATION_FILE);
			if(!GooglePropertiesHolder.getProperties().containsKey(GooglePropertiesHolder.CONFIGURATION_FILE)) {
				out.println("found fileName  "+fileName);
				fileName=GooglePropertiesHolder.getProperties().getProperty(GooglePropertiesHolder.CONFIGURATION_FILE);
			} else {
				out.println("no property "+ GooglePropertiesHolder.CONFIGURATION_FILE+" found");
			}
			
		} else {
			fileName = args[0];
		}
		
		if(fileName==null) {
			out.println("No fileName found");
		}
		return fileName;
	}	

}
