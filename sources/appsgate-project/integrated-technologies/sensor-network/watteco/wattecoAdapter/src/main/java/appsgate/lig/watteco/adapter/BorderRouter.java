package appsgate.lig.watteco.adapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	
	/** class logger member */
	private static Logger logger = LoggerFactory.getLogger(BorderRouter.class);
	
	/** Executor scheduler for reporting listening */
	private ScheduledExecutorService listenningService;
	
	/** Boolean to control received listening loop */
	private boolean started;
	
	/** list to return result to correct border router command */
	private ArrayList<BorderRouterCommand> returnCallList;
	
	/** Socket to receive Watteco border router message */
	private DatagramSocket recver = null;
	
	
	/* ***********************************************************************
	 * 							 PUBLIC FUNCTIONS                            *
	 *********************************************************************** */
	
	public BorderRouter() {
		super();
		returnCallList = new ArrayList<BorderRouterCommand>();
		listenningService = Executors.newScheduledThreadPool(1);
		started = true;
		listenningService.execute(new ListeningService());
	}
	

	public byte[] sendCommand(BorderRouterCommand c) {
		byte[] result = null;
		// array containing the data to be sent/received
		byte[] sendData 			= new byte[SEND_BUFFER_SIZE];
		// sockets created for sending/receiving data
		DatagramSocket sender 		= null;
		DatagramPacket sendPacket 	= null;
		
		sendData 					= stringToHexBuffer(c.getValue());
		
		// create the sockets
		try {
			sender 		= new DatagramSocket(BINDING_PORT);
			// create the data packets
			sendPacket 	= new DatagramPacket(sendData, sendData.length, c.getAddress(), SEND_TO_PORT);
			// debug purpose only: display the command to be sent
			displayCommand(sendPacket);
			
			// wait for the answer, if any
			if (c.sendsBack()) {
				synchronized(c) {
					addToReturnCallList(c);
					// send the command
					sender.send(sendPacket);
					c.wait(30000);
					result = c.getData();
					removeToReturnCallList(c);
				}
			}else {
				// send the command
				sender.send(sendPacket);
			}
			
			// close the sockets
			sender.close();
		
		} catch (Exception e) {logger.error(e.getMessage());}
		
		return result;
	}
	
	/**
	 * Stop the listening thread of the border router
	 */
	public void stopBorderRouter() {
			started = false;
			listenningService.shutdown();
			try {
				listenningService.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.debug("Watteco border router listening service thread crash at termination");
			}
			if(recver != null && !recver.isClosed()) {
				recver.close();
			}
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
	
	/**
	 * Fin a corresponding border router command to set into the return packet
	 * @param route the sensor ipv6 address
	 * @param idDatagram the packet header
	 * @return the border router command that correspond to the response packet received 
	 */
	private synchronized BorderRouterCommand findReturnCommand(InetAddress addr, String idDatagram) {
		BorderRouterCommand cmd = null;
		
		for(BorderRouterCommand brcmd : returnCallList) {
			if(brcmd.getAddress().getHostAddress().contentEquals(addr.getHostAddress())) {
				if(brcmd.getValue().contains(idDatagram)) {
					cmd = brcmd;
					break;
				}
			}
		}
		return cmd;
	}
	
	private synchronized void  addToReturnCallList (BorderRouterCommand c) {
		returnCallList.add(c);
	}
	
	private synchronized void  removeToReturnCallList (BorderRouterCommand c) {
		returnCallList.remove(c);
	}
	
	/* ***********************************************************************
	 * 						   	 INNER CLASS                                 *
	 *********************************************************************** */
	
	/**
	 * Inner class for sensor reporting listening
	 * @author Cédric Gérard
	 * @since August 23, 2013
	 * @version 1.0.0
	 */
	private class ListeningService implements Runnable {
		
		public ListeningService() {
			super();
		}

		public void run() {
			logger.info("Watteco reporting service ON.");
			byte[] result				= null;
			byte[] recvData 			= new byte[RECV_BUFFER_SIZE];
			DatagramPacket recvPacket	= new DatagramPacket(recvData, recvData.length);
			
			try{
				recver		= new DatagramSocket(RC_FROM_PORT);
				//recver.setSoTimeout(20000);
				while (started) {
					recver.receive(recvPacket);
					result = recvPacket.getData();
					// debug purpose only: display the received data
					displayReceivedData(result, recvPacket.getLength());
					
//					if(result[1] == new Byte("0A").byteValue()){
//						logger.info("Watteco notification sensor received.");
//						String route  = recvPacket.getAddress().getHostAddress();
//						//TODO notify phase
//						
//					} else {
						logger.info("Watteco command response received.");
						byte[] tabB = new byte[5];
						tabB[0] = (byte) (result[1] - (new Byte("1").byteValue()));
						tabB[1] = result[2];
						tabB[2] = result[3];
						tabB[3] = result[4];
						tabB[4] = result[5];
						
						String idDatagram = "";
						for(int i=0; i<tabB.length; i++) {
							idDatagram += '$' + byteToHexString(tabB[i]);
						}
						
						BorderRouterCommand cmd = findReturnCommand(recvPacket.getAddress(), idDatagram);
						if( cmd != null ) {
							synchronized(cmd) {
								cmd.setData(result.clone());
								cmd.notify();
							}
						}
					//}
				}
				
			} catch(SocketException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch( Exception e){
				logger.error(e.getMessage());
			}
			
			if(recver != null) {
				logger.info("Closing Watteco reporting service...");
				recver.close();
			}
			logger.info("Watteco reporting service OFF.");
		}
		
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
	
	/**
	 * Convert a byte to its hexadecimal string representation
	 * @param b the byte to be converted
	 * @return the hexadecimal string representation of a byte
	 */
	public static String byteToHexString(byte b) {
		
		final char[] hexArray = {'0','1','2','3','4','5','6','7',
								 '8','9','A','B','C','D','E','F'};
		char[] hex = new char[2];
		int v = b & 0xFF;
		hex[0] = hexArray[v >>> 4];
		hex[1] = hexArray[v & 0x0F];
		return new String(hex);
	}
	
	/**
	 * Enumeration of the commands it is possible to send to a border router. 
	 * 
	 * @author thalgott
	 */
	public class BorderRouterCommand {
		
		private String value;
		private boolean response;
		private InetAddress address;
		
		private byte[] returnData;
		
		/**
		 * Inner constructor for command-type items
		 * 
		 * @param value the hexadecimal value of the command
		 * @param reponse whether this command triggers a response from the border
		 * 		router 
		 */
		public BorderRouterCommand(InetAddress address, String value, boolean response) {
			this.value = value;
			this.response = response;
			this.address = address;
			this.returnData = null;
		}
		
		public InetAddress getAddress() {
			return address;
		}

		public byte[] getData() {
			return returnData;
		}
		
		public void setData(byte[] data) {
			returnData = data;
		}

		/**
		 * Returns whether the border router sends back a response after receiving 
		 * this command. 
		 * @return whether the border router will send a value back
		 */
		public boolean sendsBack() {
			return this.response;
		}
		
		/**
		 * Returns the command value which is to be sent to the border router, as a
		 * string.
		 * 
		 * @return the value of the command to be sent
		 */
		public String getValue() {
			return this.value;
		}
	}
}
