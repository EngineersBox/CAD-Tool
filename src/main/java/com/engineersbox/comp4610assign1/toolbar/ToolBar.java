package com.engineersbox.comp4610assign1.toolbar;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ToolBar - this is a widget for a list of radio buttons.
 * @author Eric McCreath
 *
 */
public class ToolBar extends JPanel implements ActionListener {

	private final ButtonGroup toolgroup;
	private final List<JRadioButton> buttons;
	private final List<ToolChangeObserver> observers;
	private final Map<ButtonModel,Object> reference;
	
	public ToolBar(final int axis) {
		this.setLayout(new BoxLayout(this,axis));
		this.toolgroup = new ButtonGroup();
		this.buttons = new ArrayList<>();
		this.observers = new ArrayList<>();
		this.reference = new HashMap<>();
	}
	
	
	public void addButton(final String text, final Object command) {
		final JRadioButton res = new JRadioButton(text);
		res.addActionListener(this);
		this.reference.put(res.getModel(),command);
		this.toolgroup.add(res);
		this.buttons.add(res);
		this.add(res);
		if (this.buttons.size() == 1) res.setSelected(true);
	}

	@Override
	public void actionPerformed(final ActionEvent arg0) {
		for (final ToolChangeObserver o : this.observers) o.update();
	}


	public Object getSelectCommand() {
		return this.reference.get(this.toolgroup.getSelection());
	}


	public void addChangeObserver(final ToolChangeObserver observer) {
		this.observers.add(observer);
	}
	
	public void removeChangeObserver(final ToolChangeObserver observer) {
		this.observers.remove(observer);
	}
	
	public JRadioButton getButton(final int i) {
		return this.buttons.get(i);
	}
}