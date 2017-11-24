package org.concord.energy3d.agents;

import java.net.URL;

/**
 * 
 * @author Charles Xie
 *
 */
public class QuestionnaireEvent implements NonundoableEvent {

	QuestionnaireModel model;
	long timestamp;
	URL file;

	public QuestionnaireEvent(final URL file, final long timestamp, final QuestionnaireModel model) {
		this.file = file;
		this.timestamp = timestamp;
		this.model = model;
	}

	public QuestionnaireModel getModel() {
		return model;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getName() {
		return model.getQuestion() + ":" + model.getChoice();
	}

	@Override
	public char getOneLetterCode() {
		return 'Q';
	}

	@Override
	public URL getFile() {
		return file;
	}

}
