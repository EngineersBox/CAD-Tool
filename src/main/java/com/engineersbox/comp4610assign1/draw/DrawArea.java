package com.engineersbox.comp4610assign1.draw;

import com.engineersbox.comp4610assign1.MyCAD;
import com.engineersbox.comp4610assign1.draw.element.*;
import com.engineersbox.comp4610assign1.draw.factory.DrawElementFactory;
import com.engineersbox.comp4610assign1.toolbar.ToolChangeObserver;
import com.engineersbox.comp4610assign1.utils.AffineTransformUtils;
import com.engineersbox.comp4610assign1.utils.PointUtils;
import com.engineersbox.comp4610assign1.utils.StateUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DrawArea - this class deals with the "view" and "control" for the drawing.
 * Eric McCreath 2015 GPL
 * Edited by Matthew Aitchison
 */

public class DrawArea extends JComponent implements MouseMotionListener, MouseListener, MouseWheelListener, ToolChangeObserver {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final float SCALE_FACTOR = 0.1f;
	private static final String ELEMENT_SEARCH_PACKAGE = "com.engineersbox.comp4610assign1.draw.element";
	private static final Reflections ELEMENT_REFLECTIONS = new Reflections(
			DrawArea.ELEMENT_SEARCH_PACKAGE,
			Scanners.SubTypes
	);
	private static final Map<String, Integer> ELEMENT_CONTROL_POINTS = DrawArea.ELEMENT_REFLECTIONS.getSubTypesOf(DrawElement.class)
			.stream()
			.filter((final Class<? extends DrawElement> element) -> element.isAnnotationPresent(ElementMetadata.class))
			.map((final Class<?> cls) -> cls.getAnnotation(ElementMetadata.class))
			.collect(Collectors.toMap(
					ElementMetadata::name,
					ElementMetadata::additionalControlPoints
			));

	private final MyCAD drawGUI;

	private ElementControlPoint currentControl;
	private final List<ElementControlPoint> controlPoints;
	private int currentControlPointCount;
	private DrawElement currentElement;
	private final DrawElementFactory drawAreaFactory;
	private FontMetrics fontMetrics;
	private boolean isEditing;
	private boolean rightMousePressed;
	private boolean isSelectingPointGroup;
	private BoxElement selectionBox;
	private final List<ElementControlPoint> selectedControlPoints;
	private boolean isNAryPointElement;
	private String currentTool;

	private final AffineTransform transform;
	private Point2D currentPointerPosition;
	private Point2D previousPointerPosition;

	/**
	 * Construct a DrawArea Object.
	 * @param drawGUI gui to draw to.
	 * @param drawCanvasFactory Factory for constructing a canvas.
	 */
	public DrawArea(final MyCAD drawGUI, final DrawElementFactory drawCanvasFactory) {
		this.drawGUI = drawGUI;
		this.drawAreaFactory = drawCanvasFactory;
		this.currentControl = null;
		this.currentElement = null;
		this.isEditing = false;
		this.isSelectingPointGroup = false;
		this.rightMousePressed = false;
		this.isNAryPointElement = false;
		this.controlPoints = new ArrayList<>();
		this.selectedControlPoints = new ArrayList<>();
		this.setPreferredSize(new Dimension(700, 500));
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		bindKeys();
		this.setFocusable(true);
		this.requestFocus();
		this.currentControlPointCount = 0;
		this.currentPointerPosition = new Point2D.Double();
		this.previousPointerPosition = new Point2D.Double();
		drawGUI.drawtool.addChangeObserver(this);
		this.currentTool = (String) drawGUI.drawtool.getSelectCommand();
		this.transform = new AffineTransform();
	}

