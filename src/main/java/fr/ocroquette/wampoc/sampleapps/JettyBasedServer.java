package fr.ocroquette.wampoc.sampleapps;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import fr.ocroquette.wampoc.adapters.jetty.WampocJettyWebSocketHandler;
import fr.ocroquette.wampoc.common.Channel;
import fr.ocroquette.wampoc.messages.MessageMapper;
import fr.ocroquette.wampoc.messages.PublishMessage;
import fr.ocroquette.wampoc.server.ClientId;


public class JettyBasedServer 
{

	public static void main( String[] args )
	{
		int tcpPort = 8081;

		Server jettyServer = new Server(tcpPort);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		jettyServer.setHandler(context);

		fr.ocroquette.wampoc.server.Server wampocServer = new fr.ocroquette.wampoc.server.Server();
		WampocJettyWebSocketHandler webSocketHandler = new WampocJettyWebSocketHandler(wampocServer);
		webSocketHandler.setHandler(new DefaultHandler());
		jettyServer.setHandler(webSocketHandler);

		System.err.println("Starting the WS server on TCP port: " + tcpPort);
		try {
			jettyServer.start();
			startPostman(wampocServer);
			jettyServer.join();
		} catch (Exception e) {
			System.err.println("Failed to start the WS server:\n" +e);
		}
	}

	public static class Postman implements Runnable {
		Postman(fr.ocroquette.wampoc.server.Server server) {
			this.server = server;
		}
		@Override
		public void run() {
			ClientId clientId;
			try {
				clientId = server.addClient(new Channel() {
					@Override
					public void handle(String message) throws IOException {
					}});
				PublishMessage msg = new PublishMessage("http://example.com/simple");
				msg.setPayload("Hello world!");
				String json = MessageMapper.toJson(msg);
				while( true ) {
					try {
						Thread.sleep(5000);
						System.out.println("Postman says:"+json);
						server.handleIncomingMessage(clientId, json);
					} catch (InterruptedException e) {
						return;
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		protected fr.ocroquette.wampoc.server.Server server;
	}

	public static void startPostman(fr.ocroquette.wampoc.server.Server server) {
		new Thread(new Postman(server)).start();
	}
}