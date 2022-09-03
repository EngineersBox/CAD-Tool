package com.engineersbox.cadtool.draw;

import com.engineersbox.cadtool.utils.PointUtils;
import com.engineersbox.cadtool.utils.TextUtils;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class ParallelLineMeasurement {

    private ParallelLineMeasurement() {
        throw new IllegalStateException("Utility class");
    }

    private static final double MEASUREMENT_OFFSET_FACTOR = 5.0;
    private static final double RAD_OF_180 = Math.toRadians(180);
    private static final double RAD_OF_90 = Math.toRadians(90);

    public static void drawMeasurementLine(final Point2D start,
                                           final Point2D end,
                                           final Graphics2D g,
                                           final RenderState renderState) {
        final double baseAngle = Math.atan2(
                end.getY() - start.getY(),
                end.getX() - start.getX()
        );
        final double offsetAngle = RAD_OF_90 + baseAngle;
        final Point2D[] parallelLine = new Point2D[]{
                new Point2D.Double(
                        start.getX() + (Math.cos(offsetAngle) * MEASUREMENT_OFFSET_FACTOR),
                        start.getY() + (Math.sin(offsetAngle) * MEASUREMENT_OFFSET_FACTOR)
                ),
                new Point2D.Double(
                        end.getX() + (Math.cos(offsetAngle) * MEASUREMENT_OFFSET_FACTOR),
                        end.getY() + (Math.sin( offsetAngle) * MEASUREMENT_OFFSET_FACTOR)
                )
        };
        g.setStroke(Strokes.DASH_STROKE);
        g.draw(new Line2D.Double(parallelLine[0], parallelLine[1]));
        final Point2D midPoint = PointUtils.mid(parallelLine[0], parallelLine[1]);
        final boolean isOnLeft = baseAngle > -RAD_OF_90 && baseAngle < RAD_OF_90;
        final Point2D textCentre = new Point2D.Double(
                midPoint.getX() + (Math.cos(offsetAngle) * MEASUREMENT_OFFSET_FACTOR * (isOnLeft ? 1.5 : 2.5)),
                midPoint.getY() + (Math.sin( offsetAngle) * MEASUREMENT_OFFSET_FACTOR * (isOnLeft ? 1.5 : 2.5))
        );
        g.rotate(
                baseAngle + (isOnLeft ? 0 : RAD_OF_180),
                textCentre.getX(),
                textCentre.getY()
        );
        TextUtils.centredAt(
                renderState.getFinalisedMeasurementString(start.distance(end)),
                textCentre,
                g
        );
    }

}
