package com.engineersbox.comp4610assign1.draw.factory;

import com.engineersbox.comp4610assign1.facade.LoadFacade;
import com.engineersbox.comp4610assign1.toolbar.ToolBar;
import com.engineersbox.comp4610assign1.draw.element.DrawElement;

import java.awt.*;
import java.awt.geom.Point2D;

public interface DrawElementFactory {
	DrawElement createElementFromMousePress(final String toolcommand, final Color color, final Point pos);
	DrawElement createElementFromLoadFacade(final String name, final LoadFacade lf);
	void addButtons(final ToolBar drawtool);
}
