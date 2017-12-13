package org.concord.energy3d.agents;

/**
 * @author Charles Xie
 *
 */
public class Feedback {

	private final int type;
	private final String message;
	private final boolean negate;
	private String customMessage;

	public Feedback(final int type, final boolean negate, final String message) {
		this.type = type;
		this.negate = negate;
		this.message = message;
		this.customMessage = message;
	}

	public void setCustomMessage(final String customMessage) {
		this.customMessage = customMessage;
	}

	public String getCustomMessage() {
		return customMessage;
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
