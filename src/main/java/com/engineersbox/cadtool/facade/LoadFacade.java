package com.engineersbox.cadtool.facade;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.geom.Point2D;
import java.io.File;

/**
 * LoadFacade - This provides a facade into the standard XML API for loading
 * drawings.
 * 
 * @author Eric McCreath
 *
 */

public class LoadFacade {
	NodeList nl;
	int nodepos;
	Node currentelement;

	public static LoadFacade load(final File file) {
		final LoadFacade res = new LoadFacade();
		try {
			// load the xml tree
			final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc = db.parse(file);

			// parse the tree and obtain the person info
			final Node drawingElement = doc.getFirstChild();
			res.nl = drawingElement.getChildNodes();
			res.nodepos = 0;
			return res;
		} catch (final Exception e) {
			System.err.println("Problem loading " + file);
		}
		return null;
	}

	public String nextElement() {
		if (this.nodepos == this.nl.getLength())
			return null;
		this.currentelement = this.nl.item(this.nodepos);
		this.nodepos++;
		return this.currentelement.getNodeName();
	}

	public Point2D getPoint(final String name) {
		final NodeList list = this.currentelement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				return LoadFacade.extractPoint(list.item(i));
			}
		}
		return null;
	}

	public Integer getInteger(final String name) {
		final NodeList list = this.currentelement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				return extractInteger(list.item(i));
			}
		}
		return null;
	}

	private static Integer extractInteger(final Node item) {
		return Integer.parseInt(item.getTextContent());
	}

	private static Double extractDouble(final Node item) {
		return Double.parseDouble(item.getTextContent());
	}

	public static Point2D extractPoint(final Node p) {
		final NodeList nl = p.getChildNodes();
		return new Point2D.Double(Double.parseDouble(nl.item(0).getTextContent()), Double.parseDouble(nl.item(1).getTextContent()));
	}

	public Double getDouble(final String name) {
		final NodeList list = this.currentelement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				return extractDouble(list.item(i));
			}
		}
		return null;
	}

	public String getString(final String name) {
		final NodeList list = this.currentelement.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				return list.item(i).getTextContent();
			}
		}
		return null;
	}
}
