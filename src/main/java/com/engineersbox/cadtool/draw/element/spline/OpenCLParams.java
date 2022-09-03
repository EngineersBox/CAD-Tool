package com.engineersbox.cadtool.draw.element.spline;

import org.jocl.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.jocl.CL.*;

public class OpenCLParams {

    private final cl_context context;
    private final cl_command_queue queue;
    private final cl_program program;
    private final cl_device_id device;

    public OpenCLParams(final String filePath) {
        final long deviceType = CL_DEVICE_TYPE_ALL;
        CL.setExceptionsEnabled(true);

        final int[] platforms = new int[1];
        clGetPlatformIDs(0, null, platforms);
        final cl_platform_id[] platformIds = new cl_platform_id[platforms[0]];
        clGetPlatformIDs(
                platformIds.length,
                platformIds,
                null
        );
        final cl_platform_id platform = platformIds[0];

        final int[] devices = new int[1];
        clGetDeviceIDs(
                platform,
                deviceType,
                0,
                null,
                devices
        );
        final int deviceCount = devices[0];
        final cl_device_id[] deviceIds = new cl_device_id[deviceCount];
        clGetDeviceIDs(
                platform,
                deviceType,
                deviceCount,
                deviceIds,
                null
        );
        this.device = deviceIds[0];

        final cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        final int[] error = new int[1];
        this.context = clCreateContext(
                contextProperties,
                1,
                new cl_device_id[]{this.device},
                null,
                null,
                error
        );
        checkError(error[0]);

        final byte[] driverVersion = new byte[100];
        clGetDeviceInfo(
                this.device,
                CL_DEVICE_VERSION,
                Sizeof.cl_char * 100,
                Pointer.to(driverVersion),
                null
        );
        final float openCLVersion = Float.parseFloat(new String(driverVersion).split(" ")[1]);
        System.out.println("Detected OpenCL Version: " + openCLVersion);

        if (openCLVersion < 2.0f) {
            this.queue = clCreateCommandQueue(
                    this.context,
                    this.device,
                    0,
                    error
            );
        } else {
            final cl_queue_properties properties = new cl_queue_properties();
            this.queue = clCreateCommandQueueWithProperties(
                    this.context,
                    this.device,
                    properties,
                    error
            );
        }
        checkError(error[0]);

        final String programCode;
        try {
            programCode = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource(filePath)).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.program = clCreateProgramWithSource(
                this.context,
                1,
                new String[]{programCode},
                null,
                error
        );
        checkError(error[0]);
        clBuildProgram(
                this.program,
                0,
                null,
                null,
                null,
                null
        );
        Runtime.getRuntime().addShutdownHook(new Thread(this::releaseContextual));
    }

    private static void checkError(final int errorCode) {
        if (errorCode != CL_SUCCESS) {
            throw new RuntimeException("Unable to create command queue: " + stringFor_errorCode(errorCode));
        }
    }

    public cl_context getContext() {
        return this.context;
    }

    public cl_command_queue getQueue() {
        return this.queue;
    }

    public cl_kernel getKernel(final String kernelName) {
        return clCreateKernel(this.program, kernelName, null);
    }

    public cl_program getProgram() {
        return this.program;
    }

    public long getMaxWorkGroupSize(final cl_kernel kernel) {
        final long[] workGroupSize = new long[1];
        final int result = clGetKernelWorkGroupInfo(
                kernel,
                this.device,
                CL_KERNEL_WORK_GROUP_SIZE,
                Sizeof.cl_ulong,
                Pointer.to(workGroupSize),
                null
        );
        return result != CL_SUCCESS ? -1 : workGroupSize[0];
    }

    public void releaseAll(final cl_kernel kernel) {
        if (kernel != null) {
            clReleaseKernel(kernel);
        }
    }

    public void releaseContextual() {
        clReleaseProgram(this.program);
        clReleaseCommandQueue(this.queue);
        clReleaseContext(this.context);
    }
}
