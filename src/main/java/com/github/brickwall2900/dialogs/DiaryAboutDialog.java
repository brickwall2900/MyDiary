package com.github.brickwall2900.dialogs;

import com.github.brickwall2900.DiaryException;
import com.github.brickwall2900.DiaryFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.IOException;
import java.util.Objects;

import static com.github.brickwall2900.DiaryFrame.ICON;
import static com.github.brickwall2900.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.DiaryStore.FILE_VERSION;
import static com.github.brickwall2900.DiaryStore.USERNAME;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryAboutDialog extends JDialog {
    public static final String TITLE = "About";

    public JLabel icon;
    public JEditorPane aboutArea;

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
                        glue(),
                        cell(closeButton = new JButton("Close")))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    public void showAboutScreen() {
        aboutArea.setContentType("text/html");
        aboutArea.setText("""
                <h1>The Diary</h1>
                
                <p>Made <strike>with no effort</strike> by <em>brickwall2900</em></p>
                 
                <p>Current File Version: %d</p>
                
                <p>... and you are %s!</p>
                
                <em><strong>(WARNING: THIS APPLICATION DOES NOT GUARANTEE 100%% SECURITY WITH ALL YOUR PRIVATE SECRETS!)</strong></em>
                """.formatted(FILE_VERSION, USERNAME));
        setVisible(true);
    }
}
