package com.engineersbox.comp4610assign1.draw.element;

import com.engineersbox.comp4610assign1.Options;
import com.engineersbox.comp4610assign1.draw.RenderState;
import com.engineersbox.comp4610assign1.draw.Strokes;
import com.engineersbox.comp4610assign1.draw.factory.FacadeLoadHandler;
import com.engineersbox.comp4610assign1.draw.factory.PointInitialiser;
import com.engineersbox.comp4610assign1.facade.LoadFacade;
import com.engineersbox.comp4610assign1.utils.PointUtils;
import com.engineersbox.comp4610assign1.facade.StoreFacade;
import com.engineersbox.comp4610assign1.utils.StateUtils;
import com.engineersbox.comp4610assign1.utils.TextUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * BoxElement - represents a simple rectangle defined by two points.
 * 
 * @author Eric McCreath
 * edited by Matthew Aitichson
 *
 */
@ElementMetadata(
		name = BoxElement.BOX_ELEMENT,
		displayName = "Box",
		additionalControlPoints = 1
)
public class BoxElement implements DrawElement {

	public static final String BOX_ELEMENT = "BOX_ELEMENT";
	public static final String TOP_LEFT_POINT_ATTRIBUTE = "topLeft";
	public static final String BOTTOM_RIGHT_POINT_ATTRIBUTE = "bottomRight";
	private static final double MEASUREMENT_OFFSET_FACTOR = 5.0;

	Point2D topLeft;
	Point2D bottomRight;

	@PointInitialiser(BoxElement.BOX_ELEMENT)
	public BoxElement(final Point2D pos) {
		this(pos, pos);
	}

