package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.DiaryFrame;
import com.github.brickwall2900.diary.DiaryStore;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static com.github.brickwall2900.diary.DiaryFrame.ICON;
import static com.github.brickwall2900.diary.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryAboutDialog extends JDialog {
    public static final String TITLE = text("about.title");

    public JLabel icon;
    public JEditorPane aboutArea;
    public JButton debugMode;

    public JButton closeButton;

    private DiaryFrame parent;

    public DiaryAboutDialog(DiaryFrame parent) {
        super(parent);
        this.parent = parent;

        buildContentPane();

        aboutArea.setEditable(false);
        aboutArea.setText("Allan please put an About screen here...");
        icon.setIcon(ICON);
        closeButton.addActionListener(e -> dispose());
        debugMode.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, text("about.debugMode.warn")) == JOptionPane.YES_OPTION) {
                parent.setDebugMode(true);
                debugMode.setEnabled(false);
            }
        });

        setIconImage(IMAGE_ICON);
        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(640, 270);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                row(4,
                        cell(icon = new JLabel("")),
                        cell(aboutArea = new JEditorPane()).weightBy(1)),
                glue(),
                row(4,
                        cell(debugMode = new JButton(text("about.debugMode"))),
                        glue(),
                        cell(closeButton = new JButton(text("dialog.close"))))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    public void showAboutScreen() {
        aboutArea.setContentType("text/html");
        aboutArea.setText(text("about.content", DiaryStore.FILE_VERSION, null));
        setVisible(true);
    }
}
