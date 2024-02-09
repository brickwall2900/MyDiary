package com.github.brickwall2900.diary;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.github.brickwall2900.diary.dialogs.DiaryPasswordDialog;
import com.github.brickwall2900.diary.utils.ThisIsAnInsaneEncryptAlgorithm;
import org.httprpc.sierra.TaskExecutor;

import javax.swing.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.brickwall2900.diary.utils.ThisIsAnInsaneEncryptAlgorithm.eraseData;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.swing.JOptionPane.*;

public class DiarySetup {
    protected static ThisIsAnInsaneEncryptAlgorithm.Key key = null;

    public static void askForKey() {
        byte iterations = askIterations();
        long uid1 = askRandomNumber();
        char[] uid2 = askPassword();
        key = new ThisIsAnInsaneEncryptAlgorithm.Key(iterations, uid1, ThisIsAnInsaneEncryptAlgorithm.generateUUIDFromString(uid2));
    }

    public static void setup() {
        int confirmed;
        do {
            byte iterations = askIterations();
            long uid1 = askRandomNumber();
            char[] uid2 = askPassword();
            confirmed = confirmOptions(iterations, uid1, uid2);
            if (confirmed == YES_OPTION) {
                key = new ThisIsAnInsaneEncryptAlgorithm.Key(iterations, uid1, ThisIsAnInsaneEncryptAlgorithm.generateUUIDFromString(uid2));
                eraseData(uid2);
            } else if (confirmed == CANCEL_OPTION) {
                System.exit(0);
            }
        } while (confirmed == NO_OPTION);
    }

    private static int confirmOptions(byte iterations, long uid1, char[] uid2) {
        String builder = text("password.confirm", iterations, uid1, new String(uid2));
        return showConfirmDialog(null, builder);
    }

    private static byte askIterations() {
        try {
            String in = showInputDialog(null, text("password.option1"));
            if (in != null && !in.isBlank()) {
                byte b = Byte.decode(in);
                if (1 <= b && b <= 100) {
                    return b;
                } else {
                    throwIllegalArgument(text("password.error.outOfRange"));
                }
            } else {
                throwIllegalArgument(text("password.error.emptyInput"));
            }
        } catch (NumberFormatException e) {
            throwIllegalArgument(text("password.error.numberFormat"));
        }
        return 0;
    }

    private static long askRandomNumber() {
        try {
            String in = showInputDialog(null, text("password.option2"));
            if (in != null && !in.isBlank()) {
                return Long.decode(in);
            } else {
                throwIllegalArgument(text("password.error.emptyInput"));
            }
        } catch (NumberFormatException e) {
            throwIllegalArgument(text("password.error.numberFormat"));
        }
        return 0;
    }

    private static char[] askPassword() {
        DiaryPasswordDialog passwordDialog = Main.INSTANCE.frame.passwordDialog;
        char[] pwd = passwordDialog.askPassword();
        if (pwd == null) {
            throwIllegalArgument(text("password.error.notEntered"));
        }
        return pwd;
    }

    private static void throwIllegalArgument(String message) {
        showMessageDialog(null, message, "", ERROR_MESSAGE);
        throw new IllegalArgumentException(message);
    }

    public static void applyConfiguration(DiaryFrame lafChange, DiaryStore.DiaryConfiguration configuration) {
        if (configuration.darkMode) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
       }
        FlatAnimatedLafChange.showSnapshot();
        lafChange.editor.changeTheme(configuration.darkMode);
        lafChange.configurationDialog.templateEditor.changeTheme(configuration.darkMode);
        lafChange.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }
}
