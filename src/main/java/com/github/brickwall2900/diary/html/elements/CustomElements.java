package com.github.brickwall2900.diary.html.elements;

import java.util.HashMap;
import java.util.Map;

public class CustomElements {
    public static final CustomElement BUTTON = new ButtonElement();

    public static final Map<String, CustomElement> MAP = new HashMap<>();
    public static void initElements() {
        if (MAP.isEmpty()) {
            MAP.put(BUTTON.getName(), BUTTON);
        }
    }
}
