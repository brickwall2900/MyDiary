package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.DiaryFrame;
import com.github.brickwall2900.diary.DiaryStore;
import com.github.brickwall2900.diary.DiarySetup;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static javax.swing.SwingUtilities.updateComponentTreeUI;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryConfigurationDialog extends JDialog {
    public static final String TITLE = text("config.title");

    public TextPane darkModeLabel;
    public JCheckBox darkModeCheckBox;

    public TextPane timeFormattingLabel;
    public JTextField timeFormattingField;
    public JButton timeFormattingHelpButton;

    public TextPane templateEditLabel;
    public JButton templateEditButton;

    public JButton okButton;
    public JButton cancelButton;

    public DiaryFrame parent;
    public DiaryTemplateEditor templateEditor;

    private boolean canceled = true;

    public DiaryConfigurationDialog(DiaryFrame parent) {
        super(parent);
        this.parent = parent;

        buildContentPane();
        templateEditor = new DiaryTemplateEditor(parent);

        okButton.addActionListener(e -> { apply(); dispose(); });
        cancelButton.addActionListener(e -> dispose());

        templateEditButton.addActionListener(e -> templateEditor.setVisible(true));

        timeFormattingHelpButton.addActionListener(e -> {
            try {
                openLink(new URI("https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html"));
            } catch (URISyntaxException ex) {}
        });

        darkModeCheckBox.setToolTipText(text("config.darkMode.tooltip"));
        timeFormattingField.setToolTipText(text("config.timeFormat.tooltip"));
        templateEditButton.setToolTipText(text("config.template.tooltip"));

        setIconImage(DiaryFrame.IMAGE_ICON);
        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(320, 240);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                row(6,
                        cell(darkModeLabel = new TextPane(text("config.darkMode.label"))),
                        cell(darkModeCheckBox = new JCheckBox())),
                row(6,
                        cell(timeFormattingLabel = new TextPane(text("config.timeFormat.label"))),
                        cell(timeFormattingField = new JTextField()).weightBy(1),
                        cell(timeFormattingHelpButton = new JButton(text("dialog.question")))),
                row(6,
                        cell(templateEditLabel = new TextPane(text("config.template.label"))),
                        cell(templateEditButton = new JButton(text("config.template.edit")))),
                glue(),
                row(4,
                        glue(),
                        cell(okButton = new JButton(text("dialog.ok"))),
                        cell(cancelButton = new JButton(text("dialog.cancel"))))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    private void updateFields() {
        DiaryStore.DiaryConfiguration configuration = DiaryStore.CONFIGURATION;
        darkModeCheckBox.setSelected(configuration.darkMode);
        timeFormattingField.setText(configuration.timeFormat);
        templateEditor.textArea.setText(configuration.template);
    }

    private void apply() {
        canceled = false;
    }

    public void updateUI() {
        updateComponentTreeUI(this);
        updateComponentTreeUI(templateEditor);
    }

    public void editOptions() {
        updateFields();
        setVisible(true);
        if (!canceled) {
            DiaryStore.DiaryConfiguration configuration = DiaryStore.CONFIGURATION;
            configuration.darkMode = darkModeCheckBox.isSelected();
            configuration.timeFormat = timeFormattingField.getText();
            configuration.template = templateEditor.textArea.getText();
            DiarySetup.applyConfiguration(parent, configuration);
        }
        canceled = true;
    }

    public static void openLink(URI uri) {
        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
