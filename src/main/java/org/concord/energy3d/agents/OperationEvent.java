package org.concord.energy3d.agents;

import java.net.URL;
import java.util.Map;

/**
 * @author Charles Xie
 *
 */
public class OperationEvent implements NonundoableEvent {

	String name;
	char oneLetterCode = 'O';
	long timestamp;
	URL file;
	Map<String, ?> attributes;

	public OperationEvent(final URL file, final long timestamp, final String name, final Map<String, ?> attributes) {
		this.file = file;
		this.timestamp = timestamp;
		this.name = name;
		this.attributes = attributes;
	}

	public OperationEvent(final URL file, final long timestamp, final char oneLetterCode, final String name, final Map<String, ?> attributes) {
		this.file = file;
		this.timestamp = timestamp;
		this.oneLetterCode = oneLetterCode;
		this.name = name;
		this.attributes = attributes;
	}

	public Map<String, ?> getAttributes() {
		return attributes;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public char getOneLetterCode() {
		return oneLetterCode;
	}

	@Override
	public URL getFile() {
		return file;
	}

}
