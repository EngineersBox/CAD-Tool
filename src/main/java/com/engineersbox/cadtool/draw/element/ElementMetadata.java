package com.engineersbox.cadtool.draw.element;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ElementMetadata {
    String name();
    String displayName();
    int additionalControlPoints();

    int N_ARY_CONTROL_POINTS = -1;
}
