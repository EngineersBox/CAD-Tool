package com.engineersbox.comp4610assign1;

import com.engineersbox.comp4610assign1.draw.RenderState;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.stream.IntStream;

public class MeasurementMenu extends JDialog implements ActionListener, PropertyChangeListener {

    private static final String ENABLE_MEASUREMENT_COMMAND = "enableMeasurement";

    private final RenderState renderState;
    private final JCheckBox enableMeasurementCheckBox;
    private final JFormattedTextField scaleInput;
    private final JTextField unitsInput;
    private final JComboBox<Integer> precisionInput;

    public MeasurementMenu(final Frame frame, final RenderState renderState) {
        super(frame, "Measurement Properties", true);
        this.renderState = renderState;

        this.setSize(new Dimension(350, 120));
        this.setResizable(false);
        final JPanel modalPanel = new JPanel(new GridLayout(4, 2));
        this.add(modalPanel);

        modalPanel.add(new JLabel("Enable Measurements"));
        this.enableMeasurementCheckBox = new JCheckBox();
        this.enableMeasurementCheckBox.addActionListener(this);
        this.enableMeasurementCheckBox.setActionCommand(MeasurementMenu.ENABLE_MEASUREMENT_COMMAND);
        modalPanel.add(this.enableMeasurementCheckBox);

        modalPanel.add(new JLabel("Scale: "));
        this.scaleInput = new JFormattedTextField(this.renderState.getMeasurementScale());
        this.scaleInput.addPropertyChangeListener("value", this);
        modalPanel.add(this.scaleInput);

        modalPanel.add(new JLabel("Units: "));
        this.unitsInput = new JTextField(this.renderState.getMeasurementUnits());
        this.unitsInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                renderState.setMeasurementUnits(unitsInput.getText());
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                renderState.setMeasurementUnits(unitsInput.getText());
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                renderState.setMeasurementUnits(unitsInput.getText());
            }
        });
        modalPanel.add(this.unitsInput);

        modalPanel.add(new JLabel("Precision: "));
        this.precisionInput = new JComboBox<>(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
        this.precisionInput.setSelectedIndex(this.renderState.getMeasurementPrecision() - 1);
        this.precisionInput.addActionListener(this);
        modalPanel.add(this.precisionInput);

    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (e.getActionCommand().equals(MeasurementMenu.ENABLE_MEASUREMENT_COMMAND)) {
            this.renderState.setMeasurements(this.enableMeasurementCheckBox.isSelected());
        } else if (e.getSource() == this.precisionInput) {
            this.renderState.setMeasurementPrecision(this.precisionInput.getSelectedIndex() + 1);
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent e) {
        if (e.getSource() == this.scaleInput) {
            this.renderState.setMeasurementScale(((Number) this.scaleInput.getValue()).doubleValue());
        }
    }
}
