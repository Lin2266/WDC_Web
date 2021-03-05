package course.model;

import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket2")
public class WebSocketServer2 {
	private final int port;
	
	public WebSocketServer2(int port) {
		this.port = port;
	}
	
}
