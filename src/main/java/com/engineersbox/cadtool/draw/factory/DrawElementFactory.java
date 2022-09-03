package com.engineersbox.cadtool.draw.factory;

import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.toolbar.ToolBar;
import com.engineersbox.cadtool.draw.element.DrawElement;

import java.awt.*;

public interface DrawElementFactory {
	DrawElement createElementFromMousePress(final String toolcommand, final Color color, final Point pos);
	DrawElement createElementFromLoadFacade(final String name, final LoadFacade lf);
	void addButtons(final ToolBar drawtool);
}
