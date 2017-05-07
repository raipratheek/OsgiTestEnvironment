package com.rai.mt.serverimpl;

import java.nio.BufferUnderflowException;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.rai.mt.protocol.IApprotocolServer;

@Component
public class ServerLauncher {

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
				Display display = new Display();
				Shell shell = new Shell(display);
				shell.setLayout(new FillLayout());
				shell.setText("SERVER");
				ServerTestClient stc = new ServerTestClient(shell, protocol);
				stc.createView();
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
