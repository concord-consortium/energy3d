package org.concord.energy3d.etc.oneinstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

final class OneInstanceClient implements Runnable {
	/** The application id. */
	private final String appId;

	/** The client socket. */
	private final Socket socket;

	/**
	 * Constructor.
	 *
	 * @param socket
	 *            The client socket.
	 * @param appId
	 *            The application id.
	 */
	OneInstanceClient(final Socket socket, final String appId) {
		this.socket = socket;
		this.appId = appId;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			try {
				// Send the application ID.
				final PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				out.println(appId);
				out.flush();

				// Read the data from the client
				final InputStream in = socket.getInputStream();
				final ObjectInputStream objIn = new ObjectInputStream(in);
				final File workingDir = (File) objIn.readObject();
				final String[] args = (String[]) objIn.readObject();

				// Call event handler
				final boolean result = OneInstance.getInstance().fireNewInstance(workingDir, args);

				// Send the result
				out.println(result ? "start" : "exit");
				out.flush();

				// Wait for client disconnect.
				in.read();
			} finally {
				socket.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