	private void bindKeys() {
		final String enterKey = "ENTER";
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(enterKey),
				enterKey
		);
		this.getActionMap().put(
				enterKey,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (isNAryPointElement) {
							isNAryPointElement = false;
							currentControlPointCount = 0;
							currentControl = null;
							controlPoints.clear();
							repaint();
						}
					}
				}
		);

		final String escapeKey = "ESCAPE";
		this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke((char) KeyEvent.VK_ESCAPE),
				escapeKey
		);
		this.getActionMap().put(
				escapeKey,
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (!selectedControlPoints.isEmpty()) {
							selectedControlPoints.clear();
							selectionBox = null;
							isSelectingPointGroup = false;
							repaint();
						}
					}
				}
		);
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent mouseWheelEvent) {
		// negative = scroll up, positive = scroll down
		final float scale = mouseWheelEvent.getWheelRotation() < 0 ? 1 + DrawArea.SCALE_FACTOR : 1 - DrawArea.SCALE_FACTOR;
		this.transform.setTransform(AffineTransformUtils.getRelativeScale(
				scale,
				scale,
				this.currentPointerPosition
		));
		this.drawGUI.drawing.applyTransform(this.transform);
		repaint();
	}

	/**
	 * Paint our canvas area.
	 * @param g Graphics2D context.
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		this.fontMetrics = g2.getFontMetrics();
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.black);
		this.drawGUI.drawing.draw(g2);

		g.setColor(Color.black);
		final String command = (String) this.drawGUI.drawtool.getSelectCommand();

		if (this.isSelectingPointGroup && this.selectionBox != null) {
			StateUtils.pushPop(
					(final Graphics2D g2d) -> {
						g2d.setStroke(Strokes.FINE_DASH_STROKE);
						this.selectionBox.draw(g2d, new RenderState());
					},
					g2
			);
		}
		if (!this.selectedControlPoints.isEmpty()) {
			StateUtils.pushPop(
					(final Graphics2D g2d) -> {
						g2d.setColor(Color.green);
						drawControlPoints(
								g2d,
								this.selectedControlPoints.stream()
										.map((final ElementControlPoint controlPoint) -> controlPoint.element().controlPoints().get(controlPoint.control()))
										.toList()
						);
					},
					g2
			);
		}
		
		// draw the control points if in edit mode
		if (!command.equals(MyCAD.EDIT_TOOL)) {
			return;
		}
		for (final DrawElement de : this.drawGUI.drawing) {
			drawControlPoints(g2, de.controlPoints());
		}
	}

	private void drawControlPoints(final Graphics2D g, final List<Point2D> points) {
		for (final Point2D p : points) {
			g.draw(new Ellipse2D.Double(p.getX() - 2.0, p.getY() - 2.0, 4.0, 4.0));
		}

	}

	private List<ElementControlPoint> findSelectedControlPoints() {
		// This could be improved by using a quad tree to partition the
		// screen space and store ElementControlPoint instances in buckets of
		// size 1, splitting when more than one point is in a region. You
		// could even be more general with a kd-tree for example.
		if (this.selectionBox == null) {
			return new ArrayList<>();
		}
		final List<ElementControlPoint> selectedControlPoints = new ArrayList<>();
		for (final DrawElement element : this.drawGUI.drawing) {
			final List<Point2D> elementControlPoints = element.controlPoints();
			for (int i = 0; i < elementControlPoints.size(); i++) {
				if (this.selectionBox.contains(elementControlPoints.get(i))) {
					selectedControlPoints.add(new ElementControlPoint(
							element,
							i
					));
				}
			}
		}
		return selectedControlPoints;
	}

	private void updateMousePoints(final MouseEvent me) {
		this.previousPointerPosition = new Point2D.Double(
				this.currentPointerPosition.getX(),
				this.currentPointerPosition.getY()
		);
		this.currentPointerPosition = me.getPoint();
	}

	@Override
	public void mouseDragged(final MouseEvent me) {
		updateMousePoints(me);
		if (this.isSelectingPointGroup) {
			this.selectionBox.moveControlPoint(1, me.getPoint());
			this.selectedControlPoints.addAll(findSelectedControlPoints());
		}
		if (this.rightMousePressed && !me.isShiftDown()) {
			this.transform.setToTranslation(
					(this.currentPointerPosition.getX() - this.previousPointerPosition.getX()),
					(this.currentPointerPosition.getY() - this.previousPointerPosition.getY())
			);
			if (!this.selectedControlPoints.isEmpty()) {
				for (final ElementControlPoint controlPoint : this.selectedControlPoints) {
					controlPoint.element().moveControlPoint(
							controlPoint.control(),
							PointUtils.applyTransform(controlPoint.element().controlPoints().get(controlPoint.control()), transform)
					);
				}
			} else if (!this.isSelectingPointGroup) {
				this.drawGUI.drawing.applyTransform(this.transform);
			}
		}
		if (this.currentControl != null) {
			this.currentControl.element().moveControlPoint(this.currentControl.control(), this.currentPointerPosition);
		}
		repaint();
	}

	@Override
	public void mouseMoved(final MouseEvent me) {
		updateMousePoints(me);
	}

	@Override
	public void mouseClicked(final MouseEvent me) {
		// Unused
	}

	@Override
	public void mouseEntered(final MouseEvent me) {
		// Unused
	}

	@Override
	public void mouseExited(final MouseEvent me) {
		// Unused
	}

	@Override
	public void mousePressed(final MouseEvent me) {
		updateMousePoints(me);
		if (me.getButton() == MouseEvent.BUTTON1) {
			leftMousePressed(me);
		} else if (me.getButton() == MouseEvent.BUTTON3) {
			rightMousePressed(me);
		}
	}

	private void leftMousePressed(final MouseEvent me) {
		this.currentTool = (String) this.drawGUI.drawtool.getSelectCommand();
		this.selectedControlPoints.clear();
		if (this.currentTool.equals(MyCAD.EDIT_TOOL)) {
			this.currentControl = this.drawGUI.drawing.findControl(me.getPoint());
			this.isEditing = true;
			this.isNAryPointElement = false;
			repaint();
			return;
		} else if (this.controlPoints.isEmpty()) {
			this.currentElement = this.drawAreaFactory.createElementFromMousePress(
					this.currentTool,
					(Color) this.drawGUI.colortool.getSelectCommand(),
					me.getPoint()
			);
			this.currentElement.updateOptions(this.drawGUI.drawing.options);
			this.drawGUI.drawing.add(this.currentElement);
			this.currentControl = new ElementControlPoint(this.currentElement, 1);
			this.currentControlPointCount = DrawArea.ELEMENT_CONTROL_POINTS.getOrDefault(this.currentTool, 0);
			if (this.currentControlPointCount == 0) {
				throw new IllegalStateException("Invalid command: " + this.currentTool);
			} else if (this.currentControlPointCount == ElementMetadata.N_ARY_CONTROL_POINTS) {
				this.isNAryPointElement = true;
			}
		} else if (this.isNAryPointElement || this.controlPoints.size() < this.currentControlPointCount) {
			this.currentControl = new ElementControlPoint(this.currentElement, this.controlPoints.size() + 1);
		}
		repaint();
	}

	private void rightMousePressed(final MouseEvent me) {
		this.rightMousePressed = true;
		this.isSelectingPointGroup = me.isShiftDown();
		if (this.isSelectingPointGroup) {
			this.selectionBox = new BoxElement(me.getPoint());
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me) {
		updateMousePoints(me);
		if (me.getButton() == MouseEvent.BUTTON1) {
			leftMouseReleased(me);
		} else if (me.getButton() == MouseEvent.BUTTON3) {
			rightMouseReleased(me);
		}
	}

	private void leftMouseReleased(final MouseEvent me) {
		this.controlPoints.add(this.currentControl);
		this.currentControl = null;
		if (this.isEditing) {
			this.controlPoints.clear();
			this.currentControlPointCount = 0;
			this.isEditing = false;
			return;
		} else if (this.controlPoints.size() != this.currentControlPointCount) {
			this.currentElement.updateControlPoints(me.getPoint());
			repaint();
			return;
		} else if (this.currentElement instanceof DrawElementDecorator decoratedElement
				&& decoratedElement.drawElement instanceof LabelElement labelElement) {
			final String labelTextResult = JOptionPane.showInputDialog(this.drawGUI.jframe, "Label text");
			if (labelTextResult == null) {
				this.currentElement = null;
				this.drawGUI.drawing.remove(this.drawGUI.drawing.size() - 1);
			} else {
				labelElement.setText(
						labelTextResult,
						this.fontMetrics.stringWidth(labelTextResult),
						this.fontMetrics.getHeight()
				);
			}
		}
		this.controlPoints.clear();
		this.currentControlPointCount = 0;
		repaint();
	}

	private void rightMouseReleased(final MouseEvent me) {
		this.rightMousePressed = false;
		if (this.isSelectingPointGroup) {
			this.selectionBox = null;
			this.isSelectingPointGroup = false;
			repaint();
		}
	}

	@Override
	public void update() {
		repaint();
	}
}
