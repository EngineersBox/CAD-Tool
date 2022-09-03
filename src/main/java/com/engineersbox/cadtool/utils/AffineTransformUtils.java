package com.engineersbox.cadtool.utils;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class AffineTransformUtils {

    public AffineTransformUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static AffineTransform getRelativeScale(final double scaleX,
                                                   final double scaleY,
                                                   final Point2D point) {
        final AffineTransform transform = AffineTransform.getTranslateInstance(point.getX(), point.getY());
        transform.concatenate(AffineTransform.getScaleInstance(scaleX, scaleY));
        transform.concatenate(AffineTransform.getTranslateInstance(-point.getX(), -point.getY()));
        return transform;
    }

}
