package org.concord.energy3d.agents;

/**
 * @author Charles Xie
 *
 */
class FeedbackPool {

	private final String[][] feedbackItems;
	private final int[] level;

	FeedbackPool(final int numberOfCases, final int numberOfOptions) {
		feedbackItems = new String[numberOfCases][numberOfOptions];
		level = new int[numberOfCases];
	}

	int getNumberOfCases() {
		return feedbackItems.length;
	}

	int getNumberOfOptions() {
		return feedbackItems[0].length;
	}

	void setItem(final int iCase, final int iOption, final String item) {
		feedbackItems[iCase][iOption] = item;
	}

	void forward(final int iCase) {
		level[iCase]++;
		if (level[iCase] >= getNumberOfOptions()) {
			level[iCase] = getNumberOfOptions() - 1;
		}
	}

	void backward(final int iCase) {
		level[iCase]--;
		if (level[iCase] < 0) {
			level[iCase] = 0;
		}
	}

	String getCurrentItem(final int iCase) {
		return feedbackItems[iCase][level[iCase]];
	}

}
