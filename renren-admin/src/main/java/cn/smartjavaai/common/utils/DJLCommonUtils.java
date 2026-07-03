package cn.smartjavaai.common.utils;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Point;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.file.Path;
import java.util.List;

public class DJLCommonUtils {
    private static final List<String> SUPPORTED_PROTOCOLS = List.of("http://", "https://");

    public DJLCommonUtils() {
    }

    public static boolean isServingPropertiesExists(Path path) {
        return false;
    }

    public static boolean isNDArrayEmpty(NDArray ndArray) {
        return ndArray == null || ndArray.getShape().size() == 0;
    }

    public static float[][] floatNDArrayToArray(NDArray ndArray) {
        float[] data = ndArray.toFloatArray();
        Shape shape = ndArray.getShape();
        int rows = (int) shape.get(0);
        int cols = (int) shape.get(1);
        float[][] result = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(data, i * cols, result[i], 0, cols);
        }
        return result;
    }

    public static Mat floatNDArrayToMat(NDArray ndArray, int cvType) {
        float[] data = ndArray.toFloatArray();
        Shape shape = ndArray.getShape();
        int rows = (int) shape.get(0);
        int cols = (int) shape.get(1);
        Mat mat = new Mat(rows, cols, cvType);
        mat.put(0, 0, data);
        return mat;
    }

    public static Mat floatNDArrayToMat(NDArray ndArray) {
        return floatNDArrayToMat(ndArray, CvType.CV_32F);
    }

    public static Mat uint8NDArrayToMat(NDArray ndArray) {
        byte[] data = ndArray.toByteArray();
        Shape shape = ndArray.getShape();
        int rows = (int) shape.get(0);
        int cols = (int) shape.get(1);
        Mat mat = new Mat(rows, cols, CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    public static Mat toMat(List<Point> points) {
        if (points == null || points.isEmpty()) {
            return new Mat();
        }
        float[] data = new float[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            data[i * 2] = (float) points.get(i).getX();
            data[i * 2 + 1] = (float) points.get(i).getY();
        }
        Mat mat = new Mat(points.size(), 1, CvType.CV_32SC2);
        mat.put(0, 0, data);
        return mat;
    }

    public static DetectedObjects buildEmptyDetectedObjects() {
        return new DetectedObjects(List.of(), List.of(), List.of());
    }

    public static boolean hasSupportedProtocol(String uri) {
        if (uri == null) {
            return false;
        }
        for (String protocol : SUPPORTED_PROTOCOLS) {
            if (uri.startsWith(protocol)) {
                return true;
            }
        }
        return false;
    }
}
