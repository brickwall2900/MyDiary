package com.github.brickwall2900;

import org.httprpc.sierra.DatePicker;
import org.httprpc.sierra.TextPane;
import org.httprpc.sierra.TimePicker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Executors;

import static java.awt.Dialog.ModalityType.APPLICATION_MODAL;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static org.httprpc.sierra.HorizontalAlignment.LEADING;
import static org.httprpc.sierra.UIBuilder.*;
import static org.httprpc.sierra.VerticalAlignment.BOTTOM;

public class DiaryNewEntryDialog extends JDialog {
    public static final String TITLE = "Create a new Entry";

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

        setModal(true);
        setModalityType(APPLICATION_MODAL);
        setTitle(TITLE);
        setSize(320, 240);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
    }

    private void buildContentPane() {
        setContentPane(column(4,
                row(6,
                        cell(nameLabel = new TextPane("Name:")),
                        cell(nameField = new JTextField()).weightBy(1)),
                row(6,
                        cell(dateLabel = new TextPane("Date:")),
                        cell(datePicker = new DatePicker()).weightBy(1)),
                row(6,
                        cell(timeLabel = new TextPane("Time:")),
                        cell(timePicker = new TimePicker()).weightBy(1)),
                glue(),
                row(4,
                        glue(),
                        cell(createButton = new JButton("Create!")),
                        cell(cancelButton = new JButton("Cancel")))
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
