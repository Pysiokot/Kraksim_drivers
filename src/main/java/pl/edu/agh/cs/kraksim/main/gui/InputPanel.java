package pl.edu.agh.cs.kraksim.main.gui;

import com.google.common.base.Objects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class InputPanel extends JPanel {
	private static final long serialVersionUID = 3399884476951113192L;
	private JTextField textField = null;
	private JFileChooser fileChooser = null;

	public InputPanel(String inputName, String defaultValue, int initialSize, final JFileChooser fileChooser) {
		this.fileChooser = fileChooser;
		initLayout(inputName, Objects.firstNonNull(defaultValue, ""), initialSize);
	}

	private void initLayout(String inputName, String defaultValue, int initialSize) {
		setLayout(new FlowLayout(FlowLayout.RIGHT));
		textField = new JTextField(defaultValue, initialSize);
		JLabel label = new JLabel(inputName + ": ");
		label.setLabelFor(textField);

		add(label);
		add(textField);

		if (fileChooser != null) {
			JButton btn = new JButton("...");
			btn.setMargin(new Insets(0, 0, 0, 0));
			Dimension dim = new Dimension(30, 20);
			btn.setSize(dim);
			btn.setPreferredSize(dim);

			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (fileChooser.showOpenDialog(InputPanel.this) == JFileChooser.APPROVE_OPTION) {
						File file = fileChooser.getSelectedFile();
						textField.setText(file.getAbsolutePath());
					}
				}
			});

			add(btn);
		} else {
			JButton btn = new JButton("...");
			btn.setMargin(new Insets(0, 0, 0, 0));
			Dimension dim = new Dimension(30, 20);
			btn.setSize(dim);
			btn.setPreferredSize(dim);
			btn.setEnabled(false);

			add(btn);
		}
	}

	public String getText() {
		return textField.getText();
	}

	public void setText(String text) {
		textField.setText(text);
	}
}
