package com.engineersbox.cadtool.draw;

public class RenderState {

    private boolean measurements;
    private double measurementScale;
    private String measurementUnits;
    private int measurementPrecision;

    public RenderState() {
        this.measurements = false;
        this.measurementScale = 1000.0;
        this.measurementUnits = "mm";
        this.measurementPrecision = 2;
    }

    public boolean measurementsEnabled() {
        return this.measurements;
    }

    public void setMeasurements(final boolean measurements) {
        this.measurements = measurements;
    }

    public double getMeasurementScale() {
        return this.measurementScale;
    }

    public void setMeasurementScale(final double measurementScale) {
        this.measurementScale = measurementScale;
    }

    public String getMeasurementUnits() {
        return this.measurementUnits;
    }

    public void setMeasurementUnits(final String measurementUnits) {
        this.measurementUnits = measurementUnits;
    }

    public int getMeasurementPrecision() {
        return this.measurementPrecision;
    }

    public String getMeasurementPrecisionFormatSpecifier() {
        return String.format(
                "%%.%df",
                Math.min(15, Math.max(0, this.measurementPrecision)) // 15 is max decimal digits for a double
        );
    }

    public String getFinalisedMeasurementString(final double length) {
        return String.format(
                getMeasurementPrecisionFormatSpecifier() + " %s",
                length,
                getMeasurementUnits()
        );
    }

    public void setMeasurementPrecision(final int measurementPrecision) {
        this.measurementPrecision = measurementPrecision;
    }
}
