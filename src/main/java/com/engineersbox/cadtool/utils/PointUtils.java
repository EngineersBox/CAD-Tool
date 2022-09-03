package com.engineersbox.cadtool.utils;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * PUtil - a utility class for calculations on points
 * Eric McCreath 2020
 */
public class PointUtils {

	private PointUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Point2D mid(final Point2D p1, final Point2D p2) {
		return new Point2D.Double((p1.getX() + p2.getX())/2.0,(p1.getY() + p2.getY())/2.0) ;
	}
	
	public static Point2D sub(final Point2D p1, final Point2D p2) {
		return new Point2D.Double((p1.getX() - p2.getX()),(p1.getY() - p2.getY())) ;
	}
	
	public static Point2D add(final Point2D p1, final Point2D p2) {
		return new Point2D.Double((p1.getX() + p2.getX()),(p1.getY() + p2.getY())) ;
	}
	
	public static Point2D scale(final Point2D p1, final double s) {
		return new Point2D.Double((p1.getX() * s),(p1.getY() * s)) ;
	}

	public static Point2D applyTransform(final Point2D point,
										 final AffineTransform transform) {
		final Point2D newPoint = new Point2D.Double();
		transform.transform(point, newPoint);
		return newPoint;
	}
}
