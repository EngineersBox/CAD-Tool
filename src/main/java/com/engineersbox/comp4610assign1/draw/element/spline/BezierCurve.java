package com.engineersbox.comp4610assign1.draw.element.spline;

import com.engineersbox.comp4610assign1.draw.RenderState;
import com.engineersbox.comp4610assign1.draw.path.Point2DPath;
import com.engineersbox.comp4610assign1.utils.OpenCLUtils;
import com.engineersbox.comp4610assign1.utils.PointUtils;
import com.engineersbox.comp4610assign1.utils.TextUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;

import static org.jocl.CL.*;

public class BezierCurve {

    private static final String COMPUTE_CONTROL_POINTS_KERNEL_NAME = "computeBezierControlPoints";
    private final float smoothFactor;
    private final Point2D[] initialPoints;
    private final OpenCLParams openclParams;
    private final Point2D[] bezierPoints;

    public BezierCurve(final Point2D[] points,
                       final float smoothFactor,
                       final OpenCLParams openclParams,
                       final boolean useGpuAcceleration) {
        this.initialPoints = points;
        this.smoothFactor = smoothFactor;
        this.openclParams = openclParams;
        this.bezierPoints = useGpuAcceleration ? calculateControlPointsGPU() : calculateControlPointsCPU();
    }

    private Point2D[] calculateControlPointsGPU() {
        final int flattenedPointsLength = this.initialPoints.length * 2;
        final double[] flattenedPoints = new double[flattenedPointsLength];
        final int controlPointsLength = 4 * (this.initialPoints.length - 2);
        final double[] controlPoints = new double[controlPointsLength];
        for (int i = 0; i < this.initialPoints.length; i++) {
            flattenedPoints[(i * 2)] = this.initialPoints[i].getX();
            flattenedPoints[(i * 2) + 1] = this.initialPoints[i].getY();
        }
        final cl_mem deviceInitialPoints = clCreateBuffer(
                this.openclParams.getContext(),
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_double * (long) flattenedPointsLength,
                Pointer.to(flattenedPoints),
                null
        );
        int result = clEnqueueWriteBuffer(
                this.openclParams.getQueue(),
                deviceInitialPoints,
                CL_TRUE,
                0,
                Sizeof.cl_double * (long) flattenedPointsLength,
                Pointer.to(flattenedPoints),
                0,
                null,
                null
        );
        if (result != CL_SUCCESS) {
            clReleaseMemObject(deviceInitialPoints);
            this.openclParams.releaseAll(null);
            throw new IllegalStateException("Unable to write flattened points to device: " + stringFor_errorCode(result));
        }
        final cl_mem deviceControlPoints = clCreateBuffer(
                this.openclParams.getContext(),
                CL_MEM_READ_WRITE,
                Sizeof.cl_double * (long) controlPointsLength,
                null,
                null
        );
        final cl_kernel kernel = this.openclParams.getKernel(BezierCurve.COMPUTE_CONTROL_POINTS_KERNEL_NAME);
        result = OpenCLUtils.bindKernelArgs(
                kernel,
                Pair.of(Pointer.to(deviceInitialPoints), Sizeof.cl_mem),
                Pair.of(Pointer.to(deviceControlPoints), Sizeof.cl_mem),
                Pair.of(Pointer.to(new float[]{this.smoothFactor}), Sizeof.cl_float)
        );
        if (result != CL_SUCCESS) {
            clReleaseMemObject(deviceInitialPoints);
            clReleaseMemObject(deviceControlPoints);
            this.openclParams.releaseAll(kernel);
            throw new IllegalStateException("Unable to bind kernel args: " + stringFor_errorCode(result));
        }
        result = clEnqueueNDRangeKernel(
                this.openclParams.getQueue(),
                kernel,
                1,
                null,
                new long[]{Math.min(
                        2 * (this.initialPoints.length - 2L),
                        this.openclParams.getMaxWorkGroupSize(kernel)
                )},
                new long[]{1},
                0,
                null,
                null
        );
        if (result != CL_SUCCESS) {
            clReleaseMemObject(deviceInitialPoints);
            clReleaseMemObject(deviceControlPoints);
            this.openclParams.releaseAll(kernel);
            throw new IllegalStateException("Unable to execute kernel: " + stringFor_errorCode(result));
        }
        result = clFinish(this.openclParams.getQueue());
        if (result != CL_SUCCESS) {
            clReleaseMemObject(deviceInitialPoints);
            clReleaseMemObject(deviceControlPoints);
            this.openclParams.releaseAll(kernel);
            throw new IllegalStateException("Kernel did not finish successfully: " + stringFor_errorCode(result));
        }
        result = clEnqueueReadBuffer(
                this.openclParams.getQueue(),
                deviceControlPoints,
                CL_TRUE,
                0,
                Sizeof.cl_double * (long) controlPointsLength,
                Pointer.to(controlPoints),
                0,
                null,
                null
        );
        clReleaseMemObject(deviceInitialPoints);
        clReleaseMemObject(deviceControlPoints);
        this.openclParams.releaseAll(kernel);
        if (result != CL_SUCCESS) {
            throw new IllegalStateException("Unable to read result from kernel: " + stringFor_errorCode(result));
        }
        final Point2D[] finalControlPoints = new Point2D[2 * (this.initialPoints.length - 2)];
        for (int i = 0; i < 2 * (this.initialPoints.length - 2); i++) {
            finalControlPoints[i] = new Point2D.Double(
                    controlPoints[(i * 2)],
                    controlPoints[(i * 2) + 1]
            );
        }
        return finalControlPoints;
    }

