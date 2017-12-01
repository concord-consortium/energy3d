package org.concord.energy3d.agents;

import java.net.URL;

/**
 * 
 * @author Charles Xie
 *
 */
public class DataCollectionEvent implements NonundoableEvent {

	String name;
	String value;
	long timestamp;
	URL file;

	public DataCollectionEvent(final URL file, final long timestamp, final String name, final String value) {
		this.file = file;
		this.timestamp = timestamp;
		this.name = name;
		this.value = value;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	@Override
	public char getOneLetterCode() {
		return 'D';
	}

	@Override
	public URL getFile() {
		return file;
	}

}
