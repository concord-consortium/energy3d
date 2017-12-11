package org.concord.energy3d.agents;

/**
 * @author Charles Xie
 *
 */
public class Feedback {

	private final int type;
	private final String message;
	private final boolean negate;

	public Feedback(final int type, final boolean negate, final String message) {
		this.type = type;
		this.negate = negate;
		this.message = message;
	}

	public boolean getNegate() {
		return negate;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

}
