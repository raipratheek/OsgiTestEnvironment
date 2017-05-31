package com.rai.mt.tcp.client;

import java.io.IOException;
import java.io.InputStream;
import com.rai.mt.protocol.IReceiver;

public class InStreamHandler extends Thread {

	private InputStream inStream;

	private IReceiver receiver;
	
	private IMessageReadListner msgReadListener;
	
	private FragmentStreamHandler fragStreamHandler;

	

	public InStreamHandler(InputStream inStream, IReceiver receiver) {
		this.inStream = inStream;
		this.receiver = receiver;
		msgReadListener = new MessageRead();
		fragStreamHandler =new FragmentStreamHandler(msgReadListener);
	}

	@Override
	public void run() {

		boolean isRunning = true;
		while (isRunning) {
			
			try {
				if (inStream.available() > 0) {
					fragStreamHandler.read(inStream);
				}
				else {
//					try {
//						Thread.sleep(1);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
				}
			} catch (IOException e) {
				isRunning = false;
				System.err.println("Error in Client instream thread" + e.getMessage());
			}

		}
		System.out.println(" The inStream Thread on Client stopped ");

	}

	class MessageRead implements IMessageReadListner {

		public void onMessageRead(String msg) {
			receiver.onDataReceived(msg);
		}
	}

}
