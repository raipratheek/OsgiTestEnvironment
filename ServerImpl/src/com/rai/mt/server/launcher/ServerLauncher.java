package com.rai.mt.server.launcher;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rai.mt.protocol.IApprotocolServer;
import com.rai.mt.server.impl.ServerWithoutUI;

@Component
public class ServerLauncher {

	/**
	 * Server port.
	 */
	private int port = 8025;

	/**
	 * Server host address.
	 */
	private String host = "192.168.0.38";

	/**
	 * Secure server flag.
	 */
	private boolean isSecure = false;

	/**
	 * Server with UI flag.
	 */
	private boolean withUI = false;
	
	/**
	 * App prototcol handle.
	 */
	private IApprotocolServer protocol;


	@Reference
	public void setApplicationProtocol(IApprotocolServer prt) {
		protocol = prt;
	}

	public void unsetApplicationProtocol(IApprotocolServer prt) {
		protocol = null;
	}

	@Activate
	public void activate() {
		new Thread(new Runnable() {

			@Override
			public void run() {

				if (withUI) {
//					Display display = new Display();
//					Shell shell = new Shell(display);
//					shell.setLayout(new FillLayout());
//					shell.setText("SERVER");
//					ServerTestClient stc = new ServerTestClient(shell, protocol , port , host);
//					stc.createView();
//					shell.open();
//
//					while (!shell.isDisposed()) {
//						if (!display.readAndDispatch()) {
//							display.sleep();
//						}
//					}

				} else {
					ServerWithoutUI plainVanillaServer = new ServerWithoutUI(protocol, port, host, isSecure);
					plainVanillaServer.startServer();

				}
			}
		}).start();
	}
}
