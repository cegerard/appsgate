package appsgate.lig.light.actuator.philips.HUE.impl;

import org.json.JSONException;
import org.json.JSONObject;

import appsgate.lig.colorLight.actuator.spec.ColorLightSpec;
import appsgate.lig.logical.object.spec.AbstractObjectSpec;
import appsgate.lig.proxy.PhilipsHUE.interfaces.PhilipsHUEServices;

/**
 * 
 * @author cedric
 *
 */
public class PhilipsHUEImpl implements ColorLightSpec, AbstractObjectSpec {
	
	private PhilipsHUEServices PhilipsBridge;

	@Override
	public JSONObject getLightStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLightColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLightBrightness() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getCurrentState() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public JSONObject getManufacturerDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setStatus(JSONObject newStatus) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean On() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Off() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setColor(long color) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setBrightness(long brightness) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setRed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setBlue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setGreen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setYellow() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setOrange() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setPurple() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setPink() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean increaseBrightness(int step) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean decreaseBrightness(int step) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getAbstractObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserObjectName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocationId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getUserType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getObjectStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPictureId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getDescription() throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserObjectName(String userName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocationId(int locationId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPictureId(String pictureId) {
		// TODO Auto-generated method stub
	}

}
