package org.concord.energy3d;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Charles Xie
 *
 */

public class MyServerSocket {

	private ServerSocket server;
	private Socket client;

	public MyServerSocket(final String ipAddress) throws Exception {
		if (ipAddress != null && !ipAddress.isEmpty()) {
			server = new ServerSocket(0, 1, InetAddress.getByName(ipAddress));
		} else {
			server = new ServerSocket(11999, 1, InetAddress.getByName("127.0.0.1"));
		}
	}

	public void listen() throws Exception {
		client = server.accept();
		final String clientAddress = client.getInetAddress().getHostAddress();
		System.out.println("\r\nNew connection from " + clientAddress);
		final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		final PrintWriter out = new PrintWriter(client.getOutputStream());
		String data = null;
		while ((data = in.readLine()) != null) {
			System.out.println("\r\nMessage from " + clientAddress + ": " + data);
			out.println("B");
			out.flush();
		}
	}

	public InetAddress getSocketAddress() {
		return server.getInetAddress();
	}

	public int getPort() {
		return server.getLocalPort();
	}

}