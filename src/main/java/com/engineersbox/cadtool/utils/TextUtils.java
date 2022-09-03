package com.engineersbox.cadtool.utils;

import java.awt.*;
import java.awt.geom.Point2D;

public class TextUtils {

    public TextUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void centredAt(final String text,
                                 final Point2D point,
                                 final Graphics2D g) {
        final FontMetrics fontMetrics = g.getFontMetrics();
        final int width = fontMetrics.stringWidth(text);
        final int height = fontMetrics.getHeight();
        g.drawString(
                text,
                (int) (point.getX() - (width / 2.0)),
                (int) (point.getY() + (height / 2.0))
        );
    }

}