	/**
	 * Construct a box element.
	 * @param topLeft top left point
	 * @param bottomRight bottom right point
	 */
	public BoxElement(final Point2D topLeft, final Point2D bottomRight) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
	}

	/**
	 * Draws the rectangle to given Graphics2D context.
	 * @param g Graphics Context.
	 */
	@Override
	public void draw(final Graphics2D g, final RenderState renderState) {
		drawPrimitive(g);
		if (renderState.measurementsEnabled()) {
			StateUtils.pushPop(
					(final Graphics2D g2d) -> drawMeasurements(g2d, renderState),
					g
			);
		}
	}

	private void drawPrimitive(final Graphics2D g) {
		g.draw(new Line2D.Double(
				this.topLeft,
				new Point2D.Double(
						this.bottomRight.getX(),
						this.topLeft.getY()
				)
		));
		g.draw(new Line2D.Double(
				new Point2D.Double(
						this.bottomRight.getX(),
						this.topLeft.getY()
				), this.bottomRight
		));
		g.draw(new Line2D.Double(
				this.bottomRight,
				new Point2D.Double(
						this.topLeft.getX(),
						this.bottomRight.getY()
				)
		));
		g.draw(new Line2D.Double(
				new Point2D.Double(
						this.topLeft.getX(),
						this.bottomRight.getY()
				), this.topLeft
		));
	}

	private void drawMeasurements(final Graphics2D g,
								  final RenderState renderState) {
		g.setStroke(Strokes.DASH_STROKE);
		final double xOffset = (this.topLeft.getX() > this.bottomRight.getX() ? 1 : -1);
		final double yOffset = (this.topLeft.getY() > this.bottomRight.getY() ? 1 : -1);
		final Point2D[] verticalSide = new Point2D[]{
				new Point2D.Double(
						this.topLeft.getX() + (MEASUREMENT_OFFSET_FACTOR * xOffset),
						this.bottomRight.getY()
				),
				PointUtils.add(
						this.topLeft,
						new Point2D.Double(MEASUREMENT_OFFSET_FACTOR * xOffset, 0)
				)
		};
		final Point2D[] horizontalSide = new Point2D[]{
				PointUtils.sub(
						this.bottomRight,
						new Point2D.Double(0, MEASUREMENT_OFFSET_FACTOR * yOffset)
				),
				new Point2D.Double(
						this.topLeft.getX(),
						this.bottomRight.getY() - (MEASUREMENT_OFFSET_FACTOR * yOffset)
				)
		};
		g.draw(new Line2D.Double(verticalSide[0], verticalSide[1]));
		g.draw(new Line2D.Double(horizontalSide[0], horizontalSide[1]));

		final Point2D horizontalTextCentre = PointUtils.sub(
				PointUtils.mid(horizontalSide[0], horizontalSide[1]),
				new Point2D.Double(0, yOffset * MEASUREMENT_OFFSET_FACTOR * 1.5)
		);
		TextUtils.centredAt(
				renderState.getFinalisedMeasurementString(Math.abs(horizontalSide[0].getX() - horizontalSide[1].getX())),
				horizontalTextCentre,
				g
		);

		final Point2D verticalTextCentre = PointUtils.add(
				PointUtils.mid(verticalSide[0], verticalSide[1]),
				new Point2D.Double(xOffset * MEASUREMENT_OFFSET_FACTOR * 1.9, 0)
		);
		g.rotate(Math.toRadians(90 * xOffset), verticalTextCentre.getX(), verticalTextCentre.getY());
		TextUtils.centredAt(
				renderState.getFinalisedMeasurementString(Math.abs(verticalSide[0].getY() - verticalSide[1].getY())),
				verticalTextCentre,
				g
		);
	}

	@Override
	public void applyTransform(final AffineTransform transform) {
		this.topLeft = PointUtils.applyTransform(this.topLeft, transform);
		this.bottomRight = PointUtils.applyTransform(this.bottomRight, transform);
	}

	/**
	 * ControlPoints - noting the control points for a box are: topleft, bottomright, bottomleft, topright, center
	 * @return array of four control points.
	 */
	@Override
	public List<Point2D> controlPoints() {
		final List<Point2D> controlPoints = new ArrayList<>();
		controlPoints.add(this.topLeft);
		controlPoints.add(this.bottomRight);
		controlPoints.add(new Point2D.Double(this.topLeft.getX(), this.bottomRight.getY()));
		controlPoints.add(new Point2D.Double(this.bottomRight.getX(), this.topLeft.getY()));
		controlPoints.add(PointUtils.mid(this.topLeft, this.bottomRight));
		return controlPoints;
	}

	/**
	 * Process moving of a control point.
	 * @param control index to control point
	 * @param pos new position.
	 */
	@Override
	public void moveControlPoint(final int control, final Point2D pos) {
		if (control == 0)  // topleft
			this.topLeft = pos;
		else if (control == 1) // bottomright
			this.bottomRight = pos;
		else if (control == 2) { // bottomleft
			this.bottomRight = new Point2D.Double(this.bottomRight.getX(), pos.getY());
			this.topLeft = new Point2D.Double(pos.getX(), this.topLeft.getY());
		} else if (control == 3) { // topright
			this.bottomRight = new Point2D.Double(pos.getX(), this.bottomRight.getY());
			this.topLeft = new Point2D.Double(this.topLeft.getX(), pos.getY());
		} else if (control == 4) { // center
			final Point2D vec = PointUtils.sub(pos, PointUtils.mid(this.topLeft, this.bottomRight));
			this.topLeft = PointUtils.add(this.topLeft, vec);
			this.bottomRight = PointUtils.add(this.bottomRight, vec);
		}
	}

	@Override
	public void updateControlPoints(final Point2D points) {
		// Not reactive to updates, ignored
	}

	@Override
	public void updateOptions(final Options options) {
		// Not reactive to updates, ignored
	}

	public boolean contains(final Point2D point) {
		return new Rectangle(
				(int) this.topLeft.getX(),
				(int) this.topLeft.getY(),
				(int) (this.bottomRight.getX() - this.topLeft.getX()),
				(int) (this.bottomRight.getY() - this.topLeft.getY())
		).contains(point);
	}

	@Override
	public void storeElement(final StoreFacade sf) {
		sf.start(BoxElement.BOX_ELEMENT);
		sf.addPoint(BoxElement.TOP_LEFT_POINT_ATTRIBUTE, this.topLeft);
		sf.addPoint(BoxElement.BOTTOM_RIGHT_POINT_ATTRIBUTE, this.bottomRight);
	}

	@FacadeLoadHandler(BoxElement.BOX_ELEMENT)
	public static DrawElement loadElement(final LoadFacade lf) {
		return new BoxElement(
				lf.getPoint(BoxElement.TOP_LEFT_POINT_ATTRIBUTE),
				lf.getPoint(BoxElement.BOTTOM_RIGHT_POINT_ATTRIBUTE)
		);
	}
}
