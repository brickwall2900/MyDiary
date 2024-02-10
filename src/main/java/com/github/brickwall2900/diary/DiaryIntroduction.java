package com.github.brickwall2900.diary;

import com.github.brickwall2900.diary.utils.MapBuilder;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static com.github.brickwall2900.diary.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.diary.utils.TranslatableText.intro;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryIntroduction extends JFrame {
    public static final String TITLE = text("title");

    public TextPane headerLabel;
    public JScrollPane scrollPane;
    public JList<String> fileList;
    public JButton newButton, openButton, deleteButton;
    public JButton openInExplorerButton;

    public DiaryFrame frame;

    public DiaryIntroduction(DiaryFrame frame) {
        this.frame = frame;

        buildContentPane();
        fileList.setModel(new DefaultListModel<>());

        openButton.setEnabled(false);
        deleteButton.setEnabled(false);

        newButton.addActionListener(e -> newFile());
        openButton.addActionListener(this::onOpen);
        fileList.addListSelectionListener(this::onFileSelect);
        openInExplorerButton.addActionListener(this::onOpenInExplorer);

        populateFileList();

        setIconImage(IMAGE_ICON);
        setTitle(TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 360);
        setLocationRelativeTo(null);
    }

    private static void throwIllegalArgument(String message) {
        showMessageDialog(null, message, "", ERROR_MESSAGE);
        throw new IllegalArgumentException(message);
    }

    public void newFile() {
        DefaultListModel<String> model = (DefaultListModel<String>) fileList.getModel();
        String input = JOptionPane.showInputDialog(this, text("introduction.chooseName"), TITLE, JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.isBlank()) {
            model.addElement(input);
        } else {
            throwIllegalArgument(text("password.error.emptyInput"));
        }
    }

    public static void openFileInExplorer(File file) {
        Desktop.getDesktop().browseFileDirectory(file);
    }

    private void populateFileList() {
        String[] names = DiaryStore.HOME.list();
        if (names != null) {
            DefaultListModel<String> model = (DefaultListModel<String>) fileList.getModel();
            model.addAll(List.of(names));
        }
    }

    private void onOpen(ActionEvent e) {
        String selected = fileList.getSelectedValue();
        if (selected != null) {
            File translated = new File(DiaryStore.HOME, selected);
            if (translated.exists()) {
                DiaryStore.currentFile = translated;
                DiarySetup.askForKey();
                DiaryStore.load(translated);
                DiarySetup.applyConfiguration(frame, DiaryStore.CONFIGURATION);
            } else {
                DiarySetup.setup();
                DiaryStore.createDiary(translated);
                DiaryStore.save(translated);
                DiaryStore.currentFile = translated;
            }
            frame.setVisible(true);
            dispose();
        }
    }

    private void onOpenInExplorer(ActionEvent e) {
        String selected = fileList.getSelectedValue();
        if (selected != null) {
            File translated = new File(DiaryStore.HOME, selected);
            if (translated.exists()) {
                openFileInExplorer(translated);
            } else {
                openFileInExplorer(DiaryStore.HOME);
            }
        } else {
            openFileInExplorer(DiaryStore.HOME);
        }
    }

    private void onFileSelect(ListSelectionEvent e) {
        String selected = fileList.getSelectedValue();
        boolean shouldBeEnabled = selected != null;
        openButton.setEnabled(shouldBeEnabled);
        deleteButton.setEnabled(shouldBeEnabled);

    }

    public String randomIntro() {
        return intro(MapBuilder.<String, Object>build()
                .put("user", System.getProperty("user.name"))
                .get());
    }

    @Override
    public void setVisible(boolean b) {
        headerLabel.setText(randomIntro());
        super.setVisible(b);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                cell(headerLabel = new TextPane(randomIntro())),
                cell(scrollPane = new JScrollPane(fileList = new JList<>())).weightBy(1),
                row(4,
                        cell(openInExplorerButton = new JButton(text("introduction.openInExplorer"))),
                        glue(),
                        cell(newButton = new JButton(text("introduction.new"))),
                        cell(openButton = new JButton(text("introduction.open"))),
                        cell(deleteButton = new JButton(text("introduction.delete"))))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }
}
