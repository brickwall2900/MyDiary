package com.github.brickwall2900.diary.dialogs;

import com.github.brickwall2900.diary.DiaryStore;
import org.httprpc.sierra.DatePicker;
import org.httprpc.sierra.TextPane;
import org.httprpc.sierra.TimePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;

import static com.github.brickwall2900.diary.DiaryFrame.IMAGE_ICON;
import static com.github.brickwall2900.diary.utils.TranslatableText.text;
import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static org.httprpc.sierra.UIBuilder.*;
import static org.httprpc.sierra.VerticalAlignment.BOTTOM;

public class DiaryNewEntryDialog extends JDialog {
    public static final String TITLE = text("newEntry.title");

    public TextPane nameLabel;
    public JTextField nameField;

    public TextPane dateLabel;
    public DatePicker datePicker;

    public TextPane timeLabel;
    public TimePicker timePicker;

    public JButton createButton;
    public JButton cancelButton;

    public DiaryStore.DiaryEntry lastEntry;

    private boolean canceled = true;

    public DiaryNewEntryDialog(JFrame parent) {
        super(parent);
        buildContentPane();

        timePicker.setPopupVerticalAlignment(BOTTOM);

        clearFields();

        createButton.addActionListener(e -> create());
        cancelButton.addActionListener(e -> dispose());

        nameField.addActionListener(e -> create());
        nameField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == VK_ESCAPE) dispose();
            }
        });

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
                        cell(nameLabel = new TextPane(text("newEntry.name.label"))),
                        cell(nameField = new JTextField()).weightBy(1)),
                row(6,
                        cell(dateLabel = new TextPane(text("newEntry.date.label"))),
                        cell(datePicker = new DatePicker()).weightBy(1)),
                row(6,
                        cell(timeLabel = new TextPane(text("newEntry.time.label"))),
                        cell(timePicker = new TimePicker()).weightBy(1)),
                glue(),
                row(4,
                        glue(),
                        cell(createButton = new JButton(text("newEntry.createButton"))),
                        cell(cancelButton = new JButton(text("dialog.cancel"))))
        ).with(contentPane -> contentPane.setBorder(new EmptyBorder(8, 8, 8, 8))).getComponent());
    }

    private void clearFields() {
        nameField.setText("");
        datePicker.setDate(LocalDate.now());
        timePicker.setTime(LocalTime.now());
    }

    private void create() {
        canceled = false;
        dispose();
    }

    public DiaryStore.DiaryEntry askNewEntry() {
        clearFields();
        setVisible(true);
        DiaryStore.DiaryEntry entry = null;
        if (!canceled) {
            String name = nameField.getText();
            LocalDate date = datePicker.getDate();
            LocalTime time = timePicker.getTime();
            entry = new DiaryStore.DiaryEntry(name, date, time);
        }
        canceled = true;
        return entry;
    }
}
