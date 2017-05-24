package com.rai.mt.tcp.client;

import java.io.IOException;
import java.io.InputStream;
import com.rai.mt.protocol.IReceiver;

public class InStreamHandler extends Thread {

	private InputStream inStream;

	private IReceiver receiver;
	
	private IMessageReadListner msgReadListener;
	
	private FragmentHandler fragHandler;

	

	public InStreamHandler(InputStream inStream, IReceiver receiver) {
		this.inStream = inStream;
		this.receiver = receiver;
		msgReadListener = new MessageRead();
		fragHandler =new FragmentHandler(msgReadListener);
	}

	@Override
	public void run() {

		boolean isRunning = true;
		while (isRunning) {
			
			try {
				if (inStream.available() > 0) {
					fragHandler.read(inStream);
				}
				else {
				//	Thread.sleep(1);
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
