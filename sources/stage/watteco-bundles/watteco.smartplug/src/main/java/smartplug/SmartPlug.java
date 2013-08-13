package smartplug;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import adapter.commands.BorderRouterCommand;
import adapter.interfaces.BorderRouterServices;

import smartplug.data.SmartPlugValue;
import smartplug.interfaces.SmartPlugServices;

@Component
@Provides(specifications = { SmartPlugServices.class })
public class SmartPlug implements SmartPlugServices {

	/** the IPv6 address of this smartplug */
	private InetAddress address;
	/** the main border router */
	@Requires
	private BorderRouterServices br;
	
	/* ***********************************************************************
	 * 							 PUBLIC FUNCTIONS                            *
	 *********************************************************************** */
	
	public void toggle() {
		br.sendCommand(address, BorderRouterCommand.SP_TOGGLE);
	}
	
	public SmartPlugValue readAttribute() {
		byte[] b = null;
		b = br.sendCommand(address, BorderRouterCommand.SP_READ_ATTRIBUTE);
		return extractValues(b);
	}
	
	/* ***********************************************************************
	 * 							PRIVATE FUNCTIONS                            *
	 *********************************************************************** */
	
	private SmartPlugValue extractValues(byte[] ba) {
		// TODO: unsure, doc inaccurate
		SmartPlugValue spv = new SmartPlugValue();
		Byte b;
		
		// calculation of the 'summation of the active energy in W.h'
		b = new Byte(ba[9]);
		spv.activeEnergy  = (b << 16);
		b = new Byte(ba[10]);
		spv.activeEnergy += (b << 8);
		b = new Byte(ba[11]);
		spv.activeEnergy += b;
		
		// calculation of the 'number of sample'
		b = new Byte(ba[15]);
		spv.nbOfSamples  = (b << 8);
		b = new Byte(ba[16]);
		spv.nbOfSamples += b;
		
		// calculation of the 'active power in W'
		b = new Byte(ba[17]);
		spv.activePower = (b << 8);
		b = new Byte(ba[18]);
		spv.activePower += b;
		
		return spv;
	}
	
	/* ***********************************************************************
	 * 							    ACCESSORS                                *
	 *********************************************************************** */
	
	public InetAddress getAddress() {
		return this.address;
	}
	
	public void setAddress(String addr) {
		try {
			this.address = InetAddress.getByName(addr);
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
