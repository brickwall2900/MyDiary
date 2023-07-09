package com.github.brickwall2900;

import com.github.brickwall2900.dialogs.DiaryLoadDialog;
import com.github.brickwall2900.utils.ThisIsAnInsaneEncryptAlgorithm;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.brickwall2900.DiaryFrame.TITLE;
import static com.github.brickwall2900.Main.INSTANCE;
import static com.github.brickwall2900.utils.ThisIsAnInsaneEncryptAlgorithm.*;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class DiaryStore {
    public static final String USERNAME;
    private static final File HOME;
    public static final File DIARY_FILE;

    public static final int FILE_VERSION = 1000;

    public record DiaryEntry(String name, LocalDate date, LocalTime time) implements Comparable<DiaryEntry>, Serializable {
        @Override
        public String toString() {
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CONFIGURATION.timeFormat);
            return name + ", created on " + formatter.format(dateTime);
        }
        @Override
        public int compareTo(DiaryEntry o) {
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            LocalDateTime comparingAgainst = LocalDateTime.of(o.date, o.time);
            return dateTime.compareTo(comparingAgainst);
        }

    }
    public static Map<DiaryEntry, String> ENTRIES = new HashMap<>();

    public static DiaryConfiguration CONFIGURATION = new DiaryConfiguration();
    static {
        USERNAME = System.getProperty("user.name");
        HOME = new File(System.getProperty("user.home"));
        DIARY_FILE = new File(HOME, ".personal1");
    }

    private DiaryStore() {}

    public static boolean hasDiaryBeenCreated() {
        return DIARY_FILE.exists();
    }

    public static boolean createDiary() {
        try {
            for (int i = 0; i < 10 || DIARY_FILE.createNewFile(); i++);
            return true;
        } catch (IOException e) {
            throw new DiaryException("Error creating file!", e);
        }
    }

    public static List<DiaryEntry> getSortedEntryList() {
        return new java.util.ArrayList<>(ENTRIES.keySet().stream().sorted().toList());
    }

    protected static void load(File file) {
        ThisIsAnInsaneEncryptAlgorithm.Key key = DiarySetup.key;
        DiaryLoadDialog load = INSTANCE.frame.loadDialog;
        SwingUtilities.invokeLater(() -> load.openLoadDialog("Restoring Diary state...", 100));
        if (key != null) {
            try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
                 ByteArrayInputStream bis = new ByteArrayInputStream(decrypt(key, fis.readAllBytes()));
                 GZIPInputStream gis = new GZIPInputStream(bis);
                 ObjectInputStream ois = new ObjectInputStream(gis)) {

                DiaryState state = (DiaryState) ois.readObject();
                if (state.fileVersion == FILE_VERSION) {
                    ENTRIES = state.entries;
                    CONFIGURATION = state.configuration;
                } else if (JOptionPane.showConfirmDialog(null, "The fileVersion you've loaded is incompatible with the current fileVersion! (%d (prog. ver.) != %d (file ver.))\n".formatted(FILE_VERSION, state.fileVersion) +
                        "Are you sure you want to continue?", TITLE, YES_NO_OPTION) == YES_OPTION) {
                    ENTRIES = state.entries;
                    CONFIGURATION = state.configuration;
                } else {
                    throw new DiaryException("Incompatible versions!");
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new DiaryException("Error loading from file!", e);
            }
        }
        SwingUtilities.invokeLater(load::closeLoadDialog);
    }

    protected static void save(File file) {
        ThisIsAnInsaneEncryptAlgorithm.Key key = DiarySetup.key;
        if (key != null) {
            try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 GZIPOutputStream gos = new GZIPOutputStream(bos, true);
                 ObjectOutputStream oos = new ObjectOutputStream(gos)) {
                DiaryLoadDialog load = INSTANCE.frame.loadDialog;
                SwingUtilities.invokeLater(() -> load.openLoadDialog("Saving...", 25));
                oos.writeObject(new DiaryState(CONFIGURATION, ENTRIES, FILE_VERSION));
                oos.flush();
                SwingUtilities.invokeLater(() -> load.openLoadDialog("Compressing...", 50));
                gos.flush();
                byte[] raw = bos.toByteArray();
                SwingUtilities.invokeLater(() -> load.openLoadDialog("Encrypting...", 75));
                byte[] enc = encrypt(key, raw);
                SwingUtilities.invokeLater(() -> load.openLoadDialog("Writing...", 100));
                fos.write(enc);
                eraseData(enc);
                eraseData(raw);
                SwingUtilities.invokeLater(load::closeLoadDialog);
            } catch (IOException e) {
                throw new DiaryException("Error saving to file!", e);
            }
        }
    }

    public static void saveAndExit(File file) {
        save(file);
        System.exit(0);
    }

    public static class DiaryConfiguration implements Serializable {

        public boolean darkMode = false;
        public String timeFormat = "dd-MM-yyy HH:mm";
        public String template = """
            # {name}
            
            {time}
            
            ...
            """;

        public DiaryConfiguration(boolean darkMode, String timeFormat, String template) {
            this.darkMode = darkMode;
            this.timeFormat = timeFormat;
            this.template = template;
        }

        public DiaryConfiguration() {
        }
    }
    public record DiaryState(DiaryConfiguration configuration, Map<DiaryEntry, String> entries, int fileVersion) implements Serializable {}
}
