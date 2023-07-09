package com.github.brickwall2900;

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

import javax.swing.*;

import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.github.brickwall2900.DiaryMarkdownToHTML.*;
import static com.github.brickwall2900.DiarySetup.applyConfiguration;
import static com.github.brickwall2900.DiarySetup.askForKey;
import static com.github.brickwall2900.DiaryStore.*;
import static java.awt.event.KeyEvent.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.*;
import static javax.swing.SwingUtilities.updateComponentTreeUI;

public class DiaryFrame extends JFrame implements ActionListener, WindowListener {
    public static final String TITLE = "The Diary";

    public JMenuBar menuBar;
        public JMenu fileMenu;
            public JMenuItem saveItem;
            public JMenuItem reloadItem;
            public JMenuItem backupItem;
            public JMenuItem loadFromBackupItem;
            public JMenuItem exitItem;
        public JMenu entriesMenu;
            public JMenuItem newEntryItem;
            public JMenuItem editEntryItem;
            public JMenuItem jumpToEntryItem;
            public JMenuItem removeEntryItem;
            public JSeparator separator1;
            public JMenuItem nextEntryItem, prevEntryItem;
        public JMenu aboutMenu;
            public JMenuItem preferencesMenu;
            public JMenuItem aboutItem;

    public FSScrollPane scrollPane;
        public XHTMLPanel htmlPanel;

    public DiaryMarkdownEditor editor;
    public DiaryNewEntryDialog newEntryDialog;
    public DiaryJumpToEntryDialog jumpToEntryDialog;
    public DiaryConfigurationDialog configurationDialog;
    public DiaryLoadScreen loadDialog;

    public JFileChooser fileChooser;

    public DiaryStore.DiaryEntry currentEntry;

