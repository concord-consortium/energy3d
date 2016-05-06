package org.concord.energy3d.etc.oneinstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * The One-Instance manager. This is a singleton so you must use the {@link OneInstance#getInstance()} method to obtain it. The application must call the {@link OneInstance#register(Class, String[])} method at the beginning of its main method. If this method returns false then the application must exit immediately.
 *
 * The application must also provide an implementation of the {@link OneInstanceListener} interface which must be registered with the {@link OneInstance#addListener(OneInstanceListener)} method. THis listener is called when additional instances of the application are started. The listener can then decide what to do with the new instance and with its command-line arguments.
 *
 * When the application exits then it should call {@link OneInstance#unregister(Class)} but this is not a requirement. This method just closes the server socket (Which will be closed anyway when the application exits) and it removes the port number from the preferences so the next started application does not need to check if this port is still valid. When the application instance was not the first instance then this method does nothing at all.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public final class OneInstance {
	/** The singleton instance. */
	private static final OneInstance instance = new OneInstance();

	/** The registered listeners. */
	private final List<OneInstanceListener> listeners = Collections.synchronizedList(new ArrayList<OneInstanceListener>());

	/** The key name used in the preferences to remember the server port. */
	public static final String PORT_KEY = "oneInstanceServerPort";

	/** The minimum port address. */
	public static final int MIN_PORT = 49152;

	/** The maximum port address. */
	public static final int MAX_PORT = 65535;

	/** The random number generator used to find a free port number. */
	private final Random random = new Random();

	/** The server socket. */
	private OneInstanceServer server;

	/**
	 * Private constructor to prevent instantiation of singleton from outside.
	 */
	private OneInstance() {
		// Empty
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return The singleton instance.
	 */
	public static OneInstance getInstance() {
		return instance;
	}

	/**
	 * Adds a new listener. This listener is informed about a new application instance which is about to be started. The listener gets the command-line arguments and can decide what to do with the new instance by returning true or false.
	 *
	 * @param listener
	 *            The listener to add. Must not be null.
	 */
	public void addListener(final OneInstanceListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener must be set");
		listeners.add(listener);
	}

	/**
	 * Removes a listener.
	 *
	 * @param listener
	 *            The listener to remove. Must not be null.
	 */
	public void removeListener(final OneInstanceListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener must be set");
		listeners.remove(listener);
	}

	/**
	 * Creates and returns the lock file for the specified class name.
	 *
	 * @param className
	 *            The name of the main class.
	 * @return The lock file.
	 */
	private File getLockFile(final String className) {
		return new File(System.getProperty("java.io.tmpdir"), "oneinstance-" + className + ".lock");
	}

	/**
	 * Locks the specified lock file.
	 *
	 * @param lockFile
	 *            The lock file.
	 * @return The lock or null if no locking could be performed.
	 */
	@SuppressWarnings("resource")
	private FileLock lock(final File lockFile) {
		try {
			final FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();
			return channel.lock();
		} catch (final IOException e) {
			System.out.println("Unable to lock the lock file: " + e + ". Trying to run without a lock.");
			return null;
		}
	}

	/**
	 * Releases the specified lock.
	 *
	 * @param fileLock
	 *            The file lock to release. If null then nothing is done.
	 */
	private void release(final FileLock fileLock) {
		if (fileLock == null)
			return;
		try {
			fileLock.release();
		} catch (final IOException e) {
			System.out.println("Unable to release lock file: " + e);
		}
	}

	/**
	 * Registers this instance of the application. Returns true if the application is allowed to run or false when the application must exit immediately.
	 *
	 * @param mainClass
	 *            The main class of the application. Must not be null. This is used for determining the application ID and as the user node key for the preferences.
	 * @param args
	 *            The command line arguments. They are passed to an already running instance if found. Must not be null.
	 * @return True if instance is allowed to start, false if not.
	 */
	public boolean register(final Class<?> mainClass, final String[] args) {
		if (mainClass == null)
			throw new IllegalArgumentException("mainClass must be set");
		if (args == null)
			throw new IllegalArgumentException("args must be set");

		// Determine application ID from class name.
		final String appId = mainClass.getName();

		try {
			// Acquire a lock
			final File lockFile = getLockFile(appId);
			final FileLock lock = lock(lockFile);

			try {
				// Get the port which is currently recorded as active.
				final Integer port = getActivePort(mainClass);

				// If port is found then we have to validate it
				if (port != null) {
					// Try to connect to the first instance.
					final Socket socket = openClientSocket(appId, port);

					// If connection is successful then run as a client
					// (non-first instance)
					if (socket != null) {
						try {
							// Run the client and return the result from the
							// server
							return runClient(socket, args);
						} finally {
							socket.close();
						}
					}
				}

				// Run the server
				runServer(mainClass);

				// Mark the lock file to be deleted when this instance exits.
				lockFile.deleteOnExit();

				// Allow this first instance to run.
				return true;
			} finally {
				release(lock);
			}
		} catch (final IOException e) {
			// When something went wrong log the error as a warning and then
			// let instance start.
			e.printStackTrace();
			return true;
		}
	}

	/**
	 * Unregisters this instance of the application. If this is the first instance then the server is closed and the port is removed from the preferences. If this is not the first instance then this method does nothing.
	 *
	 * This method should be called when the application exits. But it is not a requirement. When you don't do this then the port number will stay in the preferences so on next start of the application this port number must be validated. So by calling this method on application exit you just save the time for this port validation.
	 *
	 * @param mainClass
	 *            The main class of the application. Must not be null. This is used as the user node key for the preferences.
	 */
	public void unregister(final Class<?> mainClass) {
		if (mainClass == null)
			throw new IllegalArgumentException("mainClass must be set");

		// Nothing to do when no server socket is present
		if (server == null)
			return;

		// Close the server socket
		server.stop();
		server = null;

		// Remove the port from the preferences
		final Preferences prefs = Preferences.userNodeForPackage(mainClass);
		prefs.remove(PORT_KEY);
		try {
			prefs.flush();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the port which was last recorded as active.
	 *
	 * @param mainClass
	 *            The main class of the application.
	 * @return The active port address or null if not found.
	 */
	private Integer getActivePort(final Class<?> mainClass) {
		final Preferences prefs = Preferences.userNodeForPackage(mainClass);
		final int port = prefs.getInt(PORT_KEY, -1);
		return port >= MIN_PORT && port <= MAX_PORT ? port : null;
	}

	/**
	 * Remembers an active port number in the preferences.
	 *
	 * @param mainClass
	 *            The main class of the application.
	 * @param port
	 *            The port number.
	 */
	private void setActivePort(final Class<?> mainClass, final int port) {
		final Preferences prefs = Preferences.userNodeForPackage(mainClass);
		prefs.putInt(PORT_KEY, port);
		try {
			prefs.flush();
		} catch (final BackingStoreException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns a random port number.
	 *
	 * @return A random port number.
	 */
	private int getRandomPort() {
		return random.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
	}

	/**
	 * Opens a client socket to the specified port. If this is successful then the port is returned, otherwise it is closed and null is returned.
	 *
	 * @param appId
	 *            The application ID.
	 * @param port
	 *            The port number to connect to.
	 * @return The client socket or null if no connection to the server was possible or the server is not the same application.
	 */
	private Socket openClientSocket(final String appId, final int port) {
		try {
			Socket socket = new Socket(InetAddress.getByName(null), port);
			try {
				// Open communication channels
				final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

				// Read the appId from the server. Use a one second timeout for
				// this just in case some unresponsive application listens on
				// this port.
				socket.setSoTimeout(1000);
				final String serverAppId = in.readLine();
				socket.setSoTimeout(0);

				// Abort if server app ID doesn't match (Or there was none at
				// all)
				if (serverAppId == null || !serverAppId.equals(appId)) {
					socket.close();
					socket = null;
				}

				return socket;
			} catch (final IOException e) {
				socket.close();
				return null;
			}
		} catch (final IOException e) {
			return null;
		}
	}

	/**
	 * Runs the client.
	 *
	 * @param socket
	 *            The client socket.
	 * @param args
	 *            The command-line arguments.
	 * @return True if server accepted the new instance, false if not.
	 * @throws IOException
	 *             When communication with the server fails.
	 */
	private boolean runClient(final Socket socket, final String[] args) throws IOException {
		// Send serialized command-line argument list to the server.
		final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		out.writeObject(new File(".").getCanonicalFile());
		out.writeObject(args);
		out.flush();

		// Read response from server
		final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		final String response = in.readLine();

		// If response is "exit" then don't start new instance. Any other
		// reply will allow the new instance.
		return response == null || !response.equals("exit");
	}

	/**
	 * Runs the server in a new thread and then returns.
	 *
	 * @param mainClass
	 *            The main class.
	 */
	private void runServer(final Class<?> mainClass) {
		while (true) {
			final String appId = mainClass.getName();
			final int port = getRandomPort();
			try {
				server = new OneInstanceServer(appId, port);
				setActivePort(mainClass, port);
				server.start();
			} catch (final PortAlreadyInUseException e) {
				// Ignored, trying next port.
			}
			return;
		}
	}

	/**
	 * Fires the newInstance event. When no listeners are registered then this method always returns false. If at least one listener accepts the new instance then true is returned.
	 *
	 * @param workingDir
	 *            The current working directory of the client. Needed if relative pathnames are specified on the command line because the server may currently be in a different directory than the client.
	 * @param args
	 *            The command line arguments of the new instance.
	 * @return True if the new instance is allowed to start, false if it must exit.
	 */
	boolean fireNewInstance(final File workingDir, final String[] args) {
		boolean start = false;
		for (final OneInstanceListener listener : listeners)
			start |= listener.newInstanceCreated(workingDir, args);
		return start;
	}
}
