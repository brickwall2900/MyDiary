package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.DiaryFrame;
import org.httprpc.sierra.ActivityIndicator;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import static com.github.brickwall2900.diary.DiaryFrame.IMAGE_ICON;
import static java.awt.Dialog.ModalityType.MODELESS;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryLoadDialog extends JDialog {
    private static final String TITLE = "Please wait...";
    public DiaryFrame parent;

    public JProgressBar progressBar;
    public TextPane taskDoing;
    public ActivityIndicator loadingIcon;

    public DiaryLoadDialog(DiaryFrame parent) {
        super(parent);
        this.parent = parent;
        buildContentPane();

        setIconImage(IMAGE_ICON);
        setModal(false);
        setModalityType(MODELESS);
        setTitle(TITLE);
        setSize(320, 100);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(parent);
    }

    private void buildContentPane() {
        setContentPane(row(6,
                cell(loadingIcon = new ActivityIndicator(48)),
                column(6,
                        cell(taskDoing = new TextPane(TITLE)),
                        cell(progressBar = new JProgressBar())).weightBy(1)
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    public void openLoadDialog(String task, int progress) {
        loadingIcon.start();
        setTaskName(task);
        setProgress(progress);
        setVisible(true);
    }

    public void openLoadDialog() {
        loadingIcon.start();
        setVisible(true);
    }

    public void openLoadDialog(boolean open) {
        if (open) openLoadDialog();
        else closeLoadDialog();
    }

    public void setTaskName(String task) {
        if (task != null) {
            setTitle(task);
            taskDoing.setText(task);
        }
    }

    public void setProgress(int progress) {
        if (progress < 0) progressBar.setIndeterminate(true);
        else {
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);
        }
    }

    public void closeLoadDialog() {
        loadingIcon.stop();
        dispose();
    }
}
