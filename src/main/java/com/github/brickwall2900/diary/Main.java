package com.github.brickwall2900.diary;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.brickwall2900.diary.dialogs.DiaryErrorMessage;

import javax.swing.*;

import static com.github.brickwall2900.diary.utils.TranslatableText.text;

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
                text("password.firstTime", DiaryStore.USERNAME),
                text("title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}