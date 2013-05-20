package appsgate.lig.agenda.core.impl;

import java.util.Queue;

import appsgate.lig.agenda.core.messages.StartingEventNotificationMsg;
import appsgate.lig.logical.object.messages.NotificationMsg;

public class MessageEventReceiver implements Runnable {

	Queue<StartingEventNotificationMsg> queue;
	boolean started=false;

	public void start(){
		System.out.println("MessageEventReceiver: Starting event receiver");
		started=true;

		System.out.println("MessageEventReceiver: initial size of the queue:"+queue.size());
		
		Thread t=new Thread(this);
		t.start();
				
	}
	
	public void stop(){
		started=false;
		System.out.println("Starting event receiver");
	}

	@Override
	public void run() {
		
		while(started){
			
			try {
				
				System.out.println("trying to fetch queue size...");
				
				Thread.sleep(3000);
				
				if(queue==null){
					System.err.println("MessageEventReceiver: queue not injected");
				}else if(queue.size()==0){
					System.err.println("MessageEventReceiver: nothing in the queue");
				}else {
					System.out.println("**************************");
					System.out.println("MessageEventReceiver: element found in the queue");
					NotificationMsg message=queue.poll();
					System.out.println("MessageEventReceiver: "+message.getNewValue());
					System.out.println("**************************");
				}
				
				
				
			}catch(NullPointerException e){
				
				System.err.println("Message empty");
				
			} catch (InterruptedException e) {
			
				e.printStackTrace();
				
			}
			
			
		}
		
	}
	
}
