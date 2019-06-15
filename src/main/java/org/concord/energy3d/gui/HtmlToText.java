package org.concord.energy3d.gui;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

class HtmlToText extends HTMLEditorKit.ParserCallback {

    private StringBuffer s;

    HtmlToText() {
    }

    void parse(final Reader in) throws IOException {
        s = new StringBuffer();
        final ParserDelegator delegator = new ParserDelegator();
        delegator.parse(in, this, true);
    }

    @Override
    public void handleText(final char[] text, final int pos) {
        s.append(text);
        s.append("\n");
    }

    public String getText() {
        return s.toString();
    }

}