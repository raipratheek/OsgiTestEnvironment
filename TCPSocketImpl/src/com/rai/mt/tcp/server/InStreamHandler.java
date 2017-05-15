package com.rai.mt.tcp.server;

import java.io.IOException;
import java.io.InputStream;
import com.rai.mt.protocol.IReceiver;

public class InStreamHandler extends Thread {

	private InputStream inStream;

	private IReceiver receiver;

	private static final int BUFFER_SIZE = 100000;

	private Object sync = new Object();

	public InStreamHandler(InputStream inStream, IReceiver receiver) {
		this.inStream = inStream;
		this.receiver = receiver;
	}

	@Override
	public void run() {

		boolean isRunning = true;
		while (isRunning) {
			byte[] readData = new byte[BUFFER_SIZE];
			try {
				if (inStream.available() > 0) {
					inStream.read(readData);
					String stringData = new String(readData, "UTF-8");
					receiver.onDataReceived(stringData);
				} else {
					Thread.sleep(20);
				}
			} catch (IOException e) {
				isRunning = false;
				System.err.println("Error in Server instream thread" + e.getMessage());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println(" The inStream Thread on server stopped ");

	}

}
