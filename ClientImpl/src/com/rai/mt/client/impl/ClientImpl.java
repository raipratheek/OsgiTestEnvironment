package com.rai.mt.client.impl;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;

import com.rai.mt.client.api.IDataListener;
import com.rai.mt.client.data.ResponseAnalysisData;
import com.rai.mt.client.util.JSONHandler;
import com.rai.mt.data.JSONTags;
import com.rai.mt.protocol.IAppProtocolClient;

public class ClientImpl {

	/**
	 * Shell
	 */
	private Shell shell;

	// private Text responseBox;

	// response analysis widgets
	// --------------------------------------------------------------------------------------------------------------------

	/**
	 * Widgets for analysis.
	 */

	private Label responseCountLabel;

	private Label responseCount;

	private Label sequenceLabel;

	private Label sequenceValue;

	private Label validResponseLabel;

	private Label validResponseValue;

	private Label avgTimeLabel;

	private Label avgTimeValue;

	// ---------------------------------------------------------------------------------------------------------------------
	private Text requestBox;

	private Text console;

	private Combo urlBox;

	private Button sendButton;

	private Button stopButton;

	private Button connect;

	private Button newInstance;

	private Combo timeCombo;

	private Label timeUnit;

	private Label requestLabel;

	private Label requestSize;

	private Label responseLabel;

	private Label flightTimeLabel;

	private Label flightTimeValue;

	private IAppProtocolClient protocolHandler;

	private IDataListener dataListener;

	private JSONHandler jsonHandler;

	private ResponseAnalyser resAnalyser;

	private ResponseListener receiver;

	private String clientID = "";

	private volatile boolean isStopped;

	public ClientImpl(Shell shell, String clientid, IAppProtocolClient protocol, ResponseAnalyser resAnalyser,
			ResponseListener listener) {
		this.shell = shell;
		this.protocolHandler = protocol;
		this.resAnalyser = resAnalyser;
		receiver = listener;
		clientID = clientid;
		dataListener = new ClientDataListener();
		jsonHandler = new JSONHandler();
	}

