//package com.rai.mt.server.impl;
//
//import java.net.InetSocketAddress;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.FormAttachment;
//import org.eclipse.swt.layout.FormData;
//import org.eclipse.swt.layout.FormLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//import org.json.JSONObject;
//
//import com.rai.mt.data.JSONTags;
//import com.rai.mt.protocol.IApprotocolServer;
//import com.rai.mt.protocol.IReceiver;
//
///**
// * Server Instance with UI.
// * 
// * @author Prathik
// *
// */
//public class ServerTestClient {
//
//	private int port;
//
//	private String host;
//
//	private Shell shell;
//
//	private Text infoBox;
//
//	private Text requestBox;
//
//	private Button startButton;
//
//	private Button secureButton;
//
//	private Label requestLabel;
//
//	private Label infoLabel;
//
//	private IApprotocolServer protocol;
//
//	private IReceiver receiver;
//
//	private HashMap<String, RequestHandler> clientreqHandlers;
//
//	public ServerTestClient(Shell shell, IApprotocolServer protocol, int port, String host) {
//		this.shell = shell;
//		this.protocol = protocol;
//		this.host = host;
//		this.port = port;
//		clientreqHandlers = new HashMap<String, RequestHandler>();
//
//	}
//
//	public void createView() {
//
//		Composite mainComposite = new Composite(shell, SWT.NONE);
//		mainComposite.setLayout(new FormLayout());
//
//		requestLabel = new Label(mainComposite, SWT.NONE);
//		requestLabel.setLayoutData(getFormdata(2, 12, 20, 17));
//		requestLabel.setText("Requests");
//
//		requestBox = new Text(mainComposite, SWT.BORDER | SWT.V_SCROLL);
//		requestBox.setLayoutData(getFormdata(2, 17, 98, 60));
//
//		infoLabel = new Label(mainComposite, SWT.NONE);
//		infoLabel.setLayoutData(getFormdata(2, 62, 20, 67));
//		infoLabel.setText("Console");
//
//		infoBox = new Text(mainComposite, SWT.BORDER | SWT.V_SCROLL);
//		infoBox.setLayoutData(getFormdata(2, 67, 98, 95));
//
//		startButton = new Button(mainComposite, SWT.NONE);
//		startButton.setText("Start");
//		startButton.setLayoutData(getFormdata(2, 2, 10, 10));
//
//		secureButton = new Button(mainComposite, SWT.CHECK);
//		secureButton.setText("Secure");
//		secureButton.setLayoutData(getFormdata(12, 2, 27, 10));
//
//		startButton.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent arg0) {
//				try {
//
//					protocol.init(host, port, secureButton.getSelection());
//					if (receiver == null) {
//						receiver = new ServerReceiver();
//						protocol.registerReceiver(receiver);
//					}
//					protocol.startServer();
//
//					infoBox.append("INFO " + "Server started with IP Address =  "
//							+ new InetSocketAddress(host, port).toString() + " \n");
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				// TODO Auto-generated method stub
//
//			}
//		});
//
//	}
//
//	/**
//	 * Returns data for layout.
//	 * 
//	 * @param left
//	 * @param top
//	 * @param right
//	 * @param bottom
//	 * @return
//	 */
//	private FormData getFormdata(int left, int top, int right, int bottom) {
//		FormData fd = new FormData();
//		fd.left = new FormAttachment(left, 0);
//		fd.top = new FormAttachment(top, 0);
//		fd.right = new FormAttachment(right, 0);
//		fd.bottom = new FormAttachment(bottom, 0);
//		return fd;
//	}
//
//	class ServerReceiver implements IReceiver {
//
//		@Override
//		public void onDataReceived(final String req) {
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					requestBox.append(req + "\n");
//					JSONObject data = new JSONObject(req);
//					String clientID = data.getString(JSONTags.CLIENT_ID);
//					RequestHandler reqHandler = clientreqHandlers.get(clientID);
//
//					if (reqHandler == null) {
//						reqHandler = new RequestHandler(protocol);
//						clientreqHandlers.put(clientID, reqHandler);
//					}
//
//					if (data.has(JSONTags.RESPONSE_STOP)) {
//						reqHandler.setStopUpdater(true);
//						return;
//					}
//
//					int timeToRespond = data.getInt(JSONTags.TIME_TO_RESPOND);
//					if (timeToRespond > -1) {
//						reqHandler.onContinuousRequest(data);
//					} else {
//						reqHandler.onSingleRequest(data);
//					}
//				}
//			});
//
//		}
//
//		@Override
//		public void onError(final String errorDetails) {
//			System.err.println(errorDetails);
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					infoBox.append("ERROR " + errorDetails + "\n");
//
//				}
//			});
//
//		}
//
//		@Override
//		public void onConnectionOpen(final String msg) {
//			System.err.println(msg);
//			Display.getDefault().asyncExec(new Runnable() {
//
//				@Override
//				public void run() {
//					infoBox.append("INFO " + "Application Protocol Type = " + protocol.getType() + "\n");
//					infoBox.append("INFO " + msg + "\n");
//
//				}
//			});
//
//		}
//
//	}
//
//}
