package com.github.brickwall2900.dialogs;

import com.github.brickwall2900.DiaryFrame;
import com.github.brickwall2900.DiaryStore;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static com.github.brickwall2900.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.DiaryStore.ENTRIES;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryMarkdownEditor extends JDialog {
    public static final String TITLE = "Editor";

    public TextPane editLabel;
    public RTextScrollPane scrollPane;
    public RSyntaxTextArea textArea;

    public JButton editButton;

    private DiaryFrame parent;
    private DiaryStore.DiaryEntry entry;

    public DiaryMarkdownEditor(DiaryFrame parent) {
        super(parent);
        this.parent = parent;

        buildContentPane();

        textArea.setCodeFoldingEnabled(true);
        textArea.setSyntaxEditingStyle(SYNTAX_STYLE_MARKDOWN);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == VK_ESCAPE) {
                    saveAndUpdate();
                    dispose();
                }
            }
        });

        editButton.addActionListener(e -> {
            saveAndUpdate();
            dispose();
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                saveAndUpdate();
            }
        });

        setIconImage(IMAGE_ICON);
        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(640, 480);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                cell(editLabel = new TextPane("Markdown Editor: ")),
                cell(scrollPane = new RTextScrollPane(textArea = new RSyntaxTextArea())).weightBy(1),
                row(4,
                        glue(),
                        cell(editButton = new JButton("Edit")))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    public void setEntryContext(DiaryStore.DiaryEntry entry) {
        this.entry = entry;
    }

    public void changeTheme(boolean dark) {
        String themeToLoad = "/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml";
        if (dark) {
            themeToLoad = "/org/fife/ui/rsyntaxtextarea/themes/dark.xml";
        }
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(themeToLoad));
            theme.apply(textArea);
        } catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }
    }

    public void saveAndUpdate() {
        ENTRIES.put(entry, textArea.getText());
        parent.updatePanelWithEntry(entry);
    }
}