    public DiaryFrame() {
        setTitle(TITLE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        buildMenuBar();

        htmlPanel = new XHTMLPanel();
        loadToHelpPage();
        scrollPane = new FSScrollPane(htmlPanel);
        getContentPane().add(scrollPane);

        editor = new DiaryMarkdownEditor(this);
        newEntryDialog = new DiaryNewEntryDialog(this);
        jumpToEntryDialog = new DiaryJumpToEntryDialog(this);
        configurationDialog = new DiaryConfigurationDialog(this);
        loadDialog = new DiaryLoadScreen(this);

        fileChooser = new JFileChooser();

        addWindowListener(this);
    }

    private void loadToHelpPage() {
        try {
            htmlPanel.setDocument(new ByteArrayInputStream(wrapHTMLIntoActualDocument(DIARY_HELP_PREDEF).getBytes(StandardCharsets.UTF_8)), null);
        } catch (Exception e) {
            throw new DiaryException("Error opening starting page!", e);
        }
    }

    private void buildMenuBar() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        entriesMenu = new JMenu("Entries");
        aboutMenu = new JMenu("About");

        saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_DOWN_MASK));
        saveItem.addActionListener(this);

        reloadItem = new JMenuItem("Reload");
        reloadItem.setAccelerator(KeyStroke.getKeyStroke(VK_R, CTRL_DOWN_MASK));
        reloadItem.addActionListener(this);

        backupItem = new JMenuItem("Backup");
        backupItem.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_DOWN_MASK | SHIFT_DOWN_MASK));
        backupItem.addActionListener(this);

        loadFromBackupItem = new JMenuItem("Load from Backup");
        loadFromBackupItem.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_DOWN_MASK | SHIFT_DOWN_MASK));
        loadFromBackupItem.addActionListener(this);

        exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(VK_F4, ALT_DOWN_MASK));

        preferencesMenu = new JMenuItem("Configuration");
        preferencesMenu.setAccelerator(KeyStroke.getKeyStroke(VK_ENTER, CTRL_DOWN_MASK));
        preferencesMenu.addActionListener(this);

        newEntryItem = new JMenuItem("New");
        newEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_N, CTRL_DOWN_MASK));
        newEntryItem.addActionListener(this);

        editEntryItem = new JMenuItem("Edit");
        editEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_E, CTRL_DOWN_MASK));
        editEntryItem.addActionListener(this);

        jumpToEntryItem = new JMenuItem("Jump to...");
        jumpToEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_F, CTRL_DOWN_MASK));
        jumpToEntryItem.addActionListener(this);

        removeEntryItem = new JMenuItem("Remove");
        removeEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_DELETE, 0));
        removeEntryItem.addActionListener(this);

        separator1 = new JSeparator();

        nextEntryItem = new JMenuItem("Next");
        nextEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_RIGHT, 0));
        nextEntryItem.addActionListener(this);

        prevEntryItem = new JMenuItem("Previous");
        prevEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_LEFT, 0));
        prevEntryItem.addActionListener(this);

        aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(this);

        fileMenu.add(saveItem);
        fileMenu.add(reloadItem);
        fileMenu.add(backupItem);
        fileMenu.add(loadFromBackupItem);
        fileMenu.add(exitItem);

        entriesMenu.add(newEntryItem);
        entriesMenu.add(editEntryItem);
        entriesMenu.add(jumpToEntryItem);
        entriesMenu.add(removeEntryItem);
        entriesMenu.add(separator1);
        entriesMenu.add(prevEntryItem);
        entriesMenu.add(nextEntryItem);

        aboutMenu.add(preferencesMenu);
        aboutMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(entriesMenu);
        menuBar.add(aboutMenu);

        setJMenuBar(menuBar);
    }

    public void addEntry() {
        DiaryStore.DiaryEntry entry = newEntryDialog.askNewEntry();
        if (entry != null) {
            boolean overwrite = !ENTRIES.containsKey(entry);
            if (!overwrite) {
                int confirmOverwrite = JOptionPane.showConfirmDialog(this, "Are you sure you want to overwrite this entry?", TITLE, YES_NO_OPTION, WARNING_MESSAGE);
                overwrite = confirmOverwrite == YES_OPTION;
            }
            if (overwrite) {
                String template = CONFIGURATION.template;
                LocalDateTime dateTime = LocalDateTime.of(entry.date(), entry.time());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CONFIGURATION.timeFormat);
                template = template.replace("{name}", entry.name()).replace("{time}", formatter.format(dateTime));
                ENTRIES.put(entry, template);
                updatePanelWithEntry(entry);
                currentEntry = entry;
            }
        }
    }

    public void removeCurrentEntry() {
        if (currentEntry != null) {
            String[] options = {"Yes", "NO!"};
            boolean confirmDelete = JOptionPane.showOptionDialog(this, "Are you sure you want to remove this entry? (CANNOT BE UNDONE!)", TITLE, DEFAULT_OPTION, WARNING_MESSAGE, null, options, options[1]) == 0;
            if (confirmDelete) {
                ENTRIES.put(currentEntry, "");
                ENTRIES.remove(currentEntry);
                try {
                    htmlPanel.setDocument(new ByteArrayInputStream(wrapHTMLIntoActualDocument(ENTRY_REMOVED_MESSAGE_PREDEF).getBytes(StandardCharsets.UTF_8)), null);
                } catch (Exception e) {
                    throw new DiaryException("Error removing an entry!", e);
                }
                currentEntry = null;
            }
        }
    }

    public void updatePanelWithEntry(DiaryStore.DiaryEntry entry) {
        if (entry != null) {
            String content = ENTRIES.get(entry);
            try {
                htmlPanel.setDocument(new ByteArrayInputStream(wrapHTMLIntoActualDocument(getHTMLFromMarkdown(content)).getBytes(StandardCharsets.UTF_8)), null);
            } catch (Exception e) {
                throw new DiaryException("HTML/Markdown Exception!", e);
            }
        }
    }

    public void updatePanelDirect(InputStream inputStream) {
        try {
            htmlPanel.setDocument(inputStream, null);
        } catch (Exception e) {
            throw new DiaryException("HTML Exception!", e);
        }
    }

    public void openEditorOnEntry(DiaryStore.DiaryEntry entry) {
        if (entry != null) {
            String content = ENTRIES.get(entry);
            editor.textArea.setText(content);
            editor.setEntryContext(entry);
            editor.setVisible(true);
        }
    }

    public void askJumpToEntry() {
        DiaryStore.DiaryEntry entry = jumpToEntryDialog.askJumpToEntry(currentEntry);
        if (entry != null) {
            currentEntry = entry;
            updatePanelWithEntry(entry);
        }
    }

    public DiaryStore.DiaryEntry getOffsetEntry(int offset) {
        if (currentEntry != null) {
            List<DiaryStore.DiaryEntry> entryList = getSortedEntryList();
            int idx = entryList.indexOf(currentEntry);
            idx += offset;
            if (0 <= idx && idx <= entryList.size() - 1) {
                return entryList.get(idx);
            }
        }
        return null;
    }

    public void nextEntry(boolean back) {
        DiaryStore.DiaryEntry entry = getOffsetEntry(!back ? 1 : -1);
        if (entry != null) {
            currentEntry = entry;
            updatePanelWithEntry(entry);
        }
    }

    public void updateUI() {
        updateComponentTreeUI(this);
        updateComponentTreeUI(newEntryDialog);
        updateComponentTreeUI(editor);
        updateComponentTreeUI(jumpToEntryDialog);
        updateComponentTreeUI(loadDialog);
        updateComponentTreeUI(fileChooser);
        configurationDialog.updateUI();
    }

    public void backup() {
        int val = fileChooser.showSaveDialog(this);
        if (val == APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            try {
                for (int i = 0; i < 10 || f.createNewFile(); i++);
            } catch (IOException e) {
                throw new DiaryException("Cannot create backup file!", e);
            }
            Thread t = new Thread(() -> save(f));
            t.setName("Save Thread");
            t.start();
        }
    }

    public void reload(File f) {
        String message = DIARY_FILE.equals(f) ? "Are you sure you want to reload? (This won't save the current file before reloading!)"
                : "Are you sure you want to reload from a backup? (Saving after opening will overwrite the main file and this won't save the current file before reloading!)";
        if (JOptionPane.showConfirmDialog(this, message, TITLE, YES_NO_OPTION, WARNING_MESSAGE) == YES_OPTION) {
            askForKey();
            load(f);
            applyConfiguration(this, CONFIGURATION);
            MD_TO_HTML_CACHE.clear();
            currentEntry = null;
            loadToHelpPage();
        }
    }

    public void loadFromBackup() {
        int val = fileChooser.showOpenDialog(this);
        if (val == APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            reload(f);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (editEntryItem.equals(source)) {
            openEditorOnEntry(currentEntry);
        } else if (newEntryItem.equals(source)) {
            addEntry();
        } else if (jumpToEntryItem.equals(source)) {
            askJumpToEntry();
        } else if (nextEntryItem.equals(source)) {
            nextEntry(false);
        } else if (prevEntryItem.equals(source)) {
            nextEntry(true);
        } else if (preferencesMenu.equals(source)) {
            configurationDialog.editOptions();
        } else if (saveItem.equals(source)) {
            Thread t = new Thread(() -> save(DIARY_FILE));
            t.setName("Save Thread");
            t.start();
        } else if (removeEntryItem.equals(source)) {
            removeCurrentEntry();
        } else if (backupItem.equals(source)) {
            backup();
        } else if (loadFromBackupItem.equals(source)) {
            loadFromBackup();
        } else if (reloadItem.equals(source)) {
            reload(DIARY_FILE);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println(e);
        Thread t = new Thread(() -> saveAndExit(DIARY_FILE));
        t.setName("Close Save Thread");
        t.start();
    }

    @Override
    public void windowClosed(WindowEvent e) { }

    @Override
    public void windowIconified(WindowEvent e) { }

    @Override
    public void windowDeiconified(WindowEvent e) { }

    @Override
    public void windowActivated(WindowEvent e) { }

    @Override
    public void windowDeactivated(WindowEvent e) { }
}
