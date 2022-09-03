package com.engineersbox.comp4610assign1.draw.element;

import com.engineersbox.comp4610assign1.Options;
import com.engineersbox.comp4610assign1.draw.RenderState;
import com.engineersbox.comp4610assign1.facade.StoreFacade;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * DrawElement - all elements that make up the drawing will extend this abstract class. 
 * @author Eric McCreath
 * Edited by Matthew Aitchison
 */
public interface DrawElement {

     /**
      * Draw element to Graphics2D context.
      */
     void draw(final Graphics2D g, final RenderState renderState);

     void applyTransform(final AffineTransform transform);

     List<Point2D> controlPoints();

     /**
      * Move a control point
      * @param control index to control point
      * @param pos new position for control point.
      */
     void moveControlPoint(final int control, final Point2D pos);

     /**
      * Update the element control points with a new entry
      * @param point New control points to use
      */
     default void updateControlPoints(final Point2D point) {
          // Not reactive to updates, ignored
     }

     default void updateOptions(final Options options) {
          // Not reactive to updates, ignored
     }

     /**
      * Save element.
      */
     void storeElement(final StoreFacade sf);
}
