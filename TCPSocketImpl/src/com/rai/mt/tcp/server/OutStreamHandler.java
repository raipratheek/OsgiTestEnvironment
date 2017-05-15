package com.rai.mt.tcp.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

public class OutStreamHandler extends Thread {

	private Queue<String> outBuffer;

	private OutputStream outStream;

	private Object sync = new Object();

	public OutStreamHandler(OutputStream outStream) {
		outBuffer = new LinkedList<String>();
		this.outStream = outStream;
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
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				byte[] bytes = nextData.getBytes();
				try {
					outStream.write(bytes);
				} catch (IOException e) {
					isRunning = false;
					System.err.println(" Exception in Server Out stream thread " + e.getMessage());
				}
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
