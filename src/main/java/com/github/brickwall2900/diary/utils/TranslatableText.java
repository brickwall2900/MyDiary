package com.github.brickwall2900.diary.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class TranslatableText {
    public static final Properties TEXT = new Properties();

    static {
        Locale locale = Locale.getDefault();
        String tag = locale.toLanguageTag();
        String fullName = tag + ".properties";
        try (InputStream stream = TranslatableText.class.getResourceAsStream("/lang/" + fullName)) {
            TEXT.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Language file can't be read: " + fullName);
        }
    }

    public static String text(String text) {
        return TEXT.getProperty(text, text);
    }

    public static String text(String text, Object... formatting) {
        return text(text).formatted(formatting);
    }
}
