package com.engineersbox.cadtool.draw.element;

import com.engineersbox.cadtool.Options;
import com.engineersbox.cadtool.draw.RenderState;
import com.engineersbox.cadtool.facade.StoreFacade;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * ColorDrawElement  - gives us a way of adding color to elements.
 * Works by wrapping draw call and modifying g.setColor
 *
 * @author Eric McCreath
 * edited by Matthew Aitchison
 *
 */
public class ColorDrawElement extends DrawElementDecorator {

	Color color;

	/**
	 * Construct our ColorDrawElement.
	 * @param drawElement The base object to draw with a particular color.
	 * @param color The color.
	 */
	public ColorDrawElement(final DrawElement drawElement, final Color color) {
		this.drawElement = drawElement;
		this.color = color;
	}
	
	@Override
	public void draw(final Graphics2D g, final RenderState renderState) {
		final Color oldColor = g.getColor();
		g.setColor(this.color);
		this.drawElement.draw(g, renderState);
		g.setColor(oldColor);
	}

	@Override
	public void applyTransform(final AffineTransform transform) {
		this.drawElement.applyTransform(transform);
	}

	@Override
	public void updateControlPoints(final Point2D point) {
		this.drawElement.updateControlPoints(point);
	}

	@Override
	public void updateOptions(final Options options) {
		this.drawElement.updateOptions(options);
	}

	@Override
	public void storeElement(final StoreFacade sf) {
		this.drawElement.storeElement(sf);
		sf.addInteger("color", this.color.getRGB());
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final ColorDrawElement ce) {
			return this.color.equals(ce.color) && this.drawElement.equals(ce.drawElement);
		}
		return false;
	}
}
