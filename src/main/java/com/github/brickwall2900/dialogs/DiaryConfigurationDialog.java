package com.github.brickwall2900.dialogs;

import com.github.brickwall2900.DiaryFrame;
import com.github.brickwall2900.DiaryStore;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static com.github.brickwall2900.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.DiarySetup.applyConfiguration;
import static com.github.brickwall2900.DiaryStore.CONFIGURATION;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static javax.swing.SwingUtilities.updateComponentTreeUI;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryConfigurationDialog extends JDialog {
    public static final String TITLE = "Configuration";

    public TextPane darkModeLabel;
    public JCheckBox darkModeCheckBox;

    public TextPane timeFormattingLabel;
    public JTextField timeFormattingField;

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

        darkModeCheckBox.setToolTipText("Enables dark mode on UI or not.");
        timeFormattingField.setToolTipText("The formatting used for creating new entries in the diary. (https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)");
        templateEditButton.setToolTipText("Edits the template used for creating new entries in the diary. {name} resolves to the name inputted, and {time} resolves to the formatted time inputted.");

        setIconImage(IMAGE_ICON);
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
                        cell(darkModeLabel = new TextPane("Dark Mode:")),
                        cell(darkModeCheckBox = new JCheckBox())),
                row(6,
                        cell(timeFormattingLabel = new TextPane("Time Formatting:")),
                        cell(timeFormattingField = new JTextField()).weightBy(1)),
                row(6,
                        cell(templateEditLabel = new TextPane("Template:")),
                        cell(templateEditButton = new JButton("Edit..."))),
                glue(),
                row(4,
                        glue(),
                        cell(okButton = new JButton("OK")),
                        cell(cancelButton = new JButton("Cancel")))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    private void updateFields() {
        DiaryStore.DiaryConfiguration configuration = CONFIGURATION;
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
            DiaryStore.DiaryConfiguration configuration = CONFIGURATION;
            configuration.darkMode = darkModeCheckBox.isSelected();
            configuration.timeFormat = timeFormattingField.getText();
            configuration.template = templateEditor.textArea.getText();
            applyConfiguration(parent, configuration);
        }
        canceled = true;
    }
}
