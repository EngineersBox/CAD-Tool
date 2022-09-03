package com.engineersbox.comp4610assign1.draw.element;

import com.engineersbox.comp4610assign1.Options;
import com.engineersbox.comp4610assign1.draw.RenderState;
import com.engineersbox.comp4610assign1.draw.element.spline.BezierCurve;
import com.engineersbox.comp4610assign1.draw.element.spline.OpenCLParams;
import com.engineersbox.comp4610assign1.draw.factory.FacadeLoadHandler;
import com.engineersbox.comp4610assign1.draw.factory.PointInitialiser;
import com.engineersbox.comp4610assign1.facade.LoadFacade;
import com.engineersbox.comp4610assign1.facade.StoreFacade;
import com.engineersbox.comp4610assign1.utils.PointUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

@ElementMetadata(
        name = BezierSplineElement.BEZIER_SPLINE_ELEMENT,
        displayName = "Bezier Spline",
        additionalControlPoints = ElementMetadata.N_ARY_CONTROL_POINTS
)
public class BezierSplineElement implements DrawElement {

    public static final String BEZIER_SPLINE_ELEMENT = "BEZIER_SPLINE_ELEMENT";
    public static final String POINT_COUNT_ATTRIBUTE = "pointCount";
    public static final String POINT_ATTRIBUTE_TEMPLATE = "splinePoint%d";
    private static final String BEZIER_POINTS_KERNEL_PATH = "/kernels/bezier_points.ocl";
    private static final OpenCLParams OPENCL_PARAMS = new OpenCLParams(BezierSplineElement.BEZIER_POINTS_KERNEL_PATH);
    private static final float SMOOTH_FACTOR = 0.5f;

    private List<Point2D> points;
    private boolean useGpuAcceleration;

    @PointInitialiser(BezierSplineElement.BEZIER_SPLINE_ELEMENT)
    public BezierSplineElement(final Point2D point) {
        this(new Point2D[]{point});
    }

    public BezierSplineElement(final Point2D ...points) {
        this.points = new ArrayList<>();
        this.points.addAll(List.of(points));
        this.useGpuAcceleration = false;
    }

    @Override
    public void draw(final Graphics2D g, final RenderState renderState) {
        final int pointsCount = this.points.size();
        if (pointsCount == 0) {
            return;
        } else if (pointsCount < 3) {
            g.drawLine(
                    (int) this.points.get(0).getX(),
                    (int) this.points.get(0).getX(),
                    (int) this.points.get(pointsCount == 2 ? 1 : 0).getX(),
                    (int) this.points.get(pointsCount == 2 ? 1 : 0).getY()
            );
            return;
        }
        new BezierCurve(
                this.points.toArray(Point2D[]::new),
                BezierSplineElement.SMOOTH_FACTOR,
                BezierSplineElement.OPENCL_PARAMS,
                this.useGpuAcceleration
        ).draw(g, renderState);
    }

    @Override
    public void applyTransform(final AffineTransform transform) {
        this.points = this.points.stream()
                .map((final Point2D point) -> PointUtils.applyTransform(point, transform))
                .toList();
    }

    @Override
    public List<Point2D> controlPoints() {
        return this.points;
    }

    @Override
    public void moveControlPoint(final int control, final Point2D pos) {
        if (control < 0 || control >= this.points.size()) {
            return;
        }
        this.points.set(control, pos);
    }

    @Override
    public void updateControlPoints(final Point2D point) {
        this.points.add(point);
    }

    @Override
    public void updateOptions(final Options options) {
        this.useGpuAcceleration = options.useGpuAcceleration;
    }

    @Override
    public void storeElement(final StoreFacade sf) {
        sf.start(BezierSplineElement.BEZIER_SPLINE_ELEMENT);
        sf.addInteger(BezierSplineElement.POINT_COUNT_ATTRIBUTE, this.points.size());
        for (int i = 0; i < this.points.size(); i++) {
            sf.addPoint(
                    String.format(
                            BezierSplineElement.POINT_ATTRIBUTE_TEMPLATE,
                            i
                    ),
                    this.points.get(i)
            );
        }
    }

    @FacadeLoadHandler(BezierSplineElement.BEZIER_SPLINE_ELEMENT)
    public static BezierSplineElement loadElement(final LoadFacade lf) {
        final int pointCount = lf.getInteger(BezierSplineElement.POINT_COUNT_ATTRIBUTE);
        final Point2D[] points = new Point2D[pointCount];
        for (int i = 0; i < pointCount; i++) {
            points[i] = lf.getPoint(String.format(
                    BezierSplineElement.POINT_ATTRIBUTE_TEMPLATE,
                    i
            ));
        }
        return new BezierSplineElement(points);
    }
}
