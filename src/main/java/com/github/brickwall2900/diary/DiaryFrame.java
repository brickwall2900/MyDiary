package com.github.brickwall2900.diary;

import com.github.brickwall2900.diary.dialogs.*;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.github.brickwall2900.diary.DiarySetup.applyConfiguration;
import static com.github.brickwall2900.diary.DiarySetup.askForKey;
import static com.github.brickwall2900.diary.DiaryStore.currentEntry;
import static com.github.brickwall2900.diary.DiaryStore.load;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.awt.event.KeyEvent.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.*;
import static javax.swing.SwingUtilities.updateComponentTreeUI;

public class DiaryFrame extends JFrame implements ActionListener, WindowListener {
    public static final String TITLE = text("title");
    public static final Image IMAGE_ICON;
    public static final ImageIcon ICON;

    static {
        try {
            ICON = new ImageIcon(IMAGE_ICON = ImageIO.read(Objects.requireNonNull(DiaryFrame.class.getResourceAsStream("/icon.png"), "Where the hell did the icon go?")));
        } catch (IOException e) {
            throw new DiaryException("Icon read error!", e);
        }
    }

    public JMenuBar menuBar;
        public JMenu fileMenu;
            public JMenuItem saveItem;
            public JMenuItem reloadItem;
            public JSeparator separator2;
            public JMenuItem backupItem;
            public JMenuItem loadFromBackupItem;
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
    public DiaryLoadDialog loadDialog;
    public DiaryAboutDialog aboutDialog;
    public DiaryPasswordDialog passwordDialog;

    public JFileChooser fileChooser;

