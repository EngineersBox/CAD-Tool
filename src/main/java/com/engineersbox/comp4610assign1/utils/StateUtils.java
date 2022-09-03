package com.engineersbox.comp4610assign1.utils;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class StateUtils {

    private StateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <R> R pushPop(final Function<Graphics2D, R> func, final Graphics2D g) {
        final Graphics2D g2d = (Graphics2D) g.create(); // Push state
        final R result = func.apply(g2d);
        g2d.dispose(); // Pop state
        return result;
    }

    public static void pushPop(final Consumer<Graphics2D> func, final Graphics2D g) {
        final Graphics2D g2d = (Graphics2D) g.create(); // Push state
        func.accept(g2d);
        g2d.dispose(); // Pop state
    }

}
