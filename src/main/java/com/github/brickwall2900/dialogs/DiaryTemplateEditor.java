package com.github.brickwall2900.dialogs;

import com.github.brickwall2900.DiaryFrame;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static com.github.brickwall2900.DiaryFrame.IMAGE_ICON;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryTemplateEditor extends JDialog {
    public static final String TITLE = "Template Editor";

    public TextPane editLabel;
    public JTextArea textArea;

    public JButton editButton;

    private DiaryFrame parent;

    public DiaryTemplateEditor(DiaryFrame parent) {
        super(parent);
        this.parent = parent;

        buildContentPane();

        editButton.addActionListener(e -> dispose());

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
                cell(editLabel = new TextPane("Template Editor: {name} resolves to the name inputted, and {time} resolves to the formatted time inputted.")),
                cell(textArea = new JTextArea()).weightBy(1),
                row(4,
                        glue(),
                        cell(editButton = new JButton("Edit")))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }
}
