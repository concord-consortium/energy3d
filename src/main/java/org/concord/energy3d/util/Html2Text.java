package org.concord.energy3d.util;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * @author Charles Xie
 *
 */
public class Html2Text extends HTMLEditorKit.ParserCallback {

	private StringBuffer s;

	public Html2Text() {
	}

	public void parse(final Reader in) throws IOException {
		s = new StringBuffer();
		final ParserDelegator delegator = new ParserDelegator();
		delegator.parse(in, this, Boolean.TRUE);
	}

	@Override
	public void handleText(final char[] text, final int pos) {
		s.append(text);
		s.append("\n\n");
	}

	public String getText() {
		return s.toString();
	}

}
