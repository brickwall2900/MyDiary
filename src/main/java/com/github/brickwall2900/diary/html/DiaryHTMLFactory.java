package com.github.brickwall2900.diary.html;

import com.github.brickwall2900.diary.html.elements.CustomElement;
import com.github.brickwall2900.diary.html.elements.CustomElements;

import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class DiaryHTMLFactory extends HTMLEditorKit.HTMLFactory implements ViewFactory {
    @Override
    public View create(Element elem) {
        HTML.Tag kind = (HTML.Tag) elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
        if (kind instanceof HTML.UnknownTag) {
            CustomElement customElement = CustomElements.MAP.getOrDefault(elem.getName(), null);
            if (customElement != null) {
                return customElement.createView(elem);
            }
        }
        return super.create(elem);
    }
}
