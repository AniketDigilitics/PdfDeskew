package com.example.pdf_deskew;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_core;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageUtils {

    // Convert BufferedImage → Mat
    public static Mat bufferedImageToMat(BufferedImage bi) {
        if (bi.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage converted = new BufferedImage(
                    bi.getWidth(),
                    bi.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR
            );
            converted.getGraphics().drawImage(bi, 0, 0, null);
            bi = converted;
        }

        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), opencv_core.CV_8UC3);
        mat.data().put(data);

        return mat;
    }

}