package com.sandy.chat.ocr.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 处理服务 - 将 PDF 文件的每一页转换为图片
 */
@Slf4j
@Service
public class PdfProcessingService {

    /**
     * 将 PDF 文件的每一页转换为图片
     *
     * @param pdfFile PDF 文件
     * @param dpi 分辨率(建议 150-300)
     * @return 图片数据列表
     */
    public List<OcrChatService.ImageData> convertPdfToImages(MultipartFile pdfFile, int dpi) throws IOException {
        List<OcrChatService.ImageData> imageDataList = new ArrayList<>();

        log.info("开始处理 PDF 文件: {}, 大小: {} bytes, DPI: {}",
                pdfFile.getOriginalFilename(), pdfFile.getSize(), dpi);

        try (PDDocument document = Loader.loadPDF(pdfFile.getBytes())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();

            log.info("PDF 共有 {} 页，开始转换...", pageCount);

            for (int page = 0; page < pageCount; page++) {
                // 将 PDF 页面渲染为图片
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);

                // 转换为字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                byte[] imageBytes = baos.toByteArray();

                String filename = String.format("%s_第%d页.png",
                    pdfFile.getOriginalFilename().replaceAll("(?i)\\.pdf$", ""),
                    page + 1);

                imageDataList.add(new OcrChatService.ImageData(
                    imageBytes,
                    "image/png",
                    filename
                ));

                log.info("已转换第 {}/{} 页，图片大小: {} KB",
                        page + 1, pageCount, imageBytes.length / 1024);
            }
        } catch (IOException e) {
            log.error("转换 PDF 失败: {}", pdfFile.getOriginalFilename(), e);
            throw new IOException("PDF 转换失败: " + e.getMessage(), e);
        }

        log.info("PDF 转换完成，共生成 {} 张图片", imageDataList.size());
        return imageDataList;
    }

    /**
     * 使用默认 DPI (200) 转换 PDF
     */
    public List<OcrChatService.ImageData> convertPdfToImages(MultipartFile pdfFile) throws IOException {
        return convertPdfToImages(pdfFile, 200);
    }

    /**
     * 检查文件是否为 PDF
     */
    public boolean isPdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        return (contentType != null && contentType.equals("application/pdf")) ||
               (filename != null && filename.toLowerCase().endsWith(".pdf"));
    }
}

