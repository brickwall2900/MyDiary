package com.github.brickwall2900.utils;

import javax.swing.*;
import java.io.OutputStream;

public class TextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;

    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void write(int b) {
        textArea.append(String.valueOf((char) b));
    }

    @Override
    public void write(byte[] b) {
        textArea.append(new String(b));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        textArea.append(new String(b, off, len));
    }

    @Override
    public void close() {
    }
}
