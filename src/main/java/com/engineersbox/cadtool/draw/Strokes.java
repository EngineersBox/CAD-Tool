package com.engineersbox.cadtool.draw;

import java.awt.*;

public class Strokes {

    private Strokes() {
        throw new IllegalStateException("Utility class");
    }

    public static final BasicStroke DASH_STROKE = new BasicStroke(
            1.0f,
            BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{8.0f},
            0.0f
    );

    public static final BasicStroke FINE_DASH_STROKE = new BasicStroke(
            1.0f,
            BasicStroke.CAP_SQUARE,
            BasicStroke.JOIN_MITER,
            10.0f, new float[]{3.0f},
            0.0f
    );

}
