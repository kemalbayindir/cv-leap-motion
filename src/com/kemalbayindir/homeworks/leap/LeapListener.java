package com.kemalbayindir.homeworks.leap;

import com.kemalbayindir.homeworks.common.ImagePanel;
import com.leapmotion.leap.*;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Kemal BAYINDIR on 1/5/2016.
 */
public class LeapListener extends Listener {

    JFrame frm;
    ImagePanel pnl;


    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");

        frm = new JFrame();
        pnl = new ImagePanel();
        frm.setContentPane(pnl);
        frm.setSize(new Dimension(1024, 768));
        frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frm.setVisible(true);

        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
        //controller.enableGesture(Gesture.Type.TYPE_INVALID);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
    }

    public void onDisConnect(Controller controller) {
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }

    public void onFrame(Controller controller) {
        com.leapmotion.leap.Frame frame = controller.frame();
        //InteractionBox interactionBox = frame.interactionBox();
        //Vector point = ;
        //Vector normalizedCoordinates = interactionBox.normalizePoint(point);
        //Vector denormalizedCoordinates = interactionBox.denormalizePoint(normalizedCoordinates);
        frame.interactionBox().denormalizePoint(new Vector(300, 300, -250));

        // denormalizedCoordinates == point
        if(frame.isValid()) {
            try {
                ImageList images = frame.images();
                for(int imgId = 1; imgId < 2; imgId++) {
                    com.leapmotion.leap.Image image = images.get(imgId);


                    BufferedImage bi = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_3BYTE_BGR);
                    byte[] imageData = image.data();

                    int i = 0, r, g, b;

                    for (int y = 0; y < image.height(); y++) {
                        for (int x = 0; x < image.width(); x++) {

                            r = (imageData[i] & 0xFF) << 64;
                            g = (imageData[i] & 0xFF) << 32;
                            b = (imageData[i] & 0xFF);
                            bi.setRGB(x, y, r | g | b);

                            i++;
                        }
                    }

                    pnl.refresh(calculateConvexAndHull(bi));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static int CANNY_TRESHOLD_1             = 100;
    static int CANNY_TRESHOLD_2             = 300;
    static int apertureSize                 = 5;
    public BufferedImage calculateConvexAndHull(BufferedImage tempImg) throws IOException {

        //String basename = "C://hand";
        //File f = new File(basename + ".jpg");
        Mat colorful_image = matify(tempImg);//Mat.zeros(tempImg.getWidth(), tempImg.getHeight(), CvType.CV_8UC3);//Highgui.imread(f.getAbsolutePath());
        //byte[] pixels = ((DataBufferByte) tempImg.getRaster().getDataBuffer()).getData();
        //colorful_image.put(0, 0, pixels);

        Mat gray_image = new Mat();
        Mat canny_image = new Mat();
        Mat hierarcy = new Mat();
        MatOfInt hull = new MatOfInt();
        MatOfPoint hullPointMat = new MatOfPoint();
        java.util.List<MatOfPoint> hullPoints = new ArrayList<>();
        java.util.List<org.opencv.core.Point> hullPointList = new ArrayList<>();
        java.util.List<MatOfPoint> contours = new ArrayList<>();
        MatOfInt4 mConvexityDefectsMatOfInt4 = new MatOfInt4();

        Imgproc.cvtColor(colorful_image, gray_image, Imgproc.COLOR_RGB2GRAY);
        //Highgui.imwrite(basename + "_gray.jpg", gray_image);
        Imgproc.Canny(gray_image, canny_image, CANNY_TRESHOLD_1, CANNY_TRESHOLD_2, apertureSize, false);
        //Highgui.imwrite(basename + "_canny.jpg", canny_image);

        Imgproc.findContours(canny_image, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CV_CLOCKWISE);//CHAIN_APPROX_SIMPLE CV_COMP_BHATTACHARYYA
        //Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);

        //System.out.println("point size ==>" + contours.size());

        Mat zzzzz = Mat.zeros( canny_image.size(), CvType.CV_8UC3);
        colorful_image.copyTo(zzzzz);
        ArrayList<org.opencv.core.Point> points = new ArrayList<>();

        for (int k = 0; k < contours.size(); k++) {

            Imgproc.convexHull(contours.get(k), hull);

            points.addAll(contours.get(k).toList());

            for(int j=0; j < hull.toList().size(); j++)
                hullPointList.add(contours.get(k).toList().get(hull.toList().get(j)));

            hullPointMat.fromList(hullPointList);
            hullPoints.add(hullPointMat);

            mConvexityDefectsMatOfInt4 = new MatOfInt4();

            if(hull.toList().size()> 20 /*.rows() >= 20*/) {
                Imgproc.convexityDefects(contours.get(k), hull, mConvexityDefectsMatOfInt4);

                java.util.List<Integer> cdList = mConvexityDefectsMatOfInt4.toList();
                org.opencv.core.Point data[] = contours.get(k).toArray();
                org.opencv.core.Rect rect = Imgproc.boundingRect(contours.get(k));
                int meanLength = (rect.height + rect.width) / 8;
                int numFingers = 0;
                for (int j = 0; j < cdList.size(); j = j + 4) {
                    org.opencv.core.Point start = data[cdList.get(j)];
                    org.opencv.core.Point end = data[cdList.get(j + 1)];
                    org.opencv.core.Point defect = data[cdList.get(j + 2)];
                    org.opencv.core.Point depth = data.length > cdList.get(j+3) ? data[cdList.get(j+3)] : null;


                    //if (depth > 20) {
                        //Core.circle(zzzzz, start, 2, new Scalar(177, 255, 177), 1);
                        //Core.circle(zzzzz, end, 2, new Scalar(177, 255, 177), 1);

                    if (depth != null &&
                        Math.sqrt( (start.x - depth.x)*(start.x - depth.x) + (start.y - depth.y)*(start.y - depth.y)) > meanLength
                        ) {
                        numFingers++;
                        Core.circle(zzzzz, defect, 5, new Scalar(177, 255, 177), 1);
                    }
                    Core.rectangle(zzzzz, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(177, 177, 177));
                    System.out.println(numFingers);
                }



            }
        }

        //Imgproc.drawContours(zzzzz, hullPoints, 1, new Scalar(255, 177, 0, 255), 1);
        MatOfByte bytemat = new MatOfByte();
        Highgui.imencode(".jpg", zzzzz, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);

        BufferedImage result = ImageIO.read(in);
        for (org.opencv.core.Point point : points)
            result.setRGB((int)point.x, (int)point.y, 16711680);

        return result;//tempImg;//
    }

    public Mat matify(BufferedImage im) {
        // Convert INT to BYTE
        //im = new BufferedImage(im.getWidth(), im.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
        // Convert bufferedimage to byte array
        byte[] pixels = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();

        // Create a Matrix the same size of image
        Mat image = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
        // Fill Matrix with image values
        image.put(0, 0, pixels);

        return image;

    }

}
