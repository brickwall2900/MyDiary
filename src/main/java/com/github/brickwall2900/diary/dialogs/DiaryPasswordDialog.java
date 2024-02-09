package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.DiaryFrame;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static com.github.brickwall2900.diary.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryPasswordDialog extends JDialog {
    public static final String TITLE = text("password.title");

    public TextPane passwordLabel;
    public JPasswordField passwordField;

    public JButton enterButton;

    private DiaryFrame parent;
    private boolean canceled = true;

    public DiaryPasswordDialog(DiaryFrame parent) {
        super(parent);
        this.parent = parent;

        buildContentPane();

        enterButton.addActionListener(e -> success());
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == VK_ENTER) success();
                else if (e.getKeyCode() == VK_ESCAPE) dispose();
            }
        });

        setIconImage(IMAGE_ICON);
        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(640, 120);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void success() {
        canceled = false;
        dispose();
    }

    private void buildContentPane() {
        setContentPane(column(4,
                cell(passwordLabel = new TextPane(text("password.header"))),
                cell(passwordField = new JPasswordField()).weightBy(1),
                row(4,
                        glue(),
                        cell(enterButton = new JButton(text("password.enter"))))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    public char[] askPassword() {
        passwordField.setText("");
        setVisible(true);
        char[] pwd = null;
        if (!canceled) {
            pwd = passwordField.getPassword();
        }
        canceled = true;
        return pwd;
    }
}
