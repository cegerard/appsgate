package watteco.border_router;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import watteco.sensors.Sensor;
import watteco.sensors.SmartPlug;
import watteco.sensors.SmartPlugValue;

/**
 * A class representing a border router.
 * 
 * @author thalgott
 */
public class BorderRouter {
	
	/* ***********************************************************************
	 * 							    ATTRIBUTES                               *
	 *********************************************************************** */

	/** Port to which values and feedback information is sent */
	private static final int RC_FROM_PORT = 47002;
	/** Port to which the transmitter has to be bound (border router spec.) */
	private static final int BINDING_PORT = 47004;
	/** Port to which commands are sent to the border router */
	private static final int SEND_TO_PORT = 47005;
	
	/** Size of the buffer containing commands to be sent */
	private static final int SEND_BUFFER_SIZE = 2048;
	/** Size of the buffer containing returned values */
	private static final int RECV_BUFFER_SIZE = 1024;
	
	/* ***********************************************************************
	 * 							   CONSTRUCTORS                              *
	 *********************************************************************** */
	
	/**
	 * Constructs a new Border Router.<br>
	 * <br>
	 * NB: Since only one border router is used in this project and since it
	 * needs to be connected on '/dev/ttyUSB0', no specific attribute has to be
	 * specified yet.
	 */
	public BorderRouter() {
		
	}
	
	/* ***********************************************************************
	 * 							 PUBLIC FUNCTIONS                            *
	 *********************************************************************** */
	
	public void toggle(SmartPlug sp) {
		try {
			sendCommand(sp, BorderRouterCommand.SP_TOGGLE);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public SmartPlugValue readAttribute(SmartPlug sp) {
		byte[] b = null;
		try {
			b = sendCommand(sp, BorderRouterCommand.SP_READ_ATTRIBUTE);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
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
	
	private byte[] sendCommand(Sensor s, BorderRouterCommand c) throws IOException {

		byte[] result = null;
		// array containing the data to be sent/received
		byte[] sendData 			= new byte[SEND_BUFFER_SIZE];
		byte[] recvData 			= new byte[RECV_BUFFER_SIZE];
		// sockets created for sending/receiving data
		DatagramSocket sender 		= null;
		DatagramSocket recver		= null;
		DatagramPacket sendPacket 	= null;
		DatagramPacket recvPacket	= null;
		
		sendData 					= stringToHexBuffer(c.getValue());
		InetAddress addr			= s.getAddress();
		
		// create the sockets
		sender 		= new DatagramSocket(BINDING_PORT);
		recver		= new DatagramSocket(RC_FROM_PORT);
		// create the data packets
		sendPacket 	= new DatagramPacket(sendData, sendData.length, addr, 
				SEND_TO_PORT);
		recvPacket	= new DatagramPacket(recvData, recvData.length);
		
		// debug purpose only: display the command to be sent
		displayCommand(sendPacket);
		
		// send the command
		sender.send(sendPacket);
		// wait for the answer, if any
		if (c.sendsBack()) {
			recver.receive(recvPacket);
			result = recvPacket.getData();
			// debug purpose only: display the received data
			displayReceivedData(recvPacket.getData(), recvPacket.getLength());
		}
		// close the sockets
		sender.close();
		recver.close();
		
		return result;
	}
	
	private static byte[] stringToHexBuffer(String s) {
	
		List<Byte> list = new ArrayList<Byte>();
		String sub 		= new String();
		byte[] res;
		int value;
		
		try {
			for (int i = 0; i < s.length(); i++) {
				
				switch (s.charAt(i)) {
				case '$':
					if (!sub.isEmpty()) {
						value = Integer.parseInt(sub, 16);
						list.add(Integer.valueOf(value).byteValue());
						sub = new String();
					}
					break;
				default:
					sub = sub.concat(String.valueOf(s.charAt(i)));
				}
			}
			value = Integer.parseInt(sub, 16);
			list.add(Integer.valueOf(value).byteValue());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		res = new byte[list.size()];
		for (int i = 0; i < res.length; i++)
			res[i] = list.get(i);

		return res;
	}
	
	/* ***********************************************************************
	 * 							    DEBUGGING                                *
	 *********************************************************************** */
	
	private static void displayCommand(DatagramPacket p) {
		String st = "0x";
		String temp = new String();
		for (int i = 0; i < p.getData().length; i++) {
			temp = Integer.toHexString(p.getData()[i]);
			if (temp.length() == 1)
				temp = '0' + temp;
			st += temp + '.';
		}
		st = st.substring(0, st.length()-1);
		String t = p.getAddress().toString() + "::" +
				p.getPort();
		System.out.println("Sending message '" + st + "' at " + t);
	}
	
	private static void displayReceivedData(byte[] bytes, int length) {
		System.out.println("RECEIVED: ");
		
		for (int i = 0; i < length; i++) {
			// less efficient, does not print heading '0' (e.g. 0x0F is printed 'F')
			//System.out.print('$' + Integer.toHexString(bytes[i]));
			
			// more efficient, equivalent to C-function `printf("%02X", bytes[i])`
			System.out.print('$' + byteToHexString(bytes[i]));
		}
		System.out.println();
	}
	
	private static String byteToHexString(byte b) {
		
		final char[] hexArray = {'0','1','2','3','4','5','6','7',
								 '8','9','A','B','C','D','E','F'};
		char[] hex = new char[2];
		int v = b & 0xFF;
		hex[0] = hexArray[v >>> 4];
		hex[1] = hexArray[v & 0x0F];
		return new String(hex);
	}
}
