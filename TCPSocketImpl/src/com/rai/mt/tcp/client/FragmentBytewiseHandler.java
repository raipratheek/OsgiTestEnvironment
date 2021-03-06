package com.rai.mt.tcp.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FragmentBytewiseHandler {

	private static final int MAX_BUFFER_SIZE = 200000;

	private IMessageReadListner msgReadListener;

	private byte[] readData ;

	private boolean isFragmentRead = true;

	private int fragLength = 0;

	private int readlength = 0;
	
	public FragmentBytewiseHandler(){
		
	}

	public FragmentBytewiseHandler(IMessageReadListner msgReadListener) {
		this.msgReadListener = msgReadListener;

	}

	public void read(InputStream inStream) {

		try {

			if (isFragmentRead) {
				readlength = 0;
				char first = (char) inStream.read();
				char second = (char) inStream.read();
				char length0 = (char) inStream.read();
				char length1 = (char) inStream.read();
				char length2 = (char) inStream.read();
				char length3 = (char) inStream.read();

				if (first == 0x4F && second == 0xfd) {
					fragLength = length0 | ((length1 & 0xFF) << 8 )| ((length2 & 0xFF) << 16) | ((length3) & 0xFF << 24);
				} else {
					System.err.println(" Invalid data in stream ");
					return;
				}
				if(MAX_BUFFER_SIZE < fragLength)
				{
					System.err.println(" buffer size if smaller than the data in the segment !!!!");
					return;
				}
				readData = new byte[fragLength];
				isFragmentRead = false;

			} else {
				while (inStream.available() != 0) {
					byte readByte = (byte) inStream.read();
					readData[readlength++] = readByte;					
				}
				
				if (readlength == fragLength) {
					String stringData = new String(readData, "UTF-8");
					msgReadListener.onMessageRead(stringData);
					isFragmentRead = true;
				} else {
					isFragmentRead = false;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public void write(OutputStream outStream , String data) {
		
		byte[] dataBytes = data.getBytes();
		int dataLength = dataBytes.length;
		byte[]header = new byte[6];
		
		try {
			header[0] = 0x4F;
			header[1] = (byte) 0xfd;
			header[2] = (byte) (dataLength & 0xFF);
			header[3] = (byte) ((dataLength >> 8) & 0xFF);
			header[4] = (byte) ((dataLength >> 16) & 0xFF);
			header[5] = (byte) ((dataLength >> 24) & 0xFF);
			outStream.write(header);
			outStream.write(dataBytes);
			outStream.flush();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
