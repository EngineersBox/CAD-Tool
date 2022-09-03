package com.engineersbox.cadtool.draw.element;

import com.engineersbox.cadtool.draw.RenderState;
import com.engineersbox.cadtool.draw.factory.FacadeLoadHandler;
import com.engineersbox.cadtool.draw.factory.PointInitialiser;
import com.engineersbox.cadtool.facade.LoadFacade;
import com.engineersbox.cadtool.facade.StoreFacade;
import com.engineersbox.cadtool.utils.PointUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

@ElementMetadata(
        name = LabelElement.LABEL_ELEMENT,
        displayName = "Label",
        additionalControlPoints = 1
)
public class LabelElement implements DrawElement {

    public static final String LABEL_ELEMENT = "LABEL_ELEMENT";
    public static final String TEXT_ATTRIBUTE = "text";
    public static final String LABEL_POINT_ATTRIBUTE = "labelPoint";
    public static final String LINE_TARGET_ATTRIBUTE = "lineTarget";

    private String text;
    private int textWidth;
    private int textHeight;

    public Point2D labelPoint;
    public Point2D lineTarget;

    @PointInitialiser(LabelElement.LABEL_ELEMENT)
    public LabelElement(final Point2D pos) {
        this(pos, pos);
    }

    public LabelElement(final Point2D labelPoint, final Point2D lineTarget) {
        this.labelPoint = labelPoint;
        this.lineTarget = lineTarget;
        this.textWidth = 0;
        this.textHeight = 0;
    }

    public void setText(final String text,
                        final int width,
                        final int height) {
        this.text = text;
        this.textWidth = width;
        this.textHeight = height;
    }

    private Point2D findClosestSidePoint(final Rectangle rect,
                                         final Point2D out) {
        if (rect.contains(out)) {
            return null;
        }
        final int outCode = rect.outcode(out);
        if (outCode == (Rectangle.OUT_TOP | Rectangle.OUT_LEFT)) {
            return new Point2D.Double(
                    rect.x,
                    rect.y
            );
        } else if (outCode == Rectangle.OUT_TOP) {
            return new Point2D.Double(
                    rect.x + (rect.width / 2.0),
                    rect.y
            );
        } else if (outCode == (Rectangle.OUT_TOP | Rectangle.OUT_RIGHT)) {
            return new Point2D.Double(
                    (double) rect.x + rect.width,
                    rect.y
            );
        } else if (outCode == Rectangle.OUT_RIGHT) {
            return new Point2D.Double(
                    (double) rect.x + rect.width,
                    rect.y + (rect.height / 2.0)
            );
        } else if (outCode == (Rectangle.OUT_BOTTOM | Rectangle.OUT_RIGHT)) {
            return new Point2D.Double(
                    (double) rect.x + rect.width,
                    (double) rect.y + rect.height
            );
        } else if (outCode == Rectangle.OUT_BOTTOM) {
            return new Point2D.Double(
                    rect.x + (rect.width / 2.0),
                    (double) rect.y + rect.height
            );
        } else if (outCode == (Rectangle.OUT_BOTTOM | Rectangle.OUT_LEFT)) {
            return new Point2D.Double(
                    rect.x,
                    (double) rect.y + rect.height
            );
        } else if (outCode == Rectangle.OUT_LEFT) {
            return new Point2D.Double(
                    rect.x,
                    rect.y + (rect.height / 2.0)
            );
        }
        return null;
    }

    @Override
    public void draw(Graphics2D g, final RenderState renderState) {
        if (this.text == null) {
            g.drawLine(
                    (int) this.labelPoint.getX(),
                    (int) this.labelPoint.getY(),
                    (int) this.lineTarget.getX(),
                    (int) this.lineTarget.getY()
            );
            return;
        }
        g.clearRect(
                (int) (this.labelPoint.getX() - (textWidth / 2.0)),
                (int) (this.labelPoint.getY() - (textHeight / 2.0)),
                textWidth,
                textHeight
        );
        g.drawRect(
                (int) (this.labelPoint.getX() - (textWidth / 2.0)),
                (int) (this.labelPoint.getY() - (textHeight / 2.0)),
                textWidth,
                textHeight
        );
        g.drawString(
                this.text,
                (int) (this.labelPoint.getX() - (textWidth / 2.0)),
                (int) (this.labelPoint.getY() + (textHeight / 4.0))
        );
        final Point2D closestSidePoint = findClosestSidePoint(
                new Rectangle(
                        (int) (this.labelPoint.getX() - (textWidth / 2.0)),
                        (int) (this.labelPoint.getY() - (textHeight / 2.0)),
                        textWidth,
                        textHeight

                ),
                this.lineTarget
        );
        if (closestSidePoint == null) {
            return;
        }
        g.drawLine(
                (int) closestSidePoint.getX(),
                (int) closestSidePoint.getY(),
                (int) this.lineTarget.getX(),
                (int) this.lineTarget.getY()
        );
    }

    @Override
    public void applyTransform(final AffineTransform transform) {
        this.lineTarget = PointUtils.applyTransform(this.lineTarget, transform);
        this.labelPoint = PointUtils.applyTransform(this.labelPoint, transform);
    }

    @Override
    public List<Point2D> controlPoints() {
        return List.of(
                this.lineTarget,
                this.labelPoint
        );
    }

    @Override
    public void moveControlPoint(int control, Point2D pos) {
        if (control == 0) {
            this.lineTarget = pos;
        } else if (control == 1) {
            this.labelPoint = pos;
        }
    }

    @Override
    public void storeElement(StoreFacade sf) {
        sf.start(LabelElement.LABEL_ELEMENT);
        sf.addString(LabelElement.TEXT_ATTRIBUTE, this.text);
        sf.addPoint(LabelElement.LABEL_POINT_ATTRIBUTE, this.labelPoint);
        sf.addPoint(LabelElement.LINE_TARGET_ATTRIBUTE, this.lineTarget);
    }

    @FacadeLoadHandler(LabelElement.LABEL_ELEMENT)
    public static LabelElement loadElement(final LoadFacade lf) {
        final LabelElement labelElement = new LabelElement(
                lf.getPoint(LabelElement.LABEL_POINT_ATTRIBUTE),
                lf.getPoint(LabelElement.LINE_TARGET_ATTRIBUTE)
        );
        labelElement.setText(lf.getString(LabelElement.TEXT_ATTRIBUTE), 0, 0);
        return labelElement;
    }
}
