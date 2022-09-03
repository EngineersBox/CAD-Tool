package com.engineersbox.cadtool.draw;

import com.engineersbox.cadtool.Options;
import com.engineersbox.cadtool.draw.element.DrawElement;
import com.engineersbox.cadtool.draw.element.ElementControlPoint;
import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.facade.StoreFacade;
import com.engineersbox.cadtool.draw.factory.DrawElementFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Drawing - this is a list of draw elements which make up the drawing
 * @author Eric McCreath
 */

public class Drawing extends ArrayList<DrawElement> {
	@Serial
	private static final long serialVersionUID = 1L;
	private static final String DRAWING = "Drawing";
	private static final int CONTROL_POINT_RADIUS = 5;

	DrawElementFactory drawElementFactory;
	Options options;
	RenderState renderState;

	public Drawing(final DrawElementFactory drawElementFactory,
				   final Options options,
				   final RenderState renderState) {
		this.drawElementFactory = drawElementFactory;
		this.options = options;
		this.renderState = renderState;
	}

	/**
	 * Draw each element in our 'drawing'.
	 * @param g Graphics2D context.
	 */
	public void draw(final Graphics2D g) {
		for (final DrawElement d : this) d.draw(g, this.renderState);
	}

	public void applyTransform(final AffineTransform transform) {
		for (final DrawElement d : this) d.applyTransform(transform);
	}

	public void updateOptions() {
		for (final DrawElement d : this) d.updateOptions(this.options);
	}

	public ElementControlPoint findControl(final Point point) {
		for (final DrawElement d : this) {
			final List<Point2D> cps = d.controlPoints();
			for (int i = 0;i<cps.size();i++) {  // return the first control point within the limited radius
				if (cps.get(i).distance(point) < Drawing.CONTROL_POINT_RADIUS) return new ElementControlPoint(d,i);
			}
		}
		return null;
	}

	public void clearDrawing() {
		clear();
	}

	public void save(final File file) {
		final StoreFacade sf = new StoreFacade(file, Drawing.DRAWING);
		for (final DrawElement de : this) de.storeElement(sf);
		sf.close();
	}

	public static Drawing load(final File file,
							   final DrawElementFactory drawElementFactory,
							   final Options options,
							   final RenderState renderState) {
		final LoadFacade lf = LoadFacade.load(file);
		if (lf == null) {
			throw new IllegalStateException("Unable to load facade");
		}
		final Drawing drawing = new Drawing(drawElementFactory, options, renderState);
		String name;
		while ((name = lf.nextElement()) != null) {
			final DrawElement element = drawElementFactory.createElementFromLoadFacade(name, lf);
			drawing.add(element);
		}
		return drawing;
	}
}
