package com.engineersbox.cadtool.draw.element;

import com.engineersbox.cadtool.draw.ParallelLineMeasurement;
import com.engineersbox.cadtool.draw.RenderState;
import com.engineersbox.cadtool.draw.factory.FacadeLoadHandler;
import com.engineersbox.cadtool.draw.factory.PointInitialiser;
import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.facade.StoreFacade;
import com.engineersbox.cadtool.utils.PointUtils;
import com.engineersbox.cadtool.utils.StateUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@ElementMetadata(
        name = TriangleElement.TRIANGLE_ELEMENT,
        displayName = "Triangle",
        additionalControlPoints = 2
)
public class TriangleElement extends Path2D.Double implements DrawElement {

    public static final String TRIANGLE_ELEMENT = "TRIANGLE_ELEMENT";
    public static final String POINT_A_ATTRIBUTE = "TRIANGLE_POINT_A";
    public static final String POINT_B_ATTRIBUTE = "TRIANGLE_POINT_B";
    public static final String POINT_C_ATTRIBUTE = "TRIANGLE_POINT_C";

    private Point2D centre;
    private Point2D[] vertices;

    @PointInitialiser(TriangleElement.TRIANGLE_ELEMENT)
    public TriangleElement(final Point2D pos) {
        this(pos, 0);
    }

    public TriangleElement(final Point2D centre, final double radius) {
        this(
                new Point2D.Double(
                        centre.getX(),
                        centre.getY() + radius
                ),
                new Point2D.Double(
                        centre.getX() - radius,
                        centre.getY() - radius
                ),
                new Point2D.Double(
                        centre.getX() + radius,
                        centre.getY() - radius
                )
        );
        this.centre = centre;
    }

    private TriangleElement(final Point2D a,
                            final Point2D b,
                            final Point2D c) {
        this.vertices = new Point2D[]{a, b, c};
        this.centre = calculateCentroid();
    }

    private Point2D calculateCentroid() {
        return new Point2D.Double(
                (this.vertices[0].getX() + this.vertices[1].getX() + this.vertices[2].getX()) / 3,
                (this.vertices[0].getY() + this.vertices[1].getY() + this.vertices[2].getY()) / 3
        );
    }

    @Override
    public void draw(final Graphics2D g, final RenderState renderState) {
        super.reset();
        super.moveTo(this.vertices[0].getX(), this.vertices[0].getY());
        super.lineTo(this.vertices[1].getX(), this.vertices[1].getY());
        super.lineTo(this.vertices[2].getX(), this.vertices[2].getY());
        super.closePath();
        g.draw(this);
        if (renderState.measurementsEnabled()) {
            drawMeasurements(g, renderState);
        }
    }

    private void drawMeasurements(final Graphics2D g, final RenderState renderState) {
        StateUtils.pushPop(
                (final Graphics2D g2d) -> ParallelLineMeasurement.drawMeasurementLine(
                        this.vertices[0],
                        this.vertices[1],
                        g2d,
                        renderState
                ),
                g
        );
        StateUtils.pushPop(
                (final Graphics2D g2d) -> ParallelLineMeasurement.drawMeasurementLine(
                        this.vertices[1],
                        this.vertices[2],
                        g2d,
                        renderState
                ),
                g
        );
        StateUtils.pushPop(
                (final Graphics2D g2d) -> ParallelLineMeasurement.drawMeasurementLine(
                        this.vertices[2],
                        this.vertices[0],
                        g2d,
                        renderState
                ),
                g
        );
    }

    @Override
    public void applyTransform(final AffineTransform transform) {
        for (int i = 0; i < this.vertices.length; i++) {
            this.vertices[i] = PointUtils.applyTransform(this.vertices[i], transform);
        }
        this.centre = calculateCentroid();
    }

    @Override
    public List<Point2D> controlPoints() {
        return List.of(
                this.vertices[0],
                this.vertices[1],
                this.vertices[2],
                this.centre
        );
    }

    private final transient List<Consumer<Point2D>> controlPointMoveHandlers = List.of(
            (final Point2D pos) -> {
                this.vertices[0] = pos;
                this.centre = calculateCentroid();
            },
            (final Point2D pos) -> {
                this.vertices[1] = pos;
                this.centre = calculateCentroid();
            },
            (final Point2D pos) -> {
                this.vertices[2] = pos;
                this.centre = calculateCentroid();
            },
            (final Point2D pos) -> {
                final double dx = pos.getX() - this.centre.getX();
                final double dy = pos.getY() - this.centre.getY();
                Arrays.stream(this.vertices).forEach((final Point2D point) -> point.setLocation(
                        point.getX() + dx,
                        point.getY() + dy
                ));
                this.centre = calculateCentroid();
            }
    );

    @Override
    public void moveControlPoint(final int control, final Point2D pos) {
        if (control < 0 || control >= this.controlPointMoveHandlers.size()) {
            return;
        }
        this.controlPointMoveHandlers.get(control).accept(pos);
    }

    @Override
    public void storeElement(final StoreFacade sf) {
        sf.start(TriangleElement.TRIANGLE_ELEMENT);
        sf.addPoint(TriangleElement.POINT_A_ATTRIBUTE, this.vertices[0]);
        sf.addPoint(TriangleElement.POINT_B_ATTRIBUTE, this.vertices[1]);
        sf.addPoint(TriangleElement.POINT_C_ATTRIBUTE, this.vertices[2]);
    }

    @FacadeLoadHandler(TriangleElement.TRIANGLE_ELEMENT)
    public static DrawElement loadElement(final LoadFacade lf) {
        return new TriangleElement(
                lf.getPoint(TriangleElement.POINT_A_ATTRIBUTE),
                lf.getPoint(TriangleElement.POINT_B_ATTRIBUTE),
                lf.getPoint(TriangleElement.POINT_C_ATTRIBUTE)
        );
    }
}
