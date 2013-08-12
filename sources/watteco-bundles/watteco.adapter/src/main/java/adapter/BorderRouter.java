package adapter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import adapter.commands.BorderRouterCommand;
import adapter.interfaces.BorderRouterServices;

/**
 * A class representing a border router.
 * 
 * @author thalgott
 */
@Component
@Instantiate
@Provides(specifications = { BorderRouterServices.class })
public class BorderRouter implements BorderRouterServices {
	
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
	 * 							 PUBLIC FUNCTIONS                            *
	 *********************************************************************** */

	public byte[] sendCommand(InetAddress addr, BorderRouterCommand c) {
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
		
		// create the sockets
		try {
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
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		
		return result;
	}
	
	/* ***********************************************************************
	 * 							PRIVATE FUNCTIONS                            *
	 *********************************************************************** */
	
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
