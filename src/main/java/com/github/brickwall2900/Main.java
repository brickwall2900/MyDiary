package com.github.brickwall2900;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static final Main INSTANCE = new Main();

    public static void main(String[] args) {
        try {
            INSTANCE.setup();
            INSTANCE.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DiaryFrame frame;

    public void setup() {
        FlatLightLaf.setup();
        frame = new DiaryFrame();
    }

    public void start() {
        if (DiaryStore.hasDiaryBeenCreated()) {
            DiarySetup.askForKey();
            DiaryStore.load(DiaryStore.DIARY_FILE);
            DiarySetup.applyConfiguration(frame, DiaryStore.CONFIGURATION);
            frame.setVisible(true);
        } else {
            if (askCreate()) {
                DiarySetup.setup();
                DiaryStore.createDiary();
                DiaryStore.save(DiaryStore.DIARY_FILE);
                frame.setVisible(true);
            }
        }
    }

    private boolean askCreate() {
        return JOptionPane.showConfirmDialog(null,
                "Hello %s, would you like to create a new diary now?".formatted(DiaryStore.USERNAME),
                DiaryFrame.TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}