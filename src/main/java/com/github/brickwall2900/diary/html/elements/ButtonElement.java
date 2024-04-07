package com.github.brickwall2900.diary.html.elements;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.View;
import java.awt.*;

public class ButtonElement extends CustomElement {
    @Override
    public String getName() {
        return "button";
    }

    @Override
    public View createView(Element element) {
        return new ComponentView(element) {
            @Override
            protected Component createComponent() {
                JButton button = new JButton("???");
                try {
                    int start = getElement().getStartOffset();
                    int end = getElement().getEndOffset();
                    String text = getElement().getDocument().getText(start, end - start);
                    button.setText(text);
                } catch (BadLocationException e) {
                    throw new IllegalStateException("WHOOPS!");
                }

                return button;
            }
        };
    }
}