    /**
     * <p>
     *     Computes the Bezier control points for a given set of points. Loosely based on an implementation
     *     by David Benson <a href="http://www.java2s.com/Code/Java/2D-Graphics-GUI/Interpolatesgivenpointsbyabeziercurve.htm">
     *         Interpolates given points by a bezier curve
     *     </a>
     * </p>
     *
     * @return Bezier control points
     */
    private Point2D[] calculateControlPointsCPU() {
        final Point2D[] finalControlPoints = new Point2D[2 * (this.initialPoints.length - 2)];
        for (int i = 0; i < 2 * (this.initialPoints.length - 2); i++) {
            final int modi = i % 2;
            final int index = (int) Math.floor(i / 2.0);
            finalControlPoints[i] = calculateBezierPoint(
                    this.initialPoints[index],
                    this.initialPoints[index + 1],
                    this.initialPoints[index + 2],
                    this.initialPoints[index + (modi == 0 ? 0 : 2)],
                    this.smoothFactor,
                    modi == 0 ? 1 : -1
            );
        }
        return finalControlPoints;
    }

    private Point2D calculateBezierPoint(final Point2D a,
                                         final Point2D b,
                                         final Point2D c,
                                         final Point2D projectionReferencePoint,
                                         final double smoothFactor,
                                         final int direction) {
        double pointDiffACX = c.getX() - a.getX();
        double pointDiffACY = c.getY() - a.getY();
        double vecACLength = Math.sqrt(pointDiffACX * pointDiffACX + pointDiffACY * pointDiffACY);
        pointDiffACX = (pointDiffACX / vecACLength) * direction;
        pointDiffACY = (pointDiffACY / vecACLength) * direction;

        double vecProjectionFactor = Math.abs((b.getX() - projectionReferencePoint.getX()) * pointDiffACX + (b.getY() - projectionReferencePoint.getY()) * pointDiffACY);
        double projectedX = vecProjectionFactor * pointDiffACX;
        double projectedY = vecProjectionFactor * pointDiffACY;

        return new Point2D.Double(
            b.getX() - smoothFactor * projectedX,
            b.getY() - smoothFactor * projectedY
        );
    }

