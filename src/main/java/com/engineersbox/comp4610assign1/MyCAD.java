package com.engineersbox.comp4610assign1;

import com.engineersbox.comp4610assign1.draw.DrawArea;
import com.engineersbox.comp4610assign1.draw.Drawing;
import com.engineersbox.comp4610assign1.draw.RenderState;
import com.engineersbox.comp4610assign1.draw.factory.BasicDrawElementFactory;
import com.engineersbox.comp4610assign1.draw.factory.DrawElementFactory;
import com.engineersbox.comp4610assign1.toolbar.ToolBar;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.engineersbox.comp4610assign1.utils.UncheckedThrowsAdapter.ThrowsConsumer;
import static com.engineersbox.comp4610assign1.utils.UncheckedThrowsAdapter.unchecked;

/**
 * Draw GUI - simple drawing program
 * Eric McCreath 2015 GPL
 * Edited by Matthew Aitchison
 */
public class MyCAD implements Runnable, ActionListener {

	public static final String EDIT_TOOL = "EDIT_TOOL";

	private static final String EXIT_COMMAND = "exit";
	private static final String CLEAR_COMMAND = "clear";
	private static final String SAVE_COMMAND = "save";
	private static final String OPEN_COMMAND = "open";
	private static final String GPU_ACCELERATION_COMMAND = "gpuAcceleration";
	private final Map<String, Consumer<ActionEvent>> eventHandlers = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(MyCAD.class))
			.setScanners(Scanners.MethodsAnnotated)
	).getMethodsAnnotatedWith(ActionHandler.class).stream()
			.filter((final Method method) -> method.getParameterCount() == 1)
			.filter((final Method method) -> method.getParameterTypes()[0].isAssignableFrom(ActionEvent.class))
			.collect(Collectors.toMap(
					(final Method method) -> method.getAnnotation(ActionHandler.class).value(),
					(final Method method) -> unchecked((ThrowsConsumer<ActionEvent>) (final ActionEvent ae) -> method.invoke(this, ae))
			));

	public JFrame jframe;
	DrawArea drawArea;
	DrawElementFactory drawElementFactory;
	JSlider scaleSlider;
	public Drawing drawing;
	public ToolBar drawtool;
	public ToolBar colortool;
	private final Options options;
	private final RenderState renderState;
	private MeasurementMenu measurementMenu;
	JFileChooser fileChooser = new JFileChooser();

	public MyCAD() {
		this.options = new Options();
		this.renderState = new RenderState();
		SwingUtilities.invokeLater(this);
	}

	public static void main(final String[] args) {
		new MyCAD();
	}

	@Override
	public void run() {
		/**
		 * Run the program.
		 */

		this.jframe = new JFrame("MyCAD");
		this.jframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		this.drawElementFactory = new BasicDrawElementFactory();

		this.drawing = new Drawing(this.drawElementFactory, this.options, this.renderState);
		this.measurementMenu = new MeasurementMenu(this.jframe, this.renderState);
		this.measurementMenu.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent e) {
				drawArea.repaint();
			}
			@Override
			public void componentResized(ComponentEvent e) {}
			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {}
		});

		// set up the menu bar
		final JMenuBar bar = new JMenuBar();
		final JMenu menu = new JMenu("File");
		makeMenuItem(menu, "New", MyCAD.CLEAR_COMMAND);
		makeMenuItem(menu, "Open", MyCAD.OPEN_COMMAND);
		makeMenuItem(menu, "Save", MyCAD.SAVE_COMMAND);
		makeMenuItem(menu, "Exit", MyCAD.EXIT_COMMAND);
		bar.add(menu);

		final JMenu optionsMenu = new JMenu("Options");
		final JCheckBox gpuAccelerationCheckbox = new JCheckBox("Use GPU Accelerated Splines (If Available)");
		gpuAccelerationCheckbox.addActionListener(this);
		gpuAccelerationCheckbox.setActionCommand(MyCAD.GPU_ACCELERATION_COMMAND);
		optionsMenu.add(gpuAccelerationCheckbox);
		bar.add(optionsMenu);

		final JMenu measurementMenu = new JMenu("Measurement");

		final JMenuItem propertiesMenuItem = new JMenuItem("Properties");
		propertiesMenuItem.addActionListener((final ActionEvent e) -> this.measurementMenu.setVisible(true));
		measurementMenu.add(propertiesMenuItem);

		bar.add(measurementMenu);

		this.jframe.setJMenuBar(bar);

		// set up the tool bar at the top of the window that enable actions like clear
		final JPanel actionArea = new JPanel();
		actionArea.setLayout(new BoxLayout(actionArea, BoxLayout.X_AXIS));

		// set up the tool bar at the right the enable different drawing functions
		// (edit, line, box, text,...)

		this.drawtool = new ToolBar(BoxLayout.Y_AXIS);
		this.drawtool.addButton("Edit", MyCAD.EDIT_TOOL);
		this.drawElementFactory.addButtons(this.drawtool);

		// set up the draw area - this is the JComponent that enable the drawing/viewing
		// of the drawing
		this.drawArea = new DrawArea(this, this.drawElementFactory);

		this.drawtool.addChangeObserver(() -> {
			if (this.drawtool.getSelectCommand().equals(MyCAD.EDIT_TOOL)) {
				this.jframe.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else {
				this.jframe.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		});
		this.scaleSlider = new JSlider(SwingConstants.HORIZONTAL, 1, 100, 1);

		this.colortool = new ToolBar(BoxLayout.Y_AXIS);
		this.colortool.addButton("BLACK", Color.black);
		this.colortool.addButton("RED", Color.red);
		this.colortool.addButton("BLUE", Color.blue);

		final JButton jbutton = new JButton("Clear");
		jbutton.setActionCommand(MyCAD.CLEAR_COMMAND);
		jbutton.addActionListener(this);
		actionArea.add(jbutton);
		final JScrollPane drawPane = new JScrollPane(
				this.drawArea,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
		);
		drawPane.setWheelScrollingEnabled(false);
		drawPane.setPreferredSize(new Dimension(600, 600));
		final Container contentPane = this.jframe.getContentPane();
		contentPane.add(actionArea, BorderLayout.PAGE_START);
		contentPane.add(drawPane, BorderLayout.CENTER);
		contentPane.add(this.drawtool, BorderLayout.LINE_END);
		contentPane.add(this.colortool, BorderLayout.LINE_START);
		contentPane.add(this.scaleSlider, BorderLayout.PAGE_END);
		this.jframe.setMinimumSize(new Dimension(100, 100));
		this.jframe.setVisible(true);
		this.jframe.pack();
	}

	/**
	 * Create a new menu item.
	 */
	private void makeMenuItem(final JMenu menu, final String name, final String command) {
		final JMenuItem menuItem = new JMenuItem(name);
		menu.add(menuItem);
		menuItem.addActionListener(this);
		menuItem.setActionCommand(command);
	}

	@Override
	/**
	 * Perform an ActionEvent.
	 */
	public void actionPerformed(final ActionEvent ae) {
		final Consumer<ActionEvent> eventConsumer = this.eventHandlers.get(ae.getActionCommand());
		if (eventConsumer != null) {
			eventConsumer.accept(ae);
		}
	}

	@ActionHandler(MyCAD.CLEAR_COMMAND)
	private void handleClear(final ActionEvent ae) {
		this.drawing.clearDrawing();
		this.drawArea.repaint();
	}

	@ActionHandler(MyCAD.EXIT_COMMAND)
	private static void handleExit(final ActionEvent ae) {
		System.exit(0);
	}

	@ActionHandler(MyCAD.SAVE_COMMAND)
	private void handleSave(final ActionEvent ae) {
		if (this.fileChooser.showOpenDialog(this.jframe) == JFileChooser.APPROVE_OPTION) {
			this.drawing.save(this.fileChooser.getSelectedFile());
		}
	}

	@ActionHandler(MyCAD.OPEN_COMMAND)
	private void handleOpen(final ActionEvent ae) {
		if (this.fileChooser.showOpenDialog(this.jframe) == JFileChooser.APPROVE_OPTION) {
			this.drawing = Drawing.load(this.fileChooser.getSelectedFile(), this.drawElementFactory, this.options, this.renderState);
			this.drawArea.repaint();
		}
	}

	@ActionHandler(MyCAD.GPU_ACCELERATION_COMMAND)
	private void handleGPUAcceleration(final ActionEvent ae) {
		this.options.useGpuAcceleration = true;
		this.drawing.updateOptions();
		this.drawArea.repaint();
	}
}
