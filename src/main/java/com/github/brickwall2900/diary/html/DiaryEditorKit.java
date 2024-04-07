package com.github.brickwall2900.diary.html;

import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class DiaryEditorKit extends HTMLEditorKit {
    public Parser defaultParser;

    @Override
    protected Parser getParser() {
        if (defaultParser == null)
            defaultParser = new DiaryHTMLParserDelegator();

        return defaultParser;
    }

    @Override
    public Document createDefaultDocument() {
        StyleSheet styles = getStyleSheet();
        StyleSheet ss = new StyleSheet();

        ss.addStyleSheet(styles);

        DiaryHTMLDocument doc = new DiaryHTMLDocument(ss);
        doc.setParser(getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        return doc;
    }

    @Override
    public ViewFactory getViewFactory() {
        return new DiaryHTMLFactory();
    }
}
