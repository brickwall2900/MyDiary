package com.github.brickwall2900.diary;

import com.github.brickwall2900.diary.dialogs.*;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import static com.github.brickwall2900.diary.DiarySetup.applyConfiguration;
import static com.github.brickwall2900.diary.DiarySetup.askForKey;
import static com.github.brickwall2900.diary.DiaryStore.*;
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
            public JSeparator separator3;
            public JMenuItem closeItem;
        public JMenu entriesMenu;
            public JMenuItem newEntryItem;
            public JMenuItem editEntryItem;
            public JMenuItem jumpToEntryItem;
            public JCheckBoxMenuItem hiddenEntryItem;
            public JMenuItem removeEntryItem;
            public JSeparator separator1;
            public JMenuItem nextEntryItem, prevEntryItem;
        public JMenu aboutMenu;
            public JMenuItem preferencesMenu;
            public JMenuItem aboutItem;

    public JScrollPane scrollPane;
        public JTextPane htmlPanel;

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

        htmlPanel = new JTextPane();
        htmlPanel.setEditorKit(new HTMLEditorKit());
        htmlPanel.setEditable(false);
        scrollPane = new JScrollPane(htmlPanel);
        loadToHelpPage();
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

    public void clearHTMLPanelContent() {
        htmlPanel.setDocument(htmlPanel.getEditorKit().createDefaultDocument());
    }

    public void loadHTMLContent(InputStream is) {
        clearHTMLPanelContent();
        try {
            htmlPanel.getEditorKit().read(is, htmlPanel.getStyledDocument(), 0);
        } catch (IOException | BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadToHelpPage() {
        try {
            loadHTMLContent(new ByteArrayInputStream(DiaryMarkdownToHTML.wrapHTMLIntoActualDocument(text("html.place.help")).getBytes(Charset.defaultCharset())));
        } catch (Exception e) {
            throw new DiaryException(text("error.startPage"), e);
        }

        hiddenEntryItem.setSelected(false);
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

        separator3 = new JSeparator();

        closeItem = new JMenuItem(text("menu.file.close"));
        closeItem.setAccelerator(KeyStroke.getKeyStroke(VK_W, CTRL_DOWN_MASK));
        closeItem.addActionListener(this);
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

        hiddenEntryItem = new JCheckBoxMenuItem(text("menu.entries.hidden"));
        hiddenEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_H, CTRL_DOWN_MASK));
        hiddenEntryItem.addActionListener(this);

        removeEntryItem = new JMenuItem(text("menu.entries.remove"));
        removeEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_DELETE, 0));
        removeEntryItem.addActionListener(this);

        separator1 = new JSeparator();

        nextEntryItem = new JMenuItem(text("menu.entries.next"));
        nextEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_PERIOD, 0));
        nextEntryItem.addActionListener(this);

        prevEntryItem = new JMenuItem(text("menu.entries.previous"));
        prevEntryItem.setAccelerator(KeyStroke.getKeyStroke(VK_COMMA, 0));
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
        closeItem.setMnemonic(VK_C);

        newEntryItem.setMnemonic(VK_N);
        editEntryItem.setMnemonic(VK_E);
        jumpToEntryItem.setMnemonic(VK_J);
        hiddenEntryItem.setMnemonic(VK_H);
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
        fileMenu.add(separator3);
        fileMenu.add(closeItem);

        entriesMenu.add(newEntryItem);
        entriesMenu.add(editEntryItem);
        entriesMenu.add(jumpToEntryItem);
        entriesMenu.add(hiddenEntryItem);
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
                    loadHTMLContent(new ByteArrayInputStream(DiaryMarkdownToHTML.wrapHTMLIntoActualDocument(text("html.place.removed")).getBytes(Charset.defaultCharset())));
                } catch (Exception e) {
                    throw new DiaryException(text("error.removeEntry"), e);
                }
                currentEntry = null;
            }
        }
    }

    public void toggleCurrentEntryVisibility() {
        if (currentEntry != null) {
            boolean hidden = currentEntry.hidden;
            hidden = !hidden;
            currentEntry.hidden = hidden;
            hiddenEntryItem.setSelected(hidden);
        } else {
            hiddenEntryItem.setSelected(false);
        }
    }

    public void updatePanelWithEntry(DiaryStore.DiaryEntry entry) {
        if (entry != null) {
            String content = DiaryStore.ENTRIES.get(entry);
            try {
                loadHTMLContent(new ByteArrayInputStream(DiaryMarkdownToHTML.wrapHTMLIntoActualDocument(DiaryMarkdownToHTML.getHTMLFromMarkdown(content)).getBytes(Charset.defaultCharset())));
            } catch (Exception e) {
                throw new DiaryException(text("error.updateError"), e);
            }
            hiddenEntryItem.setSelected(entry.hidden);
        }
    }

    public void updatePanelWithCurrentEntry() {
        if (currentEntry != null) {
            updatePanelWithEntry(currentEntry);
        } else {
            loadToHelpPage();
        }
    }

    public void updatePanelDirect(InputStream inputStream) {
        try {
            loadHTMLContent(inputStream);
            hiddenEntryItem.setSelected(currentEntry != null && currentEntry.hidden);
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
            List<DiaryStore.DiaryEntry> entryList = DiaryStore.getSortedEntryList(false);
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
            Path f = fileChooser.getSelectedFile().toPath();
            if (showConfirmDialog(this, text("dialog.backup", f), TITLE, YES_NO_OPTION, QUESTION_MESSAGE) == YES_OPTION) {
                try {
                    Files.createFile(f);
                } catch (IOException e) {
                    throw new DiaryException(text("error.backup"), e);
                }
                wrapSaveThenCallLater(f, null);
            }
        }
    }

    public void reload(Path f) {
        String message = currentFile.equals(f) ? text("dialog.reload") : text("dialog.reloadBackup");
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
            Path f = fileChooser.getSelectedFile().toPath();
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
            wrapSaveThenCallLater(currentFile, null);
        } else if (removeEntryItem.equals(source)) {
            removeCurrentEntry();
        } else if (backupItem.equals(source)) {
            backup();
        } else if (loadFromBackupItem.equals(source)) {
            loadFromBackup();
        } else if (reloadItem.equals(source)) {
            reload(currentFile);
        } else if (aboutItem.equals(source)) {
            aboutDialog.showAboutScreen();
        } else if (closeItem.equals(source)) {
            dispose();
            wrapSaveThenCallLater(currentFile, this::onClose);
        } else if (hiddenEntryItem.equals(source)) {
            toggleCurrentEntryVisibility();
        }
    }

    private void onClose() {
        DiaryIntroduction introduction = Main.INSTANCE.introduction;
        DiarySetup.destroyKey();
        currentFile = null;
        currentName = null;
        DiaryStore.destroyStore();
        DiarySetup.applyConfiguration(this, CONFIGURATION);
        loadToHelpPage();
        introduction.setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) {
        wrapSaveThenCallLater(currentFile, this::onClose);
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
