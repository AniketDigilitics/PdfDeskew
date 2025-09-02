package com.example.pdf_deskew.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class PdfRenderService {

    public List<byte[]> renderPdfToImages(byte[] pdfBytes) throws IOException {
        List<byte[]> images = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfBytes)) { // load from bytes
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 150); // 150 DPI
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                images.add(baos.toByteArray());
            }
        }

        return images;
    }
}
