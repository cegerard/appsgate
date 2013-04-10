
/*
__BANNER__
*/
// this file was generated at 22-March-2013 11:22 AM by ${author}
package appsgate.lig.upnp.media;

import org.apache.felix.upnp.devicegen.holder.*;
import org.osgi.service.upnp.UPnPException;

	
public interface RenderingControl {	

	
	public java.lang.String getLastChange();
	
	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentPresetNameList out  parameter


	 */
	public void listPresets(
		long instanceID,

StringHolder currentPresetNameList
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * presetName in  parameter


	 */
	public void selectPreset(
		long instanceID,

java.lang.String presetName
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentBrightness out  parameter


	 */
	public void getBrightness(
		long instanceID,

IntegerHolder currentBrightness
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredBrightness in  parameter


	 */
	public void setBrightness(
		long instanceID,

int desiredBrightness
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentContrast out  parameter


	 */
	public void getContrast(
		long instanceID,

IntegerHolder currentContrast
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredContrast in  parameter


	 */
	public void setContrast(
		long instanceID,

int desiredContrast
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentSharpness out  parameter


	 */
	public void getSharpness(
		long instanceID,

IntegerHolder currentSharpness
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredSharpness in  parameter


	 */
	public void setSharpness(
		long instanceID,

int desiredSharpness
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentRedVideoGain out  parameter


	 */
	public void getRedVideoGain(
		long instanceID,

IntegerHolder currentRedVideoGain
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredRedVideoGain in  parameter


	 */
	public void setRedVideoGain(
		long instanceID,

int desiredRedVideoGain
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentGreenVideoGain out  parameter


	 */
	public void getGreenVideoGain(
		long instanceID,

IntegerHolder currentGreenVideoGain
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredGreenVideoGain in  parameter


	 */
	public void setGreenVideoGain(
		long instanceID,

int desiredGreenVideoGain
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentBlueVideoGain out  parameter


	 */
	public void getBlueVideoGain(
		long instanceID,

IntegerHolder currentBlueVideoGain
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredBlueVideoGain in  parameter


	 */
	public void setBlueVideoGain(
		long instanceID,

int desiredBlueVideoGain
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentRedVideoBlackLevel out  parameter


	 */
	public void getRedVideoBlackLevel(
		long instanceID,

IntegerHolder currentRedVideoBlackLevel
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredRedVideoBlackLevel in  parameter


	 */
	public void setRedVideoBlackLevel(
		long instanceID,

int desiredRedVideoBlackLevel
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentGreenVideoBlackLevel out  parameter


	 */
	public void getGreenVideoBlackLevel(
		long instanceID,

IntegerHolder currentGreenVideoBlackLevel
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredGreenVideoBlackLevel in  parameter


	 */
	public void setGreenVideoBlackLevel(
		long instanceID,

int desiredGreenVideoBlackLevel
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentBlueVideoBlackLevel out  parameter


	 */
	public void getBlueVideoBlackLevel(
		long instanceID,

IntegerHolder currentBlueVideoBlackLevel
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredBlueVideoBlackLevel in  parameter


	 */
	public void setBlueVideoBlackLevel(
		long instanceID,

int desiredBlueVideoBlackLevel
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentColorTemperature out  parameter


	 */
	public void getColorTemperature(
		long instanceID,

IntegerHolder currentColorTemperature
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredColorTemperature in  parameter


	 */
	public void setColorTemperature(
		long instanceID,

int desiredColorTemperature
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentHorizontalKeystone out  parameter


	 */
	public void getHorizontalKeystone(
		long instanceID,

IntegerHolder currentHorizontalKeystone
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredHorizontalKeystone in  parameter


	 */
	public void setHorizontalKeystone(
		long instanceID,

int desiredHorizontalKeystone
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * currentVerticalKeystone out  parameter


	 */
	public void getVerticalKeystone(
		long instanceID,

IntegerHolder currentVerticalKeystone
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * desiredVerticalKeystone in  parameter


	 */
	public void setVerticalKeystone(
		long instanceID,

int desiredVerticalKeystone
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * currentMute out  parameter


	 */
	public void getMute(
		long instanceID,

java.lang.String channel,

BooleanHolder currentMute
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredMute in  parameter


	 */
	public void setMute(
		long instanceID,

java.lang.String channel,

boolean desiredMute
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * currentVolume out  parameter


	 */
	public void getVolume(
		long instanceID,

java.lang.String channel,

IntegerHolder currentVolume
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredVolume in  parameter


	 */
	public void setVolume(
		long instanceID,

java.lang.String channel,

int desiredVolume
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * currentVolume out  parameter


	 */
	public void getVolumeDB(
		long instanceID,

java.lang.String channel,

IntegerHolder currentVolume
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredVolume in  parameter


	 */
	public void setVolumeDB(
		long instanceID,

java.lang.String channel,

int desiredVolume
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * minValue out  parameter

 * maxValue out  parameter


	 */
	public void getVolumeDBRange(
		long instanceID,

java.lang.String channel,

IntegerHolder minValue,

IntegerHolder maxValue
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * currentLoudness out  parameter


	 */
	public void getLoudness(
		long instanceID,

java.lang.String channel,

BooleanHolder currentLoudness
	) throws UPnPException;
	// TODO return the type of the return argument when specified


	/**
	 * This method is "add description here"	
 * instanceID in  parameter

 * channel in  parameter

 * desiredLoudness in  parameter


	 */
	public void setLoudness(
		long instanceID,

java.lang.String channel,

boolean desiredLoudness
	) throws UPnPException;
	// TODO return the type of the return argument when specified

	

}
