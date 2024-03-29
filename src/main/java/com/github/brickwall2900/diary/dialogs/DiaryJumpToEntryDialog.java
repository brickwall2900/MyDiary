package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.DiaryStore;
import org.httprpc.sierra.TextPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

import static com.github.brickwall2900.diary.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.diary.DiaryStore.getSortedEntryList;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static org.httprpc.sierra.UIBuilder.*;

public class DiaryJumpToEntryDialog extends JDialog {
    public static final String TITLE = text("jump.title");

    public TextPane nameLabel;
    public JList<DiaryStore.DiaryEntry> entries;

    public JCheckBox reverseOrderCheckBox;
    public JCheckBox showHiddenCheckBox;

    public JButton jumpButton;
    public JButton cancelButton;

    public DefaultListModel<DiaryStore.DiaryEntry> listModel;

    private boolean canceled = true;

    public DiaryJumpToEntryDialog(JFrame parent) {
        super(parent);
        buildContentPane();

        jumpButton.addActionListener(e -> create());
        cancelButton.addActionListener(e -> dispose());

        entries.setModel(listModel = new DefaultListModel<>());
        reverseOrderCheckBox.addActionListener(e -> updateFields());
        showHiddenCheckBox.addActionListener(e -> updateFields());

        entries.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == VK_ENTER) create();
                else if (e.getKeyCode() == VK_ESCAPE) dispose();
            }
        });

        setIconImage(IMAGE_ICON);
        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(480, 640);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                cell(nameLabel = new TextPane(text("jump.header"))),
                cell(entries = new JList<>()).weightBy(1),
                row(4,
                        cell(reverseOrderCheckBox = new JCheckBox(text("jump.newestFirst"), true)),
                        cell(showHiddenCheckBox = new JCheckBox(text("jump.showHidden"))),
                        glue(),
                        cell(jumpButton = new JButton(text("jump.jumpButton"))),
                        cell(cancelButton = new JButton(text("dialog.cancel"))))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    private void create() {
        canceled = false;
        dispose();
    }

    private void updateFields() {
        boolean oldestFirst = reverseOrderCheckBox.isSelected();
        boolean showHidden = showHiddenCheckBox.isSelected();
        listModel.removeAllElements();
        List<DiaryStore.DiaryEntry> entryList = getSortedEntryList(showHidden);
        if (oldestFirst) {
            Collections.reverse(entryList);
        }
        listModel.addAll(entryList);
    }

    public DiaryStore.DiaryEntry askJumpToEntry(DiaryStore.DiaryEntry currentEntry) {
        updateFields();
        if (currentEntry != null) {
            entries.setSelectedValue(currentEntry, true);
        }
        setVisible(true);
        DiaryStore.DiaryEntry entry = null;
        if (!canceled) {
            entry = entries.getSelectedValue();
        }
        canceled = true;
        return entry;
    }
}
