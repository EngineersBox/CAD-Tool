package com.engineersbox.cadtool.draw.element;

/**
 * ElementControlPoint - this class is used to reference the draw element of the control point and 
 * the index of the control of the list of control points of the element. 
 * @author Eric McCreath
 *
 */
public record ElementControlPoint(DrawElement element,
								  int control) {
}
