package com.github.brickwall2900.diary;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.diary.dialogs.DiaryErrorMessage;
import com.github.brickwall2900.diary.utils.TextAreaOutputStream;

import javax.swing.*;
import java.io.PrintWriter;

public class Main {
    public static final Main INSTANCE = new Main();

    public static void main(String[] args) {
        try {
            INSTANCE.setup();
            INSTANCE.start();
        } catch (Throwable e) { // greatest exception handling ever
            e.printStackTrace();
            INSTANCE.errorMessage.showErrorMessage(e);
            System.exit(1);
        }
    }

    public DiaryFrame frame;
    public DiaryErrorMessage errorMessage;

    public void setup() {
        FlatLightLaf.setup();
        errorMessage = new DiaryErrorMessage();
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
                "Hello %s! Would you like to create a new diary now?".formatted(DiaryStore.USERNAME),
                DiaryFrame.TITLE, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}