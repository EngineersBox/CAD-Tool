package com.engineersbox.cadtool.draw.element;

import com.engineersbox.cadtool.draw.RenderState;
import com.engineersbox.cadtool.draw.Strokes;
import com.engineersbox.cadtool.draw.factory.FacadeLoadHandler;
import com.engineersbox.cadtool.draw.factory.PointInitialiser;
import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.facade.StoreFacade;
import com.engineersbox.cadtool.utils.PointUtils;
import com.engineersbox.cadtool.utils.StateUtils;
import com.engineersbox.cadtool.utils.TextUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

@ElementMetadata(
        name = CircleElement.CIRCLE_ELEMENT,
        displayName = "Circle",
        additionalControlPoints = 2
)
public class CircleElement implements DrawElement {

    public static final String CIRCLE_ELEMENT = "CIRCLE_ELEMENT";
    public static final String CENTRE_ATTRIBUTE = "centre";
    public static final String RADIUS_X_ATTRIBUTE = "radiusX";
    public static final String RADIUS_Y_ATTRIBUTE = "radiusY";
    private static final double MEASUREMENT_OFFSET_FACTOR = 5.0;

    private Point2D centre;
    private double radiusX;
    private double radiusY;

    @PointInitialiser(CircleElement.CIRCLE_ELEMENT)
    public CircleElement(final Point2D pos) {
        this(pos, 0, 0);
    }

    public CircleElement(final Point2D centre,
                         final double radiusX,
                         final double radiusY) {
        this.centre = centre;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
    }

    @Override
    public void draw(final Graphics2D g, final RenderState renderState) {
        g.draw(new Ellipse2D.Double(
                this.centre.getX() - this.radiusX,
                this.centre.getY() - this.radiusY,
                this.radiusX * 2,
                this.radiusY * 2
        ));
        if (renderState.measurementsEnabled()) {
            StateUtils.pushPop(
                    (final Graphics2D g2d) -> drawMeasurements(g2d, renderState),
                    g
            );
        }
    }

    private double circumfrence() {
        return 2.0 * Math.PI * Math.sqrt((Math.pow(Math.abs(this.radiusX), 2.0)+ Math.pow(Math.abs(this.radiusY), 2.0)) / 2.0);
    }

    private void drawMeasurements(final Graphics2D g, final RenderState renderState) {
        g.setStroke(Strokes.DASH_STROKE);
        final Point2D[] xRadiusPoints = new Point2D[]{
                this.centre,
                PointUtils.add(
                        this.centre,
                        new Point2D.Double(this.radiusX, 0)
                )
        };
        final Point2D[] yRadiusPoints = new Point2D[]{
                this.centre,
                PointUtils.sub(
                        this.centre,
                        new Point2D.Double(0, this.radiusY + (MEASUREMENT_OFFSET_FACTOR * 3))
                )
        };
        g.draw(new Ellipse2D.Double(
                this.centre.getX() - this.radiusX - MEASUREMENT_OFFSET_FACTOR,
                this.centre.getY() - this.radiusY - MEASUREMENT_OFFSET_FACTOR,
                (this.radiusX + MEASUREMENT_OFFSET_FACTOR) * 2,
                (this.radiusY + MEASUREMENT_OFFSET_FACTOR) * 2
        ));
        TextUtils.centredAt(
                renderState.getFinalisedMeasurementString(circumfrence()),
                yRadiusPoints[1],
                g
        );
        g.draw(new Line2D.Double(xRadiusPoints[0], xRadiusPoints[1]));
        g.draw(new Line2D.Double(yRadiusPoints[0], yRadiusPoints[1]));

        final Point2D horizontalTextCentre = PointUtils.add(
                PointUtils.mid(xRadiusPoints[0], xRadiusPoints[1]),
                new Point2D.Double(0, MEASUREMENT_OFFSET_FACTOR * 1.5)
        );
        TextUtils.centredAt(
                renderState.getFinalisedMeasurementString(Math.abs(xRadiusPoints[0].getX() - xRadiusPoints[1].getX())),
                horizontalTextCentre,
                g
        );

        final Point2D verticalTextCentre = PointUtils.sub(
                PointUtils.mid(yRadiusPoints[0], yRadiusPoints[1]),
                new Point2D.Double(MEASUREMENT_OFFSET_FACTOR * 1.9, 0)
        );
        g.rotate(Math.toRadians(90), verticalTextCentre.getX(), verticalTextCentre.getY());
        TextUtils.centredAt(
                renderState.getFinalisedMeasurementString(Math.abs(yRadiusPoints[0].getY() - yRadiusPoints[1].getY())),
                verticalTextCentre,
                g
        );
    }

    @Override
    public void applyTransform(final AffineTransform transform) {
        final double previousX = this.centre.getX();
        final double previousY = this.centre.getY();
        this.centre = PointUtils.applyTransform(this.centre, transform);
        this.radiusX = PointUtils.applyTransform(
                new Point2D.Double(
                        previousX + this.radiusX,
                        previousY
                ),
                transform
        ).getX() - this.centre.getX();
        this.radiusY = PointUtils.applyTransform(
                new Point2D.Double(
                        previousX,
                        previousY + this.radiusY
                ),
                transform
        ).getY() - this.centre.getY();
    }

    @Override
    public List<Point2D> controlPoints() {
        return List.of(
                this.centre,
                new Point2D.Double(
                        this.centre.getX() + this.radiusX,
                        this.centre.getY()
                ),
                new Point2D.Double(
                        this.centre.getX(),
                        this.centre.getY() - this.radiusY
                )
        );
    }

    @Override
    public void moveControlPoint(final int control, final Point2D pos) {
        if (control == 0) {
            this.centre = pos;
        } else if (control == 1) {
            this.radiusX = this.centre.distance(pos);
        } else if (control == 2) {
            this.radiusY = this.centre.distance(pos);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof final CircleElement ce))
            return false;
        return ce.centre.equals(this.centre)
                && ce.radiusX == this.radiusX
                && ce.radiusY == this.radiusY;
    }

    @Override
    public void storeElement(final StoreFacade sf) {
        sf.start(CircleElement.CIRCLE_ELEMENT);
        sf.addPoint(CircleElement.CENTRE_ATTRIBUTE, this.centre);
        sf.addDouble(CircleElement.RADIUS_X_ATTRIBUTE, this.radiusX);
        sf.addDouble(CircleElement.RADIUS_Y_ATTRIBUTE, this.radiusY);
    }

    @FacadeLoadHandler(CircleElement.CIRCLE_ELEMENT)
    public static CircleElement loadElement(final LoadFacade lf) {
        return new CircleElement(
                lf.getPoint(CircleElement.CENTRE_ATTRIBUTE),
                lf.getDouble(CircleElement.RADIUS_X_ATTRIBUTE),
                lf.getDouble(CircleElement.RADIUS_Y_ATTRIBUTE)
        );
    }
}
