package com.github.brickwall2900.diary;

import com.github.brickwall2900.diary.dialogs.DiaryLoadDialog;
import com.github.brickwall2900.diary.utils.ThisIsAnInsaneEncryptAlgorithm;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.brickwall2900.diary.Main.INSTANCE;
import static com.github.brickwall2900.diary.utils.TranslatableText.friendlyException;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;

public class DiaryStore {
    public static final Path HOME;

    public static final int FILE_VERSION = 1002;

    public static class DiaryEntry implements Comparable<DiaryEntry>, Serializable {
        public String name;
        public LocalDate date;
        public LocalTime time;

        public DiaryEntry(String name, LocalDate date, LocalTime time) {
            this.name = name;
            this.date = date;
            this.time = time;
        }

        @Override
        public String toString() {
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CONFIGURATION.timeFormat);
            return text("entry.info", name, formatter.format(dateTime));
        }
        @Override
        public int compareTo(DiaryEntry o) {
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            LocalDateTime comparingAgainst = LocalDateTime.of(o.date, o.time);
            return dateTime.compareTo(comparingAgainst);
        }

    }
    public static Map<DiaryEntry, String> ENTRIES = new HashMap<>(), HIDDEN_ENTRIES = new HashMap<>();

    public static DiaryStore.DiaryEntry currentEntry;
    public static Path currentFile;

    public static String currentName;

    public static DiaryConfiguration CONFIGURATION = new DiaryConfiguration();

    static {
        HOME = Path.of(System.getProperty("user.home"), ".personal1");
        try {
            Files.createDirectories(HOME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DiaryStore() {}

    public static void destroyStore() {
        currentEntry = null;
        currentName = null;
        ENTRIES.clear();
        HIDDEN_ENTRIES.clear();
        ENTRIES = new HashMap<>();
        HIDDEN_ENTRIES = new HashMap<>();
        CONFIGURATION = new DiaryConfiguration();
    }

    public static boolean isFileNameIllegal(String fileName) {
        if (fileName.contains("/")) return true;
        try {
            Paths.get(fileName);
            return false;
        } catch (InvalidPathException e) {
            return true;
        }
    }

    public static boolean isFileNameIllegal(Path path) {
        String fileName = path.getFileName().toString();
        return isFileNameIllegal(fileName);
    }

    public static void createDiary(Path path) {
        if (!isFileNameIllegal(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, text("store.error.createFile", friendlyException(e)), text("title"), JOptionPane.ERROR_MESSAGE);
                throw new DiaryException(e);
            }
        } else {
            throw new DiaryException(text("store.error.illegalName", path));
        }

    }

    public static List<DiaryEntry> getSortedEntryList() {
        return new java.util.ArrayList<>(ENTRIES.keySet().stream().sorted().toList());
    }

    protected static void load(Path path) {
        ThisIsAnInsaneEncryptAlgorithm.Key key = DiarySetup.key;
        DiaryLoadDialog load = INSTANCE.frame.loadDialog;
        SwingUtilities.invokeLater(() -> load.openLoadDialog(text("store.load.dialog"), 100));
        if (key != null) {
            try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(path))) {
                StringBuilder nameBuilder = new StringBuilder();
                int b;
                while ((b = fis.read()) != 0) {
                    nameBuilder.append((char) b);
                }
                String nameRead = nameBuilder.toString();

                try (ByteArrayInputStream bis = new ByteArrayInputStream(ThisIsAnInsaneEncryptAlgorithm.decrypt(key, fis.readAllBytes()));
                     GZIPInputStream gis = new GZIPInputStream(bis);
                     ObjectInputStream ois = new ObjectInputStream(gis);) {
                    int fileVersion = ois.readInt();
                    boolean compatible = fileVersion == FILE_VERSION;
                    if (!compatible) {
                        compatible = JOptionPane.showConfirmDialog(null, text("store.warning.incompatibleVersion", FILE_VERSION, fileVersion), text("title"), YES_NO_OPTION) == YES_OPTION;
                    }
                    if (!compatible) {
                        throw new DiaryException(text("store.error.incompatibleVersion"));
                    }
                    DiaryState state = (DiaryState) ois.readObject();
                    ENTRIES = state.entries;
                    CONFIGURATION = state.configuration;
                    HIDDEN_ENTRIES = state.hiddenEntries;
                    currentName = nameRead;
                }
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                SwingUtilities.invokeLater(load::closeLoadDialog);
                JOptionPane.showMessageDialog(null, text("store.error.load", friendlyException(e)), text("title"), JOptionPane.ERROR_MESSAGE);
                throw new DiaryException(text("store.error.load"), e);
            }
        }
        SwingUtilities.invokeLater(load::closeLoadDialog);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static Path getFileByName(String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
        String fileName = bytesToHex(digest.digest(nameBytes));
        return HOME.resolve(fileName);
    }

    protected static void save(Path path) {
        ThisIsAnInsaneEncryptAlgorithm.Key key = DiarySetup.key;
        if (key != null) {
            try (BufferedOutputStream fos = new BufferedOutputStream(Files.newOutputStream(path));
                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 GZIPOutputStream gos = new GZIPOutputStream(bos, true);
                 ObjectOutputStream oos = new ObjectOutputStream(gos)) {
                DiaryLoadDialog load = INSTANCE.frame.loadDialog;
                SwingUtilities.invokeLater(() -> load.openLoadDialog(text("store.save.save"), 25));
                oos.writeInt(FILE_VERSION);
                oos.writeObject(new DiaryState(currentName, CONFIGURATION, ENTRIES, HIDDEN_ENTRIES));
                oos.flush();
                SwingUtilities.invokeLater(() -> load.openLoadDialog(text("store.save.compress"), 50));
                gos.flush();
                byte[] raw = bos.toByteArray();
                SwingUtilities.invokeLater(() -> load.openLoadDialog(text("store.save.encrypt"), 75));
                byte[] enc = ThisIsAnInsaneEncryptAlgorithm.encrypt(key, raw);
                SwingUtilities.invokeLater(() -> load.openLoadDialog(text("store.save.write"), 100));
                fos.write(currentName.getBytes()); fos.write(0);
                fos.write(enc);
                ThisIsAnInsaneEncryptAlgorithm.eraseData(enc);
                ThisIsAnInsaneEncryptAlgorithm.eraseData(raw);
                SwingUtilities.invokeLater(load::closeLoadDialog);
            } catch (IOException | RuntimeException | NoSuchAlgorithmException e) {
                DiaryLoadDialog load = INSTANCE.frame.loadDialog;
                SwingUtilities.invokeLater(load::closeLoadDialog);
                JOptionPane.showMessageDialog(null, text("store.error.save", friendlyException(e)), text("title"), JOptionPane.ERROR_MESSAGE);
                throw new DiaryException(text("store.error.save"), e);
            }
        }
    }

    public static void wrapSaveThenCallLater(Path path, Runnable runnable) {
        Thread thread = new Thread(() -> {
            save(path);
            if (runnable != null) runnable.run();
        });
        thread.setName("Save Thread");
        thread.start();
    }

    public static String getNameOnFile(Path path) {
        try (BufferedInputStream fis = new BufferedInputStream(Files.newInputStream(path))) {
            StringBuilder nameBuilder = new StringBuilder();
            int b;
            while ((b = fis.read()) > 0) {
                nameBuilder.append((char) b);
            }
            return nameBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String[] listPath(Path path) {
        String[] names = new String[0];
        try (Stream<Path> list = Files.list(path)) {
            names = list.map(p -> p.getFileName().toString()).toList().toArray(names);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return names;
    }

    public static String[] listSaves() {
        String[] fileList = listPath(HOME);
        String[] saves = new String[0];
        if (fileList != null) {
            saves = Arrays.stream(fileList).map(n -> getNameOnFile(HOME.resolve(n))).toList().toArray(saves);
        }
        return saves;
    }

    public static class DiaryConfiguration implements Serializable {

        public boolean darkMode = false;
        public String timeFormat = "dd-MM-yyy HH:mm";
        public String template = """
            # {name}
            
            {time}
            
            ...
            """;

        public DiaryConfiguration(boolean darkMode, String timeFormat, String template, String name) {
            this.darkMode = darkMode;
            this.timeFormat = timeFormat;
            this.template = template;
        }

        public DiaryConfiguration() {
        }
    }
    public record DiaryState(String name, DiaryConfiguration configuration, Map<DiaryEntry, String> entries, Map<DiaryEntry, String> hiddenEntries) implements Serializable {}
}
