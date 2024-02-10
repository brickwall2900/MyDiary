package com.github.brickwall2900.diary.utils;

import java.io.*;
import java.util.*;

public class TranslatableText {
    public static final Random RANDOM = new Random();
    public static final Properties TEXT = new Properties();
    public static final ArrayList<String> INTRO = new ArrayList<>();

    static {
        Locale locale = Locale.getDefault();
        String tag = locale.toLanguageTag();
        try (InputStream stream = TranslatableText.class.getResourceAsStream("/lang/" + tag + "/lang.properties")) {
            TEXT.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Language file can't be read: " + tag);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TranslatableText.class.getResourceAsStream("/lang/" + tag + "/introduction.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                INTRO.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Language file can't be read: " + tag);
        }
    }

    public static String text(String text) {
        return TEXT.getProperty(text, text);
    }

    public static String text(String text, Object... formatting) {
        return text(text).formatted(formatting);
    }

    public static String intro(Map<String, Object> map) {
        String intro = INTRO.get(RANDOM.nextInt(INTRO.size()));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            intro = intro.replace('{' + entry.getKey() + '}', entry.getValue().toString());
        }
        return intro;
    }

    public static String friendlyException(Throwable t) {
        Queue<Throwable> causeChain = new ArrayDeque<>();
        Throwable tmpCause = t.getCause();
        while (tmpCause != null) {
            causeChain.offer(tmpCause);
            tmpCause = tmpCause.getCause();
        }
        causeChain.offer(t);
        while (true) {
            tmpCause = causeChain.poll();
            if (tmpCause != null) {
                String key = "exceptions." + tmpCause.getClass().getName();
                String translated = text(key);
                if (!translated.equals(key)) {
                    String[] cases = translated.split(":::");
                    for (String c : cases) {
                        String sub = c.substring(1, c.length() - 1);
                        String[] split = sub.split("\\{:\\}");
                        String regex = null, newMessage;
                        if (split.length == 2) {
                            regex = split[0];
                            newMessage = split[1];
                        } else {
                            newMessage = split[0];
                        }
                        String message = tmpCause.getMessage();
                        if (message != null && regex != null && message.matches(regex)) {
                            return newMessage;
                        } else if (regex == null && split.length == 1) { // generic message
                            return newMessage;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return t.toString();
    }
}
