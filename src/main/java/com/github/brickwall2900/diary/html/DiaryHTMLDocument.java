package com.github.brickwall2900.diary.html;

import com.github.brickwall2900.diary.html.elements.CustomElements;

import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.net.URL;

public class DiaryHTMLDocument extends HTMLDocument {
    public DiaryHTMLDocument(StyleSheet styles) {
        super(styles);
    }

    @Override
    public HTMLEditorKit.ParserCallback getReader(int pos) {
        Object desc = getProperty(Document.StreamDescriptionProperty);
        if (desc instanceof URL u) {
            setBase(u);
        }

        return new DiaryHTMLReader(pos);
    }

    class DiaryHTMLReader extends HTMLReader {

        public DiaryHTMLReader(int offset) {
            super(offset);
        }

        @Override
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (CustomElements.MAP.containsKey(t.toString())) {
                registerTag(t, new BlockAction());
            }
            super.handleStartTag(t, a, pos);
        }
    }
}
