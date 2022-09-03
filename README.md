# CG CAD Individual Assignment Start Repo

A CAD tool providing core drawing features and utilities. This is design for simple workflows with 2D shapes and measurements.

## Features

### Tools

There are 6 tools available to draw various primitives and multiple point shapes. More specifically,

* Lines
* Rectangles
* Circles
* Triangles
* Bezier Splines (Use `ENTER` to terminate spline drawing)

There is an additional attribute feature available for labelling elements of the design space. This joins two points
with a line and a user defined label.

### Measurements

In the `Measurements > Preferences` menu, there are a set of options for enabling and utilising measurements on drawn
shapes. These allow you to configure the following:

* Enable/disable
* Units
* Scale
* FP precision in shows measurement

Measurements are automatically rendered for shapes based on key elements.

| Shape         	| Measurements                         	|
|---------------	|--------------------------------------	|
| Line          	| Length                               	|
| Circle        	| Radius X<br>Radius Y<br>Circumfrence 	|
| Rectangle     	| Width<br>Height                      	|
| Triangle      	| Sides                                	|
| Bezier Spline 	| Path length (limited)                	|

### Draw Area Movement

You can move the drawing around, in edition by using mouse transformations. The drawing space can be moved by
holding right-click and dragging around. You can also zoom in by scrolling up and zoom out by scrolling down.

### Selecting Control Points

Using the edit tool, you can select control points on the drawn shapes and transform them in the draw space. There is
also the option of using `SHIFT + RMB` and dragging to select control points within and area and transform them together.
Once you are done with the selection of points, you can release them by pressing `ESC`.

## Design Outlines

### Element Drawing

Each element is defined as a standalone class, with registered metadata in the form of annotations. When a shape is
created, it is instantiated dynamically from a LUT containing reflective information on the `@ElementMetadata` annotations
that are denoted on the classes.

This allows for workflow specific actions around the shapes without too much specific handling around switching and
creating shapes.

### Render State

During a render pass, the `Drawing` will pass the current render state, which contains information about attributes
of the design that need to be considered such as measurements. This removes the need to persist render state in every
shape instance and can be passed during a draw invocation.

Render state consists purely of values for the measurement options currently, with plans for further additions at a
later stage.

### Bezier Splines

#### OpenCL Acceleration

There are two variants of the Bezier splines, one is calculated with an iterative approach on the CPU. The other is
defined in an OpenCL kernel under <src/main/resources/kernels/bezier_points.ocl>, this is a massively parallel approach
which parallelises the calculation of control points through warps with threads to match the number of points.

There is an interesting property of the Bezier splines that allows for a normally partially parallel approach to be
fully parallelised without performance inhibition. For further information on that, see the comment in the kernel definition.

You can enable OpenCL acceleration under `Options > Use GPU Accelerated Splines (If Available)`.

#### Path Length

This CAD tool partially calculates the path length of the Bezier spline. This works on increasing subdivision approximations
of each cubic section summed with the initial quadratic. This is an incomplete solution, as it only accounts for the first
cubic, but can in theory be generalised to an N-ary point curve, though not accurately.

# Dependencies

There are a few dependencies for this CAD tool, they are very minimal and are essentially just QoL improvements. None
of the dependencies are at all used for drawing or easier calculation, they are simply for improving Java handling.

* Java OpenCL Bindings (JOCL) for Bezier Spline Calculation
* Apache Commons Utils for micro wrappers on common patterns like pairs of values (non-points)
* Reflection Utils for handlers to remove bloat with reflective invocations

# Statement of Originality

This is an individual assignment, except for the list below this assignment has been entirely done by myself.

Jackson Kilrain-Mottram (u6940136)
 
Below is the complete list of ideas, help, and source code I obtained form others in completing this assignment:
+ Eric McCreath (who wrote the starting point CAD code),  used with permission. https://gitlab.cecs.anu.edu.au/u4033585/cgcadassignment2020
+ David Benson whose implementation of calulcating Bezier curves was used as a reference (http://www.java2s.com/Code/Java/2D-Graphics-GUI/Interpolatesgivenpointsbyabeziercurve.htm)
+ Earl Boebert for the Bezier path length calculation (http://steve.hollasch.net/cgindex/curves/cbezarclen.html)
