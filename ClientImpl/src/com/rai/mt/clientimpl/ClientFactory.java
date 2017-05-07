package com.rai.mt.clientimpl;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.rai.mt.protocol.IAppProtocolClient;

public class ClientFactory {

	/**
	 * Main client shell
	 */
	private static Shell mainShell;

	/**
	 * Client count.
	 */
	private static int clientCount = 0;

	/**
	 * Handle for the application protocol.
	 */
	private static IAppProtocolClient protocolHandler;

	/**
	 * Data receiver. Notified when data is received from the server.
	 */
	private static ResponseListener dataReceiver;

	/**
	 * Set data receiver.
	 * @param dataReceiver - ResponseListener
	 */
	public static void setDataReceiver(ResponseListener dataReceiver) {
		ClientFactory.dataReceiver = dataReceiver;
	}

	/**
	 * Set protocol handler.
	 * @param protocolHandler - IAppProtocolClient
	 */
	public static void setProtocolHandler(IAppProtocolClient protocolHandler) {
		ClientFactory.protocolHandler = protocolHandler;
	}

	/**
	 * Set main shell.
	 * @param mainShell
	 */
	public static void setMainShell(Shell mainShell) {
		ClientFactory.mainShell = mainShell;
	}

	/**
	 * Factory method for client instance.
	 */
	public static void createNewClient() {

		ResponseAnalyser resAnalyser = new ResponseAnalyser();
		Shell shell;
		if (clientCount == 0) {
			shell = mainShell;
		} else {
			shell = new Shell(Display.getDefault());
			shell.setText("Instance " + clientCount);
		}
		shell.setLayout(new FillLayout());
		ClientImpl client = new ClientImpl(shell, "CLIENT" + clientCount, protocolHandler, resAnalyser, dataReceiver);
		clientCount++;
		client.createView();
		shell.open();
	}
}
