package com.engineersbox.cadtool.draw.factory;

import com.engineersbox.cadtool.draw.element.*;
import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.toolbar.ToolBar;
import com.engineersbox.cadtool.utils.UncheckedThrowsAdapter;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory to create draw elements.
 */
public class BasicDrawElementFactory implements DrawElementFactory {

	private static final URL ELEMENT_SEARCH_PACKAGE = ClasspathHelper.forClass(DrawElement.class);
	private static final Reflections ELEMENT_REFLECTIONS = new Reflections(
			BasicDrawElementFactory.ELEMENT_SEARCH_PACKAGE,
			Scanners.ConstructorsAnnotated,
			Scanners.MethodsAnnotated,
			Scanners.SubTypes
	);
	private static final Map<String, Function<LoadFacade, DrawElement>> LOAD_METHODS = ELEMENT_REFLECTIONS.getMethodsAnnotatedWith(FacadeLoadHandler.class)
			.stream()
			.collect(Collectors.toMap(
					(final Method method) -> method.getAnnotation(FacadeLoadHandler.class).value(),
					(final Method method) -> UncheckedThrowsAdapter.unchecked((final LoadFacade lf) -> (DrawElement) method.invoke(null, lf))
			));
	private final Map<String, Constructor<? extends DrawElement>> POINT_INIT_CONSTRUCTORS = ELEMENT_REFLECTIONS.getConstructorsAnnotatedWith(PointInitialiser.class)
			.stream()
			.map((final Constructor constructor) -> (Constructor<? extends DrawElement>) constructor)
			.collect(Collectors.toMap(
					(final Constructor<? extends DrawElement> constructor) -> constructor.getAnnotation(PointInitialiser.class).value(),
					Function.identity()
			));
	private static final Map<String, String> BUTTON_MAPPINGS =  ELEMENT_REFLECTIONS.getSubTypesOf(DrawElement.class)
			.stream()
			.filter((final Class<? extends DrawElement> element) -> element.isAnnotationPresent(ElementMetadata.class))
			.map((final Class<?> cls) -> cls.getAnnotation(ElementMetadata.class))
			.collect(Collectors.toMap(
					ElementMetadata::displayName,
					ElementMetadata::name
			));

	public BasicDrawElementFactory() {
	}

	@Override
	public DrawElement createElementFromMousePress(final String toolCommand, final Color color, final Point pos) {
		DrawElement drawelement = null;
		final Constructor<? extends DrawElement> constructor = POINT_INIT_CONSTRUCTORS.get(toolCommand);
		if (constructor != null) {
			try {
				drawelement = constructor.newInstance(pos);
			} catch (final InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException(String.format(
						"Unable to instantiate point initialiser for %s",
						toolCommand
				), e);
			}
		}
		drawelement = new ColorDrawElement(drawelement,
				color);
		return drawelement;
	}

	@Override
	public DrawElement createElementFromLoadFacade(final String name, final LoadFacade lf) {
		DrawElement element = null;
		final Function<LoadFacade, DrawElement> loadMethod = LOAD_METHODS.get(name);
		if (loadMethod != null) {
			element = loadMethod.apply(lf);
		} else {
			System.err.println("Unknown Element: " + name);
		}
		final Integer color = lf.getInteger("color");
		if (color != null) element = new ColorDrawElement(element, new Color(color));
		
		return element;
	}

	@Override
	public void addButtons(final ToolBar drawTool) {
		BUTTON_MAPPINGS.forEach(drawTool::addButton);
	}

}
