package com.github.brickwall2900;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import org.httprpc.sierra.TaskExecutor;

import javax.swing.*;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.brickwall2900.ThisIsAnInsaneEncryptAlgorithm.generateUUIDFromString;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DiarySetup {
    protected static ThisIsAnInsaneEncryptAlgorithm.Key key = null;

    public static final TaskExecutor TASK_EXECUTOR = new TaskExecutor(new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, SECONDS, new SynchronousQueue<Runnable>()));

    public static void askForKey() {
        byte iterations = askIterations();
        long uid1 = askRandomNumber();
        char[] uid2 = askPassword();
        key = new ThisIsAnInsaneEncryptAlgorithm.Key(iterations, uid1, generateUUIDFromString(uid2));
    }

    public static void setup() {
        int confirmed;
        do {
            byte iterations = askIterations();
            long uid1 = askRandomNumber();
            char[] uid2 = askPassword();
            confirmed = confirmOptions(iterations, uid1, uid2);
            if (confirmed == JOptionPane.YES_OPTION) {
                key = new ThisIsAnInsaneEncryptAlgorithm.Key(iterations, uid1, generateUUIDFromString(uid2));
            } else if (confirmed == JOptionPane.CANCEL_OPTION) {
                System.exit(0);
            }
        } while (confirmed == JOptionPane.NO_OPTION);
    }

    private static int confirmOptions(byte iterations, long uid1, char[] uid2) {
        String builder = "Are you sure these options are correct? If so, please write them down or memorize." + '\n' +
                "1st option: " + iterations + '\n' +
                "2nd option: " + uid1 + '\n' +
                "3rd option: " + new String(uid2) + '\n';
        return JOptionPane.showConfirmDialog(null, builder);
    }

    private static byte askIterations() {
        try {
            String in = JOptionPane.showInputDialog(null, "Choose a random number between 1 to 100! (the larger the number, the longer it takes to load!)");
            if (in != null && !in.isBlank()) {
                byte b = Byte.decode(in);
                if (1 <= b && b <= 100) {
                    return b;
                } else {
                    throwIllegalArgument("Number wasn't in the specified range!");
                }
            } else {
                throwIllegalArgument("Input was empty");
            }
        } catch (NumberFormatException e) {
            throwIllegalArgument("An error occurred while trying to parse number!");
        }
        return 0;
    }

    private static long askRandomNumber() {
        try {
            String in = JOptionPane.showInputDialog(null, "Choose a random number between... This time from -9 quintillion all the way to +9 quintillion!");
            if (in != null && !in.isBlank()) {
                return Long.decode(in);
            } else {
                throwIllegalArgument("Input was empty");
            }
        } catch (NumberFormatException e) {
            throwIllegalArgument("An error occurred while trying to parse number!");
        }
        return 0;
    }

    private static char[] askPassword() {
        JPasswordField pwd = new JPasswordField(10);
        int action = JOptionPane.showConfirmDialog(null, pwd,"Now enter a password: ", JOptionPane.OK_CANCEL_OPTION);
        if (action == 0)  {
            return pwd.getPassword();
        } else {
            throwIllegalArgument("You've didn't put a password in! A password is required.");
            return null;
        }
    }

    private static void throwIllegalArgument(String message) {
        JOptionPane.showMessageDialog(null, message, "", JOptionPane.ERROR_MESSAGE);
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
        lafChange.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }
}
