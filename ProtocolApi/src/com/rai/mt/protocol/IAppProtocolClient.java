package com.rai.mt.protocol;

import java.net.URI;

public interface IAppProtocolClient {

	void send(String request);

	void connect(URI url , IReceiver receiver) throws Exception;

}
