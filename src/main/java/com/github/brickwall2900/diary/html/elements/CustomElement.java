package com.github.brickwall2900.diary.html.elements;

import javax.swing.text.Element;
import javax.swing.text.View;

public abstract class CustomElement {
    private Element element;

    public abstract String getName();
    public abstract View createView(Element element);

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
