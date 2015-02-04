package appsgate.lig.tts.yakitome.utils;

import java.io.PrintStream;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;

import fr.imag.adele.apam.Apam;
import fr.imag.adele.apam.CST;
import fr.imag.adele.apam.Implementation;
import fr.imag.adele.apam.Instance;

import org.apache.felix.service.command.Descriptor;

import appsgate.lig.tts.CoreTTSService;
import appsgate.lig.tts.yakitome.impl.TTSServiceImpl;

/**
 * Gogo command that helps show retrieve information from the service without having to implement a client
 * @author thibaud
 */
@Instantiate
@org.apache.felix.ipojo.annotations.Component(public_factory = false, immediate = true)
@Provides(specifications = YakitomeCommand.class)
public class YakitomeCommand {

	@Requires
	Apam apam;

	@ServiceProperty(name = "osgi.command.scope", value = "tts")
	String universalShell_groupName;

	@ServiceProperty(name = "osgi.command.function", value = "{}")
	String[] universalShell_groupCommands = new String[] {
	    "getSpeechTextStatus",
	    "deleteSpeechText",
	    "waitForTTSGeneration",
	    "asynchronousTTSGeneration",
	    "getTTSItemMatchingText"
    };

    PrintStream out = System.out;
    
    private CoreTTSService getTTSInst() {
		Implementation implementation = CST.apamResolver.findImplByName(null,TTSServiceImpl.TTS_IMPLEM_NAME);
		if(implementation == null) {
			System.out.println(TTSServiceImpl.TTS_IMPLEM_NAME+" Implementation not found");
			return null;
		}		
		Instance inst = implementation.getInst();
		
		if(inst == null) {
			System.out.println(TTSServiceImpl.TTS_IMPLEM_NAME+" Instance not found");
			return null;
		}
		
		return (CoreTTSService)inst.getServiceObject();
    	
    }
    
    private int getBookIdFormFirstArg(String... args) {
    	int book_id;
		if(args.length<1) {
			System.out.println("No book id provided,"
					+ " calling the service with book_id = 0 (error code value)");
			book_id = 0;
		} else {
			try {
				book_id = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
    			System.out.println("book id provided is not a number,"
    					+ " calling the service with book_id = 0 (error code value)");
    			book_id = 0;
			}
		}
		return book_id;
    }
     
    @Descriptor("get current status of a Text to Speech item (using its book id)")
	public void getSpeechTextStatus(@Descriptor("book_id") String... args) {
    	CoreTTSService tts= getTTSInst();
    	
    	if(tts!= null) {
    		int book_id = getBookIdFormFirstArg(args);
    		tts.getSpeechTextStatus(book_id);
    	}
	}
    
    @Descriptor("delete a Text to Speech item from the server (using its book id)")
	public void deleteSpeechText(@Descriptor("book_id") String... args) {
    	CoreTTSService tts= getTTSInst();
    	
    	if(tts!= null) {
    		int book_id = getBookIdFormFirstArg(args);
    		tts.deleteSpeechText(book_id);
    	}
	}
    
    @Descriptor("check if a Speech item as already been generated upon the text")
	public void getTTSItemMatchingText(@Descriptor("text") String... args) {
    	CoreTTSService tts= getTTSInst();
    	
    	if(tts!= null) {
    		if(args.length>=1) {
        		tts.getTTSItemMatchingText(args[0]);
    		} else {
    			System.out.println("no argument provided");
    		}
    	}
	} 
    
    @Descriptor("Generate TTS and audio file asynchronously, must check the log to see what have been generated")
	public void asynchronousTTSGeneration(@Descriptor("text") String... args) {
    	CoreTTSService tts= getTTSInst();
    	
    	if(tts!= null) {
    		if(args.length>=1) {
        		tts.asynchronousTTSGeneration(args[0]);
    		} else {
    			System.out.println("no argument provided");
    		}
    	}
	}  
    
    @Descriptor("Generate TTS and audio file (blocking method), must check the log to see what have been generated")
	public void waitForTTSGeneration(@Descriptor("text") String... args) {
    	CoreTTSService tts= getTTSInst();
    	
    	if(tts!= null) {
    		if(args.length>=1) {
        		tts.waitForTTSGeneration(args[0]);
    		} else {
    			System.out.println("no argument provided");
    		}
    	}
	}      
}
