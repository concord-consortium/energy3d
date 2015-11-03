package org.concord.energy3d.gui;

import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author Charles Xie
 * 
 */
public class MyPlainDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;
	private String removedString;

	MyPlainDocument() {
		super();
	}

	public void remove(int offs, int len) throws BadLocationException {
		try {
			removedString = getText(offs, len);
		} catch (BadLocationException e) {
			e.printStackTrace();
			removedString = null;
		}
		super.remove(offs, len);
	}

	public String getRemovedString() {
		return removedString;
	}

}
