package org.concord.energy3d.etc.oneinstance;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * The server socket which listens for new instances.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
final class OneInstanceServer implements Runnable {
	/** The application name. */
	private final String appId;

	/** The port number. */
	private final int port;

	/** The server socket. */
	private ServerSocket socket;

	/** Flag indicating if thread is about to stop. */
	private boolean stop;

	/** The listen thread. */
	private Thread thread;

	/**
	 * Constructor.
	 *
	 * @param appId
	 *            The application name.
	 * @param port
	 *            The application port.
	 * @throws PortAlreadyInUseException
	 *             When port is already in use.
	 */
	OneInstanceServer(final String appId, final int port) throws PortAlreadyInUseException {
		this.appId = appId;
		this.port = port;

		try {
			socket = new ServerSocket(this.port, 50, InetAddress.getByName(null));
			socket.setSoTimeout(250);
		} catch (final IOException e) {
			throw new PortAlreadyInUseException();
		}
	}

	/**
	 * Accept new client connection.
	 *
	 * @return The accepted client connection or null if no client was found.
	 */
	private Socket accept() {
		try {
			return socket.accept();
		} catch (final SocketTimeoutException e) {
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Starts the server.
	 */
	public final void start() {
		if (thread != null) {
			throw new IllegalStateException("Thread already started");
		}
		thread = new Thread(this, "One Instance Server");
		stop = false;
		thread.start();
	}

	/**
	 * Stops the server.
	 */
	public final void stop() {
		if (thread == null) {
			throw new IllegalStateException("Thread already stopped");
		}
		if (stop) {
			throw new IllegalStateException("Thread already stopping");
		}
		stop = true;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		try {
			while (!stop) {
				// Accept the next client. If timeout occurred then do nothing in
				// this thread iteration.
				final Socket socket = accept();
				if (socket == null) {
					continue;
				}

				// Start new client thread
				new Thread(new OneInstanceClient(socket, appId), "One Instance Client").start();
			}
		} finally {
			thread = null;
		}
	}
}