    public void draw(final Graphics2D g, final RenderState renderState) {
        if (this.initialPoints.length < 3 || this.bezierPoints.length < 1) {
            return;
        }
        final Point2DPath path = new Point2DPath();
        path.moveTo(this.initialPoints[0]);
        path.quadTo(
                this.bezierPoints[0],
                this.initialPoints[1]
        );

        for(int i = 2; i < this.initialPoints.length - 1; i++ ) {
            path.curveTo(
                    this.bezierPoints[(2 * i) - 3],
                    this.bezierPoints[(2 * i) - 2],
                    this.initialPoints[i]
            );
        }

        path.quadTo(
                this.bezierPoints[this.bezierPoints.length - 1],
                this.initialPoints[this.initialPoints.length - 1]
        );
        g.draw(path);
        if (renderState.measurementsEnabled()) {
            // I didn't have enough time to get to this
            // with other assignments, thesis work, etc.
            // However, this computed value would have been
            // put onto some kind of "parallel" Bezier curve
            // (dunno even what that would be tbh). I couldn't
            // get a proper analytical solution that could
            // accurately create a parallel curve, so a label on the
            // initial point will have to do.
            final double length = this.initialPoints.length < 4 ? 0 : length(
                    this.initialPoints,
                    0.05
            );
            TextUtils.centredAt(
                    renderState.getFinalisedMeasurementString(length),
                    PointUtils.sub(
                            this.initialPoints[0],
                            new Point2D.Double(5, 0)
                    ),
                    g
            );
        }
    }

    private Pair<Point2D[], Point2D[]> bezierSplit(final Point2D[] points) {
        final Point2D[] left = new Point2D[4];
        Point2D[] right = new Point2D[4];
        final Point2D[][] temp = new Point2D[4][4];

        for (int j = 0; j < 4; j++) {
            temp[0][j] = new Point2D.Double(
                    points[j].getX(),
                    points[j].getY()
            );
        }

        for (int i = 1; i < 4; i++) {
            for (int j = 0; j <= 3 - i; j++) {
                temp[i][j] = new Point2D.Double(
                        (0.5 * temp[i - 1][j].getX()) + (0.5 * temp[i - 1][j + 1].getX()),
                        (0.5 * temp[i - 1][j].getY()) + (0.5 * temp[i - 1][j + 1].getY())
                );
            }
        }
        for (int j = 0; j < 4; j++) {
            left[j] = temp[j][0];
        }
        for (int j = 0; j < 4; j++) {
            right[j] = temp[3 - j][j];
        }

        return Pair.of(left, right);
    }

    /**
     * <p>
     *     Computes the length of a Bezier curve by segmenting it and processing each individually.
     *     This relies on an error threshold that scales relative to the degree of the curve.
     * </p>
     * <p>
     *     Implementation is by Earl Boebert at
     *     <a href="http://steve.hollasch.net/cgindex/curves/cbezarclen.html">
     *         Computing the Arc Length of Cubic Bezier Curves
     *     </a>,
     *     modified here and ported to Java.
     * </p>
     * <p>
     *     This implementation is not generalised (yet), so only the first four points on a Bezier
     *     curve are actually processed.
     * </p>
     *
     * @param points Points on the curve
     * @param length Initial value for length (should be 0)
     * @param error Threshold for segmentation
     * @return Currently accumulated length
     */
    private double addIfClose(final Point2D[] points,
                              final double length,
                              final double error) {
        double len = 0.0;
        double chord;
        for (int i = 0; i < 3; i++) {
            len += points[i].distance(points[i + 1]);
        }
        chord = points[0].distance(points[3]);
        if (len - chord > error) {
            final Pair<Point2D[], Point2D[]> split = bezierSplit(points);
            double splitLen = addIfClose(split.getLeft(), length, error);
            return addIfClose(split.getRight(), splitLen, error);
        }
        return len + length;
    }

    private double length(final Point2D[] points,
                          final double error) {
        return addIfClose(points, 0, error);
    }

}
