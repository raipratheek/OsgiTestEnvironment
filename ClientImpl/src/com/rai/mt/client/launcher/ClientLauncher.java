package com.rai.mt.client.launcher;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rai.mt.client.impl.ClientFactory;
import com.rai.mt.client.impl.ResponseListener;
import com.rai.mt.protocol.IAppProtocolClient;

@Component
public class ClientLauncher {

	private IAppProtocolClient protocolHandler;

	@Reference
	public void setApplicationClientProtocol(IAppProtocolClient prt) {
		protocolHandler = prt;
	}

	public void unsetApplicationClientProtocol(IAppProtocolClient prt) {
		protocolHandler = null;
	}

	@Activate
	public void activate() {
		// start UI thread.
		new Thread(new Runnable() {

			@Override
			public void run() {
				Display display = new Display();
				Shell shell = new Shell(display);
				shell.setLayout(new FillLayout());
				shell.setText("Main Client");
				ResponseListener receiver = new ResponseListener();
				ClientFactory.setDataReceiver(receiver);
				ClientFactory.setMainShell(shell);
				ClientFactory.setProtocolHandler(protocolHandler);
				ClientFactory.createNewClient();

				shell.open();
				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}

		}).start();

	}
}
