package com.engineersbox.comp4610assign1.draw.element;

import com.engineersbox.comp4610assign1.draw.RenderState;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/*
 * DrawElementDecorator - the decorator class for elements
 * Eric McCreath
 */


public abstract class DrawElementDecorator implements DrawElement {

	public DrawElement drawElement; // this is the one being decorated
	
	@Override
	public void draw(final Graphics2D g, final RenderState renderState) {  // often this would get overwritten
		this.drawElement.draw(g, renderState);
	}

	@Override
	public List<Point2D> controlPoints() {
		return this.drawElement.controlPoints();    // this can just pass through
	}

	@Override
	public void moveControlPoint(final int control, final Point2D pos) {
		this.drawElement.moveControlPoint(control, pos); // this can also just pass through
	}
}
