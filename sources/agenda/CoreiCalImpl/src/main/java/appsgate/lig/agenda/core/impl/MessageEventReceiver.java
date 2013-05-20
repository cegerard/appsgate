package appsgate.lig.agenda.core.impl;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appsgate.lig.agenda.core.messages.StartingEventNotificationMsg;

public class MessageEventReceiver implements Runnable {

	Queue<StartingEventNotificationMsg> queue;
	boolean started=false;

	/**
	 * Static class member uses to log what happened in each instances
	 */
	private static Logger logger = LoggerFactory.getLogger(MessageEventReceiver.class);
	
	public void start(){
		
		started=true;

		logger.debug("MessageEventReceiver: initial size of the queue:"+queue.size());
		
		Thread t=new Thread(this);
		t.start();
				
	}
	
	public void stop(){
		started=false;
	}

	@Override
	public void run() {
		
		while(started){
			
			try {
				
				logger.debug("trying to fetch queue size...");
				
				Thread.sleep(3000);
				
				if(queue==null){
					logger.debug("MessageEventReceiver: queue not injected");
				}else if(queue.size()==0){
					logger.debug("MessageEventReceiver: nothing in the queue");
				}else {
					System.out.println("**************************");
					System.out.println("MessageEventReceiver: element found in the queue");
					StartingEventNotificationMsg message=queue.poll();
					System.out.println("MessageEventReceiver: "+message.getNewValue());
					System.out.println("**************************");
				}
				
				
				
			}catch(NullPointerException e){
				
				logger.debug("Message empty");
				
			} catch (InterruptedException e) {
			
				e.printStackTrace();
				
			}
			
			
		}
		
	}
	
}
