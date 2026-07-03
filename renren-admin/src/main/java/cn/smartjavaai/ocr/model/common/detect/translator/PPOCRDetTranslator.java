package cn.smartjavaai.ocr.model.common.detect.translator;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import cn.smartjavaai.common.utils.DJLCommonUtils;
import cn.smartjavaai.common.utils.OpenCVUtils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PPOCRDetTranslator implements Translator<Image, NDList> {
    private final float thresh = 0.3f;
    private final boolean use_dilation = false;
    private final String score_mode = "fast";
    private final String box_type = "quad";

    private final int limit_side_len;
    private final int max_candidates;
    private final int min_size;
    private final float box_thresh;
    private final float unclip_ratio;
    private float ratio_h;
    private float ratio_w;
    private int img_height;
    private int img_width;

    private String batchifier;

    public PPOCRDetTranslator(Map<String, ?> arguments) {
        limit_side_len =
                arguments.containsKey("limit_side_len")
                        ? Integer.parseInt(arguments.get("limit_side_len").toString())
                        : 960;
        max_candidates =
                arguments.containsKey("max_candidates")
                        ? Integer.parseInt(arguments.get("max_candidates").toString())
                        : 1000;
        min_size =
                arguments.containsKey("min_size")
                        ? Integer.parseInt(arguments.get("min_size").toString())
                        : 3;
        box_thresh =
                arguments.containsKey("box_thresh")
                        ? Float.parseFloat(arguments.get("box_thresh").toString())
                        : 0.6f;
        unclip_ratio =
                arguments.containsKey("unclip_ratio")
                        ? Float.parseFloat(arguments.get("unclip_ratio").toString())
                        : 1.6f;

        batchifier = arguments.containsKey("batchifier")
                ? arguments.get("batchifier").toString()
                : "stack";
    }

    @Override
    public NDList processOutput(TranslatorContext ctx, NDList list) {
        NDManager manager = ctx.getNDManager();
        NDArray pred = list.get(0);
        pred = pred.squeeze();
        NDArray segmentation = pred.gt(thresh);

        segmentation = segmentation.toType(DataType.UINT8, true);
        Shape shape = segmentation.getShape();
        int rows = (int) shape.get(0);
        int cols = (int) shape.get(1);

        Mat newMask = new Mat();
        if (this.use_dilation) {
            Mat mask = new Mat();
            Mat srcMat = DJLCommonUtils.uint8NDArrayToMat(segmentation);
            Mat dilation_kernel = OpenCVUtils.uint8ArrayToMat(new byte[][]{{1, 1}, {1, 1}});
            Imgproc.dilate(srcMat, mask, dilation_kernel);
            Scalar scalar = new Scalar(255);
            Core.multiply(mask, scalar, newMask);
            mask.release();
            srcMat.release();
            dilation_kernel.release();
        } else {
            Mat srcMat = DJLCommonUtils.uint8NDArrayToMat(segmentation);
            Scalar scalar = new Scalar(255);
            Core.multiply(srcMat, scalar, newMask);
            srcMat.release();
        }

        NDArray boxes = boxes_from_bitmap(manager, pred, newMask);

        NDArray boxes1 = boxes.get(":, :, 0").div(ratio_w);
        boxes.set(new NDIndex(":, :, 0"), boxes1);
        NDArray boxes2 = boxes.get(":, :, 1").div(ratio_h);
        boxes.set(new NDIndex(":, :, 1"), boxes2);

        NDList dt_boxes = this.filter_tag_det_res(boxes);

        dt_boxes.detach();

        newMask.release();

        return dt_boxes;
    }

    private NDList filter_tag_det_res(NDArray dt_boxes) {
        NDList boxesList = new NDList();

        int num = (int) dt_boxes.getShape().get(0);
        for (int i = 0; i < num; i++) {
            NDArray box = dt_boxes.get(i);
            box = order_points_clockwise(box);
            box = clip_det_res(box);
            float[] box0 = box.get(0).toFloatArray();
            float[] box1 = box.get(1).toFloatArray();
            float[] box3 = box.get(3).toFloatArray();
            int rect_width = (int) Math.sqrt(Math.pow(box1[0] - box0[0], 2) + Math.pow(box1[1] - box0[1], 2));
            int rect_height = (int) Math.sqrt(Math.pow(box3[0] - box0[0], 2) + Math.pow(box3[1] - box0[1], 2));
            if (rect_width <= 3 || rect_height <= 3)
                continue;
            boxesList.add(box);
        }

        return boxesList;
    }

    private NDArray clip_det_res(NDArray points) {
        for (int i = 0; i < points.getShape().get(0); i++) {
            int value = Math.max((int) points.get(i, 0).toFloatArray()[0], 0);
            value = Math.min(value, img_width - 1);
            points.set(new NDIndex(i + ",0"), value);
            value = Math.max((int) points.get(i, 1).toFloatArray()[0], 0);
            value = Math.min(value, img_height - 1);
            points.set(new NDIndex(i + ",1"), value);
        }

        return points;
    }

    private NDArray order_points_clockwise(NDArray pts) {
        NDList list = new NDList();
        long[] indexes = pts.get(":, 0").argSort().toLongArray();

        Shape s1 = pts.getShape();
        NDArray leftMost1 = pts.get(indexes[0] + ",:");
        NDArray leftMost2 = pts.get(indexes[1] + ",:");
        NDArray leftMost = leftMost1.concat(leftMost2).reshape(2, 2);
        NDArray rightMost1 = pts.get(indexes[2] + ",:");
        NDArray rightMost2 = pts.get(indexes[3] + ",:");
        NDArray rightMost = rightMost1.concat(rightMost2).reshape(2, 2);

        indexes = leftMost.get(":, 1").argSort().toLongArray();
        NDArray lt = leftMost.get(indexes[0] + ",:");
        NDArray lb = leftMost.get(indexes[1] + ",:");
        indexes = rightMost.get(":, 1").argSort().toLongArray();
        NDArray rt = rightMost.get(indexes[0] + ",:");
        NDArray rb = rightMost.get(indexes[1] + ",:");

        list.add(lt);
        list.add(rt);
        list.add(rb);
        list.add(lb);

        NDArray rect = NDArrays.concat(list).reshape(4, 2);
        return rect;
    }

    private NDArray boxes_from_bitmap(NDManager manager, NDArray pred, Mat bitmap) {
        int dest_height = (int) pred.getShape().get(0);
        int dest_width = (int) pred.getShape().get(1);
        int height = bitmap.rows();
        int width = bitmap.cols();

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
                bitmap,
                contours,
                hierarchy,
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);

        int num_contours = Math.min(contours.size(), max_candidates);
        NDList boxList = new NDList();
        float[] scores = new float[num_contours];

        for (int index = 0; index < num_contours; index++) {
            MatOfPoint contour = contours.get(index);
            MatOfPoint2f newContour = new MatOfPoint2f(contour.toArray());
            float[][] pointsArr = new float[4][2];
            int sside = get_mini_boxes(newContour, pointsArr);
            if (sside < this.min_size)
                continue;
            NDArray points = manager.create(pointsArr);
            float score = box_score_fast(manager, pred, points);
            if (score < this.box_thresh)
                continue;

            NDArray box = unclip(manager, points);

            NDArray boxes1 = box.get(":,0").div(width).mul(dest_width).round().clip(0, dest_width);
            box.set(new NDIndex(":, 0"), boxes1);
            NDArray boxes2 = box.get(":,1").div(height).mul(dest_height).round().clip(0, dest_height);
            box.set(new NDIndex(":, 1"), boxes2);

            boxList.add(box);
            scores[index] = score;

            contour.release();
            newContour.release();
        }

        NDArray boxes = NDArrays.stack(boxList);

        hierarchy.release();

        return boxes;
    }

    private NDArray unclip(NDManager manager, NDArray points) {
        points = order_points_clockwise(points);
        float[] pointsArr = points.toFloatArray();
        float[] lt = java.util.Arrays.copyOfRange(pointsArr, 0, 2);
        float[] lb = java.util.Arrays.copyOfRange(pointsArr, 6, 8);

        float[] rt = java.util.Arrays.copyOfRange(pointsArr, 2, 4);
        float[] rb = java.util.Arrays.copyOfRange(pointsArr, 4, 6);

        float width = distance(lt, rt);
        float height = distance(lt, lb);

        if (width > height) {
            float k = (lt[1] - rt[1]) / (lt[0] - rt[0]);

            float delta_dis = height;
            float delta_x = (float) Math.sqrt((delta_dis * delta_dis) / (k * k + 1));
            float delta_y = Math.abs(k * delta_x);

            if (k > 0) {
                pointsArr[0] = lt[0] - delta_x + delta_y;
                pointsArr[1] = lt[1] - delta_y - delta_x;
                pointsArr[2] = rt[0] + delta_x + delta_y;
                pointsArr[3] = rt[1] + delta_y - delta_x;

                pointsArr[4] = rb[0] + delta_x - delta_y;
                pointsArr[5] = rb[1] + delta_y + delta_x;
                pointsArr[6] = lb[0] - delta_x - delta_y;
                pointsArr[7] = lb[1] - delta_y + delta_x;
            } else {
                pointsArr[0] = lt[0] - delta_x - delta_y;
                pointsArr[1] = lt[1] + delta_y - delta_x;
                pointsArr[2] = rt[0] + delta_x - delta_y;
                pointsArr[3] = rt[1] - delta_y - delta_x;

                pointsArr[4] = rb[0] + delta_x + delta_y;
                pointsArr[5] = rb[1] - delta_y + delta_x;
                pointsArr[6] = lb[0] - delta_x + delta_y;
                pointsArr[7] = lb[1] + delta_y + delta_x;
            }
        } else {
            float k = (lt[1] - rt[1]) / (lt[0] - rt[0]);

            float delta_dis = width;
            float delta_y = (float) Math.sqrt((delta_dis * delta_dis) / (k * k + 1));
            float delta_x = Math.abs(k * delta_y);

            if (k > 0) {
                pointsArr[0] = lt[0] + delta_x - delta_y;
                pointsArr[1] = lt[1] - delta_y - delta_x;
                pointsArr[2] = rt[0] + delta_x + delta_y;
                pointsArr[3] = rt[1] - delta_y + delta_x;

                pointsArr[4] = rb[0] - delta_x + delta_y;
                pointsArr[5] = rb[1] + delta_y + delta_x;
                pointsArr[6] = lb[0] - delta_x - delta_y;
                pointsArr[7] = lb[1] + delta_y - delta_x;
            } else {
                pointsArr[0] = lt[0] - delta_x - delta_y;
                pointsArr[1] = lt[1] - delta_y + delta_x;
                pointsArr[2] = rt[0] - delta_x + delta_y;
                pointsArr[3] = rt[1] - delta_y - delta_x;

                pointsArr[4] = rb[0] + delta_x + delta_y;
                pointsArr[5] = rb[1] - delta_y + delta_x;
                pointsArr[6] = lb[0] + delta_x - delta_y;
                pointsArr[7] = lb[1] + delta_y + delta_x;
            }
        }
        points = manager.create(pointsArr).reshape(4, 2);

        return points;
    }

    private float distance(float[] point1, float[] point2) {
        float disX = point1[0] - point2[0];
        float disY = point1[1] - point2[1];
        float dis = (float) Math.sqrt(disX * disX + disY * disY);
        return dis;
    }

    private int get_mini_boxes(MatOfPoint2f contour, float[][] pointsArr) {
        RotatedRect rect = Imgproc.minAreaRect(contour);
        Mat points = new Mat();
        Imgproc.boxPoints(rect, points);

        float[][] fourPoints = new float[4][2];
        for (int row = 0; row < 4; row++) {
            fourPoints[row][0] = (float) points.get(row, 0)[0];
            fourPoints[row][1] = (float) points.get(row, 1)[0];
        }

        float[] tmpPoint = new float[2];
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (fourPoints[j][0] < fourPoints[i][0]) {
                    tmpPoint[0] = fourPoints[i][0];
                    tmpPoint[1] = fourPoints[i][1];
                    fourPoints[i][0] = fourPoints[j][0];
                    fourPoints[i][1] = fourPoints[j][1];
                    fourPoints[j][0] = tmpPoint[0];
                    fourPoints[j][1] = tmpPoint[1];
                }
            }
        }

        int index_1 = 0;
        int index_2 = 1;
        int index_3 = 2;
        int index_4 = 3;

        if (fourPoints[1][1] > fourPoints[0][1]) {
            index_1 = 0;
            index_4 = 1;
        } else {
            index_1 = 1;
            index_4 = 0;
        }

        if (fourPoints[3][1] > fourPoints[2][1]) {
            index_2 = 2;
            index_3 = 3;
        } else {
            index_2 = 3;
            index_3 = 2;
        }

        pointsArr[0] = fourPoints[index_1];
        pointsArr[1] = fourPoints[index_2];
        pointsArr[2] = fourPoints[index_3];
        pointsArr[3] = fourPoints[index_4];

        int height = rect.boundingRect().height;
        int width = rect.boundingRect().width;
        int sside = Math.min(height, width);

        points.release();

        return sside;
    }

    private float box_score_fast(NDManager manager, NDArray bitmap, NDArray points) {
        NDArray box = points.get(":");
        long h = bitmap.getShape().get(0);
        long w = bitmap.getShape().get(1);
        int xmin = box.get(":, 0").min().floor().clip(0, w - 1).toType(DataType.INT32, true).toIntArray()[0];
        int xmax = box.get(":, 0").max().ceil().clip(0, w - 1).toType(DataType.INT32, true).toIntArray()[0];
        int ymin = box.get(":, 1").min().floor().clip(0, h - 1).toType(DataType.INT32, true).toIntArray()[0];
        int ymax = box.get(":, 1").max().ceil().clip(0, h - 1).toType(DataType.INT32, true).toIntArray()[0];

        NDArray mask = manager.zeros(new Shape(ymax - ymin + 1, xmax - xmin + 1), DataType.UINT8);

        box.set(new NDIndex(":, 0"), box.get(":, 0").sub(xmin));
        box.set(new NDIndex(":, 1"), box.get(":, 1").sub(ymin));

        Mat maskMat = DJLCommonUtils.uint8NDArrayToMat(mask);

        Mat boxMat = DJLCommonUtils.floatNDArrayToMat(box, CvType.CV_32S);

        List<MatOfPoint> pts = new ArrayList<>();
        MatOfPoint matOfPoint = OpenCVUtils.matToMatOfPoint(boxMat);
        pts.add(matOfPoint);
        Imgproc.fillPoly(maskMat, pts, new Scalar(1));


        NDArray subBitMap = bitmap.get(ymin + ":" + (ymax + 1) + "," + xmin + ":" + (xmax + 1));
        Mat bitMapMat = DJLCommonUtils.floatNDArrayToMat(subBitMap);

        Scalar score = Core.mean(bitMapMat, maskMat);
        float scoreValue = (float) score.val[0];
        maskMat.release();
        boxMat.release();
        bitMapMat.release();

        return scoreValue;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        int h = input.getHeight();
        int w = input.getWidth();
        img_height = h;
        img_width = w;

        float ratio = 1.0f;
        if (Math.max(h, w) > limit_side_len) {
            if (h > w) {
                ratio = (float) limit_side_len / (float) h;
            } else {
                ratio = (float) limit_side_len / (float) w;
            }
        }

        int resize_h = (int) (h * ratio);
        int resize_w = (int) (w * ratio);

        resize_h = Math.round((float) resize_h / 32f) * 32;
        resize_w = Math.round((float) resize_w / 32f) * 32;

        ratio_h = resize_h / (float) h;
        ratio_w = resize_w / (float) w;

        Image resizedImage = input.resize(resize_w, resize_h, true);
        
        BufferedImage bufferedImage = null;
        Object wrapped = resizedImage.getWrappedImage();
        if (wrapped instanceof BufferedImage) {
            bufferedImage = (BufferedImage) wrapped;
        }
        if (bufferedImage == null) {
            bufferedImage = new BufferedImage(resize_w, resize_h, BufferedImage.TYPE_3BYTE_BGR);
        }

        int channels = 3;
        float[] data = new float[channels * resize_h * resize_w];
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};

        for (int c = 0; c < channels; c++) {
            for (int y = 0; y < resize_h; y++) {
                for (int x = 0; x < resize_w; x++) {
                    int pixel = bufferedImage.getRGB(x, y);
                    int b = (pixel) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int r = (pixel >> 16) & 0xFF;
                    
                    float value;
                    if (c == 0) {
                        value = (r / 255.0f - mean[0]) / std[0];
                    } else if (c == 1) {
                        value = (g / 255.0f - mean[1]) / std[1];
                    } else {
                        value = (b / 255.0f - mean[2]) / std[2];
                    }
                    data[c * resize_h * resize_w + y * resize_w + x] = value;
                }
            }
        }

        NDManager manager = ctx.getNDManager();
        NDArray img = manager.create(data, new Shape(1, channels, resize_h, resize_w));

        return new NDList(img);
    }

    @Override
    public Batchifier getBatchifier() {
        return null;
    }
}
