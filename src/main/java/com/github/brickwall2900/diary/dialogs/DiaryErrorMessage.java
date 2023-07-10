package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.utils.TextAreaOutputStream;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.io.PrintWriter;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryErrorMessage extends JDialog {
    public static final String TITLE = "Error!";

    public TextPane editLabel;
    public JScrollPane textScrollPane;
    public JTextArea textArea;

    public JButton continueButton;
    public JButton exitButton;

    public DiaryErrorMessage() {
        buildContentPane();

        textArea.setEditable(false);
        exitButton.addActionListener(e -> System.exit(1));
        continueButton.addActionListener(e -> dispose());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(640, 480);
        setLocationRelativeTo(null);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                cell(editLabel = new TextPane("An error has occurred!")),
                cell(textScrollPane = new JScrollPane(textArea = new JTextArea())).weightBy(1),
                row(4,
                        glue(),
                        cell(continueButton = new JButton("Continue anyway")),
                        cell(exitButton = new JButton("Exit")))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    public void showErrorMessage(Throwable t) {
        try (TextAreaOutputStream outputStream = new TextAreaOutputStream(textArea);
             PrintWriter printWriter = new PrintWriter(outputStream, true)) {
            t.printStackTrace(printWriter);
            setVisible(true);
        }
    }
}
