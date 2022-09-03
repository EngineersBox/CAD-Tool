package com.engineersbox.cadtool.facade;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.geom.Point2D;
import java.io.File;
/**
 * StoreFacade - This provides a facade into the standard XML API for saving drawings.
 * 
 * @author Eric McCreath
 *
 */
public class StoreFacade {
	private Document doc;
	private Element drawingElement;
	private Element currentElement;
	private File file;

	public StoreFacade(final File file, final String name) {
		try {
			final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			this.doc = db.newDocument();
			this.drawingElement = this.doc.createElement(name);
			this.currentElement = null;
			this.file = file;
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void start(final String name) { // create and start a new element
		if (this.currentElement != null) end();
		this.currentElement = this.doc.createElement(name);
	}
	
	private void end() { // end the element at add it to the drawing element
		this.drawingElement.appendChild(this.currentElement);
		this.currentElement = null;
	}

	public void addPoint(final String name, final Point2D a) {
		assert(this.currentElement != null);
		this.currentElement.appendChild(StoreFacade.xmlPoint(name, a, this.doc));
	}

	public void close() {
		if (this.currentElement != null) end();
		try {
			this.doc.appendChild(this.drawingElement);
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();
			final DOMSource source = new DOMSource(this.doc);
			final StreamResult result = new StreamResult(this.file);
			transformer.transform(source, result);
		} catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Problem saving " + this.file + " " + e);
		}
	}

	private static Node xmlPoint(final String name, final Point2D p, final Document doc) {
		final Element e = doc.createElement(name);
		final Element ex = doc.createElement("x");
		ex.setTextContent(Double.toString(p.getX()));
		final Element ey = doc.createElement("y");
		ey.setTextContent(Double.toString(p.getY()));
		e.appendChild(ex);
		e.appendChild(ey);
		return e;
	}

	public void addInteger(final String name, final int value) {
		assert(this.currentElement != null);
		this.currentElement.appendChild(xmlInteger(name, value, this.doc));
	}

	private Node xmlInteger(final String name, final int value, final Document doc2) {
		final Element e = this.doc.createElement(name);
		e.setTextContent(Integer.toString(value));
		return e;
	}
	
	private Node xmlDouble(final String name, final double value, final Document doc2) {
		final Element e = this.doc.createElement(name);
		e.setTextContent(Double.toString(value));
		return e;
	}

	private Node xmlString(final String name, final String value, final Document doc2) {
		final Element e = this.doc.createElement(name);
		e.setTextContent(value);
		return e;
	}

	public void addDouble(final String name, final double value) {
		assert(this.currentElement != null);
		this.currentElement.appendChild(xmlDouble(name, value, this.doc));
	}

	public void addString(final String name, final String value) {
		assert(this.currentElement != null);
		this.currentElement.appendChild(xmlString(name, value, this.doc));
	}
}
