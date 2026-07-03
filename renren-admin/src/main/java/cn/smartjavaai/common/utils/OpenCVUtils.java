package cn.smartjavaai.common.utils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

public class OpenCVUtils {
    public OpenCVUtils() {
    }

    public static Mat uint8ArrayToMat(byte[][] array) {
        if (array == null || array.length == 0) {
            return new Mat();
        }
        int rows = array.length;
        int cols = array[0].length;
        byte[] data = new byte[rows * cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array[i], 0, data, i * cols, cols);
        }
        Mat mat = new Mat(rows, cols, CvType.CV_8UC1);
        mat.put(0, 0, data);
        return mat;
    }

    public static MatOfPoint matToMatOfPoint(Mat mat) {
        MatOfPoint matOfPoint = new MatOfPoint();
        if (mat != null && !mat.empty()) {
            int rows = mat.rows();
            Point[] points = new Point[rows];
            for (int i = 0; i < rows; i++) {
                points[i] = new Point(mat.get(i, 0)[0], mat.get(i, 0)[1]);
            }
            matOfPoint.fromArray(points);
        }
        return matOfPoint;
    }
}
