package org.concord.energy3d.agents;

/**
 * 
 * @author Charles Xie
 *
 */
public class QuestionnaireModel {

	private final String question, choice;
	private final boolean key;

	public QuestionnaireModel(final String question, final String choice, final boolean key) {
		this.question = question;
		this.choice = choice;
		this.key = key;
	}

	public String getQuestion() {
		return question;
	}

	public String getChoice() {
		return choice;
	}

	public boolean isKey() {
		return key;
	}

}