	/**
	 * Creates the view for the client.
	 */
	public void createView() {

		Composite mainComposite = new Composite(shell, SWT.NONE);
		mainComposite.setLayout(new FormLayout());

		urlBox = new Combo(mainComposite, SWT.BORDER | SWT.SCROLL_LINE);
		urlBox.setLayoutData(getFormdata(2, 2, 48, 7));
		urlBox.add("wss://localhost:8025");
		urlBox.add("ws://localhost:8025");
		urlBox.add("ssl://localhost:8025");
		urlBox.add("tcp://localhost:8025");
		urlBox.add("tcp://192.168.2.128:8025");
		urlBox.add("tcp://169.254.142.56:8025");
		urlBox.add("coaps://localhost/coap");
		urlBox.add("coap://localhost/coap");
		urlBox.add("coap://192.168.2.128/coap");
		urlBox.add("coap://169.254.142.56/coap");
		urlBox.select(0);

		requestLabel = new Label(mainComposite, SWT.NONE);
		requestLabel.setLayoutData(getFormdata(2, 17, 20, 20));
		requestLabel.setText("Test Data");

		requestSize = new Label(mainComposite, SWT.NONE);
		requestSize.setLayoutData(getFormdata(21, 17, 48, 20));

		requestBox = new Text(mainComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		requestBox.setLayoutData(getFormdata(2, 20, 48, 70));
		requestBox.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent arg0) {
				String text = requestBox.getText();
				long size = text.getBytes().length;
				requestSize.setText(" " + size + " bytes ");
			}
		});
		requestBox.setText(" Test String !@@#$$%%^  丣丕且丗丕   ÄÉ ");
		resAnalyser.setTestPayload(" Test String !@@#$$%%^  丣丕且丗丕   ÄÉ ");
		responseLabel = new Label(mainComposite, SWT.NONE);
		responseLabel.setLayoutData(getFormdata(52, 17, 98, 20));
		responseLabel.setText("Response Analysis");

		// ---------------------------------------------------------------

		int top = 25;

		responseCountLabel = new Label(mainComposite, SWT.NONE);
		responseCountLabel.setLayoutData(getFormdata(52, top, 80, top + 5));
		responseCountLabel.setText("Response Count");

		responseCount = new Label(mainComposite, SWT.NONE);
		responseCount.setLayoutData(getFormdata(85, top, 98, top + 5));
		responseCount.setText("0");

		top = top + 6;

		sequenceLabel = new Label(mainComposite, SWT.NONE);
		sequenceLabel.setLayoutData(getFormdata(52, top, 80, top + 5));
		sequenceLabel.setText("Response Sequence ");

		sequenceValue = new Label(mainComposite, SWT.NONE);
		sequenceValue.setLayoutData(getFormdata(85, top, 98, top + 5));
		sequenceValue.setText("---");

		top = top + 6;

		validResponseLabel = new Label(mainComposite, SWT.NONE);
		validResponseLabel.setLayoutData(getFormdata(52, top, 80, top + 5));
		validResponseLabel.setText("Response Valid ");

		validResponseValue = new Label(mainComposite, SWT.NONE);
		validResponseValue.setLayoutData(getFormdata(85, top, 98, top + 5));
		validResponseValue.setText("---");

		top = top + 6;

		avgTimeLabel = new Label(mainComposite, SWT.NONE);
		avgTimeLabel.setLayoutData(getFormdata(52, top, 80, top + 5));
		avgTimeLabel.setText("Average Response Interval ");

		avgTimeValue = new Label(mainComposite, SWT.NONE);
		avgTimeValue.setLayoutData(getFormdata(85, top, 98, top + 5));
		avgTimeValue.setText("---");

		top = top + 6;

		flightTimeLabel = new Label(mainComposite, SWT.NONE);
		flightTimeLabel.setLayoutData(getFormdata(52, top, 80, top + 5));
		flightTimeLabel.setText("Flight Time ");

		flightTimeValue = new Label(mainComposite, SWT.NONE);
		flightTimeValue.setLayoutData(getFormdata(85, top, 98, top + 5));
		flightTimeValue.setText("---");

		// -------------------------------------------------------------------------

		// responseBox = new Text(mainComposite, SWT.BORDER | SWT.V_SCROLL |
		// SWT.H_SCROLL);
		// responseBox.setLayoutData(getFormdata(52, 20, 98, 70));

		sendButton = new Button(mainComposite, SWT.NONE);
		sendButton.setText("SEND");
		sendButton.setLayoutData(getFormdata(20, 72, 30, 77));

		stopButton = new Button(mainComposite, SWT.NONE);
		stopButton.setText("STOP");
		stopButton.setLayoutData(getFormdata(32, 72, 42, 77));
		stopButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				JSONObject stopReq = new JSONObject();
				stopReq.put(JSONTags.CLIENT_ID, clientID);
				stopReq.put(JSONTags.RESPONSE_STOP, "stop");
				protocolHandler.send(stopReq.toString());
				isStopped = true;
				resAnalyser.reset();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		connect = new Button(mainComposite, SWT.NONE);
		connect.setText("CONNECT");
		connect.setLayoutData(getFormdata(52, 2, 75, 7));

		connect.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					receiver.addDataListener(clientID, dataListener);
					protocolHandler.connect(new URI(urlBox.getText()), receiver);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		newInstance = new Button(mainComposite, SWT.NONE);
		newInstance.setText("New Instance");
		newInstance.setLayoutData(getFormdata(88, 2, 98, 7));

		newInstance.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ClientFactory.createNewClient();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		timeCombo = new Combo(mainComposite, SWT.BORDER);
		timeCombo.setItems("-1", "5", "10", "50", "100", "200", "500", "1000", "2000");
		timeCombo.setLayoutData(getFormdata(5, 72, 15, 77));
		timeCombo.setText("Time");

		sendButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (requestBox.getText() == null || requestBox.getText().equals("")) {
					console.setText(" Add the request that needs to be sent to the request box");
				} else {
					isStopped = false;
					receiver.addDataListener(clientID, dataListener);
					int timeToRes = -1;
					int selectionIndex = timeCombo.getSelectionIndex();
					if (selectionIndex >= 0) {
						String selectedTime = timeCombo.getItem(timeCombo.getSelectionIndex());

						if (!("Time".equals(selectedTime))) {
							timeToRes = Integer.valueOf(selectedTime);
						}
					}
					JSONObject jobj = jsonHandler.wrapRequest(requestBox.getText(), clientID, timeToRes);
					resAnalyser = new ResponseAnalyser();
					resAnalyser.setTestPayload(requestBox.getText());
					protocolHandler.send(jobj.toString());
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		timeUnit = new Label(mainComposite, SWT.NONE);
		timeUnit.setLayoutData(getFormdata(15, 72, 20, 77));
		timeUnit.setText("  ms");

		console = new Text(mainComposite, SWT.V_SCROLL | SWT.BORDER);
		console.setLayoutData(getFormdata(2, 78, 98, 99));
	}

	/**
	 * Returns data for layout.
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	private FormData getFormdata(int left, int top, int right, int bottom) {
		FormData fd = new FormData();
		fd.left = new FormAttachment(left, 0);
		fd.top = new FormAttachment(top, 0);
		fd.right = new FormAttachment(right, 0);
		fd.bottom = new FormAttachment(bottom, 0);
		return fd;
	}

	/**
	 * Data listener from server.
	 * 
	 * @author Prathik
	 *
	 */
	class ClientDataListener implements IDataListener {

		@Override
		public void onDataReceived(final String data) {
			if ("".equals(data)) {
				return;
			}
			JSONObject jobj = new JSONObject(data);
			ResponseAnalysisData analysisData = resAnalyser.analyze(jobj);
			updateAnalysis(analysisData);
		}

		/**
		 * Updates GUI with tha analysis data
		 * 
		 * @param analysisData
		 */
		private void updateAnalysis(final ResponseAnalysisData analysisData) {

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					long flightTime = analysisData.getFlightTime();

					if (!isStopped) {
						if (analysisData.isResponseValid()) {
							validResponseValue.setText("Valid");
							validResponseValue.setBackground(new Color(Display.getDefault(), 0, 255, 0));
						} else {
							validResponseValue.setText("Invalid");
							validResponseValue.setBackground(new Color(Display.getDefault(), 255, 0, 0));
						}
						if (analysisData.isSequenceValid()) {
							sequenceValue.setText("OK");
							sequenceValue.setBackground(new Color(Display.getDefault(), 0, 255, 0));
						} else {
							sequenceValue.setText("NOK");
							sequenceValue.setBackground(new Color(Display.getDefault(), 255, 0, 0));
						}
						
					}else {
						validResponseValue.setText("---");
						validResponseValue.setBackground(new Color(Display.getDefault(), 255, 255, 255));
						
						sequenceValue.setText("---");
						sequenceValue.setBackground(new Color(Display.getDefault(), 255, 255, 255));
					}

					responseCount.setText("" + analysisData.getResponseNumber());
					float avgTime = analysisData.getAverageTimeInterval();
					if (avgTime == -1) {
						avgTimeValue.setText("---");
					} else {
						avgTimeValue.setText("" + avgTime + " ms");
					}

					flightTimeValue.setText("" + flightTime + " ms");

					

					console.append(" receiving data from server. Count = " + analysisData.getResponseNumber() + "\n");
				}
			});
		}

		@Override
		public void onError(final String errorDetails) {

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					console.append("ERROR " + errorDetails + "\n");

				}
			});

		}

		@Override
		public void onConnectionOpen(final String msg) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					console.append("INFO " + msg + "\n");

				}
			});

		}

	}

}