    public DiaryFrame() {
        buildMenuBar();

        htmlPanel = new XHTMLPanel();
        loadToHelpPage();
        scrollPane = new FSScrollPane(htmlPanel);
        getContentPane().add(scrollPane);

        editor = new DiaryMarkdownEditor(this);
        newEntryDialog = new DiaryNewEntryDialog(this);
        jumpToEntryDialog = new DiaryJumpToEntryDialog(this);
        configurationDialog = new DiaryConfigurationDialog(this);
        loadDialog = new DiaryLoadDialog(this);
        aboutDialog = new DiaryAboutDialog(this);
        passwordDialog = new DiaryPasswordDialog(this);

        fileChooser = new JFileChooser();

        addWindowListener(this);

        setIconImage(IMAGE_ICON);
        setTitle(TITLE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
    }

    private void loadToHelpPage() {
        try {
            htmlPanel.setDocument(new ByteArrayInputStream(DiaryMarkdownToHTML.wrapHTMLIntoActualDocument(text("html.place.help")).getBytes(Charset.defaultCharset())), null);
        } catch (Exception e) {
            throw new DiaryException(text("error.startPage"), e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Menu Bar Instantiation">
    private void buildMenuBar() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu(text("menu.file"));
        entriesMenu = new JMenu(text("menu.entries"));
        aboutMenu = new JMenu(text("menu.about"));

        buildFileMenu();
        buildAboutMenu();
        buildEntriesMenu();

        buildMnemonics();

        addAllMenuElements();

        setJMenuBar(menuBar);
    }

    private void buildFileMenu() {
        saveItem = new JMenuItem(text("menu.file.save"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_DOWN_MASK));
        saveItem.addActionListener(this);

        reloadItem = new JMenuItem(text("menu.file.reload"));
        reloadItem.setAccelerator(KeyStroke.getKeyStroke(VK_R, CTRL_DOWN_MASK));
        reloadItem.addActionListener(this);

        separator2 = new JSeparator();

        backupItem = new JMenuItem(text("menu.file.backup"));
        backupItem.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_DOWN_MASK | SHIFT_DOWN_MASK));
        backupItem.addActionListener(this);

        loadFromBackupItem = new JMenuItem(text("menu.file.loadBackup"));
        loadFromBackupItem.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_DOWN_MASK | SHIFT_DOWN_MASK));
        loadFromBackupItem.addActionListener(this);
    }

    private void buildAboutMenu() {
        preferencesMenu = new JMenuItem(text("menu.about.preferences"));
        preferencesMenu.setAccelerator(KeyStroke.getKeyStroke(VK_ENTER, CTRL_DOWN_MASK));
        preferencesMenu.addActionListener(this);

        aboutItem = new JMenuItem(text("menu.about.item"));
        aboutItem.addActionListener(this);
    }

    private void buildEntriesMenu() {
        newEntryItem = new JMenuItem(text("menu.entries.new"));
        newEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_N, CTRL_DOWN_MASK));
        newEntryItem.addActionListener(this);

        editEntryItem = new JMenuItem(text("menu.entries.edit"));
        editEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_E, CTRL_DOWN_MASK));
        editEntryItem.addActionListener(this);

        jumpToEntryItem = new JMenuItem(text("menu.entries.jump"));
        jumpToEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_F, CTRL_DOWN_MASK));
        jumpToEntryItem.addActionListener(this);

        removeEntryItem = new JMenuItem(text("menu.entries.remove"));
        removeEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_DELETE, 0));
        removeEntryItem.addActionListener(this);

        separator1 = new JSeparator();

        nextEntryItem = new JMenuItem(text("menu.entries.next"));
        nextEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_RIGHT, 0));
        nextEntryItem.addActionListener(this);

        prevEntryItem = new JMenuItem(text("menu.entries.previous"));
        prevEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_LEFT, 0));
        prevEntryItem.addActionListener(this);
    }

    private void buildMnemonics() {
        // menu mnemonics
        fileMenu.setMnemonic(VK_F);
        entriesMenu.setMnemonic(VK_E);
        aboutMenu.setMnemonic(VK_A);

        // mnemonics
        saveItem.setMnemonic(VK_S);
        reloadItem.setMnemonic(VK_R);
        backupItem.setMnemonic(VK_B);
        loadFromBackupItem.setMnemonic(VK_L);

        newEntryItem.setMnemonic(VK_N);
        editEntryItem.setMnemonic(VK_E);
        jumpToEntryItem.setMnemonic(VK_J);
        prevEntryItem.setMnemonic(VK_P);
        nextEntryItem.setMnemonic(VK_N);

        preferencesMenu.setMnemonic(VK_C);
        aboutItem.setMnemonic(VK_A);
    }

    private void addAllMenuElements() {
        // throw them into the menu
        fileMenu.add(saveItem);
        fileMenu.add(reloadItem);
        fileMenu.add(separator2);
        fileMenu.add(backupItem);
        fileMenu.add(loadFromBackupItem);

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
    }
    // </editor-fold>

    public void addEntry() {
        DiaryStore.DiaryEntry entry = newEntryDialog.askNewEntry();
        if (entry != null) {
            boolean overwrite = !DiaryStore.ENTRIES.containsKey(entry);
            if (!overwrite) {
                int confirmOverwrite = showConfirmDialog(this, text("dialog.overwrite"), TITLE, YES_NO_OPTION, WARNING_MESSAGE);
                overwrite = confirmOverwrite == YES_OPTION;
            }
            if (overwrite) {
                String template = DiaryStore.CONFIGURATION.template;
                LocalDateTime dateTime = LocalDateTime.of(entry.date, entry.time);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DiaryStore.CONFIGURATION.timeFormat);
                template = template.replace("{name}", entry.name).replace("{time}", formatter.format(dateTime));
                DiaryStore.ENTRIES.put(entry, template);
                updatePanelWithEntry(entry);
                currentEntry = entry;
            }
        }
    }

    public void removeCurrentEntry() {
        if (currentEntry != null) {
            String[] options = new String[] {text("dialog.yes"), text("dialog.no")};
            boolean confirmDelete = showOptionDialog(this, text("dialog.removeEntry"), TITLE, DEFAULT_OPTION, WARNING_MESSAGE, null, options, options[1]) == 0;
            if (confirmDelete) {
                DiaryStore.ENTRIES.put(currentEntry, "");
                DiaryStore.ENTRIES.remove(currentEntry);
                try {
                    htmlPanel.setDocument(new ByteArrayInputStream(DiaryMarkdownToHTML.wrapHTMLIntoActualDocument(text("html.place.removed")).getBytes(Charset.defaultCharset())), null);
                } catch (Exception e) {
                    throw new DiaryException(text("error.removeEntry"), e);
                }
                currentEntry = null;
            }
        }
    }

    public void updatePanelWithEntry(DiaryStore.DiaryEntry entry) {
        if (entry != null) {
            String content = DiaryStore.ENTRIES.get(entry);
            try {
                htmlPanel.setDocument(new ByteArrayInputStream(DiaryMarkdownToHTML.wrapHTMLIntoActualDocument(DiaryMarkdownToHTML.getHTMLFromMarkdown(content)).getBytes(Charset.defaultCharset())), null);
            } catch (Exception e) {
                throw new DiaryException(text("error.updateError"), e);
            }
        }
    }

    public void updatePanelDirect(InputStream inputStream) {
        try {
            htmlPanel.setDocument(inputStream, null);
        } catch (Exception e) {
            throw new DiaryException(text("error.updateDirectError"), e);
        }
    }

    public void openEditorOnEntry(DiaryStore.DiaryEntry entry) {
        if (entry != null) {
            String content = DiaryStore.ENTRIES.get(entry);
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
            List<DiaryStore.DiaryEntry> entryList = DiaryStore.getSortedEntryList();
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
        updateComponentTreeUI(aboutDialog);
        updateComponentTreeUI(passwordDialog);
        updateComponentTreeUI(Main.INSTANCE.errorMessage);
        configurationDialog.updateUI();
    }

    public void backup() {
        int val = fileChooser.showSaveDialog(this);
        if (val == APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (showConfirmDialog(this, text("dialog.backup", f), TITLE, YES_NO_OPTION, QUESTION_MESSAGE) == YES_OPTION) {
                try {
                    for (int i = 0; i < 10 || f.createNewFile(); i++) ;
                } catch (IOException e) {
                    throw new DiaryException(text("error.backup"), e);
                }
                Thread t = new Thread(() -> DiaryStore.save(f));
                t.setName("Save Thread");
                t.start();
            }
        }
    }

    public void reload(File f) {
        String message = DiaryStore.DIARY_FILE.equals(f) ? text("dialog.reload") : text("dialog.reloadBackup");
        if (showConfirmDialog(this, message, TITLE, YES_NO_OPTION, WARNING_MESSAGE) == YES_OPTION) {
            try {
                askForKey();
                load(f);
                applyConfiguration(this, DiaryStore.CONFIGURATION);
                DiaryMarkdownToHTML.MD_TO_HTML_CACHE.clear();
                currentEntry = null;
                loadToHelpPage();
            } catch (Exception e) {
                e.printStackTrace();
                showMessageDialog(this, text("dialog.reloadError", e.getMessage()), TITLE, ERROR_MESSAGE);
                Main.INSTANCE.errorMessage.showErrorMessage(e);
                if (loadDialog.isShowing()) {
                    loadDialog.closeLoadDialog();
                }
            }
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
            Thread t = new Thread(() -> DiaryStore.save(DiaryStore.DIARY_FILE));
            t.setName("Save Thread");
            t.start();
        } else if (removeEntryItem.equals(source)) {
            removeCurrentEntry();
        } else if (backupItem.equals(source)) {
            backup();
        } else if (loadFromBackupItem.equals(source)) {
            loadFromBackup();
        } else if (reloadItem.equals(source)) {
            reload(DiaryStore.DIARY_FILE);
        } else if (aboutItem.equals(source)) {
            aboutDialog.showAboutScreen();
        }
    }

    @Override
    public void windowOpened(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) {
        Thread t = new Thread(() -> DiaryStore.saveAndExit(DiaryStore.DIARY_FILE));
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
