package com.rai.mt.tcp.client;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class OutStreamHandler extends Thread {

	private Queue<String> outBuffer;

	private OutputStream outStream;

	private Object sync = new Object();
	
	private FragmentStreamHandler fragStreamHandler;

	public OutStreamHandler(OutputStream outStream) {
		outBuffer = new LinkedList<String>();
		this.outStream = outStream;
		fragStreamHandler = new FragmentStreamHandler();
	}

	@Override
	public void run() {
		boolean isRunning = true;
		while (isRunning) {
			String nextData = null;
			synchronized (sync) {
				nextData = outBuffer.poll();
			}
			if (nextData == null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} else {
				fragStreamHandler.write(outStream, nextData);
			}

		}

		System.out.println(" Server Out stream thread stopped .");
	}

	public void addData(String data) {
		synchronized (sync) {
			outBuffer.add(data);
		}

	}
}
