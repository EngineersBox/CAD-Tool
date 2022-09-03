package com.engineersbox.cadtool.draw.element;

import com.engineersbox.cadtool.draw.ParallelLineMeasurement;
import com.engineersbox.cadtool.draw.RenderState;
import com.engineersbox.cadtool.draw.factory.FacadeLoadHandler;
import com.engineersbox.cadtool.draw.factory.PointInitialiser;
import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.utils.PointUtils;
import com.engineersbox.cadtool.facade.StoreFacade;
import com.engineersbox.cadtool.utils.StateUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * LineElement - represents a simple line with a start and end point.
 * 
 * @author Eric McCreath
 *
 */
@ElementMetadata(
		name = LineElement.LINE_ELEMENT,
		displayName = "Line",
		additionalControlPoints = 1
)
public class LineElement implements DrawElement {

	public static final String LINE_ELEMENT = "LINE_ELEMENT";
	public static final String START_ATTRIBUTE = "start";
	public static final String END_ATTRIBUTE = "end";

	public Point2D start;
	public Point2D end;

	@PointInitialiser(LineElement.LINE_ELEMENT)
	public LineElement(final Point2D pos) {
		this(pos, pos);
	}

	public LineElement(final Point2D s, final Point2D e) {
		this.start = s;
		this.end = e;
	}

	@Override
	public void draw(final Graphics2D g, final RenderState renderState) {
		g.draw(new Line2D.Float(this.start, this.end));
		if (renderState.measurementsEnabled()) {
			StateUtils.pushPop(
					(final Graphics2D g2d) -> drawMeasurements(g2d, renderState),
					g
			);
		}
	}

	private void drawMeasurements(final Graphics2D g, final RenderState renderState) {
		ParallelLineMeasurement.drawMeasurementLine(
				this.start,
				this.end,
				g,
				renderState
		);
	}

	@Override
	public void applyTransform(final AffineTransform transform) {
		this.start = PointUtils.applyTransform(this.start, transform);
		this.end = PointUtils.applyTransform(this.end, transform);
	}

	// controlPoints - there is just: start, end, mid
	@Override
	public List<Point2D> controlPoints() {
		return List.of(
				this.start,
				this.end,
				PointUtils.mid(this.start, this.end)
		);
	}

	@Override
	public void moveControlPoint(final int control, final Point2D pos) {
		if (control == 0) // start
			this.start = pos;
		else if (control == 1) // end
			this.end = pos;
		else if (control == 2) { // mid
			final Point2D vec = PointUtils.sub(pos, PointUtils.mid(this.start, this.end));
			this.start = PointUtils.add(this.start, vec);
			this.end = PointUtils.add(this.end, vec);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof final LineElement le))
			return false;
		return le.end.equals(this.end) && le.start.equals(this.start);
	}

	@Override
	public void storeElement(final StoreFacade sf) {
		sf.start(LineElement.LINE_ELEMENT);
		sf.addPoint(LineElement.START_ATTRIBUTE, this.start);
		sf.addPoint(LineElement.END_ATTRIBUTE, this.end);
	}

	@FacadeLoadHandler(LineElement.LINE_ELEMENT)
	public static DrawElement loadElement(final LoadFacade lf) {
		return new LineElement(
				lf.getPoint(LineElement.START_ATTRIBUTE),
				lf.getPoint(LineElement.END_ATTRIBUTE)
		);
	}
}
