package com.sandy.chroma.ollama;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataLoaderService {

    @Autowired
    private ChromaVectorStore vectorStore;

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        // 加载 PDF 文件
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:GBT_38899-2020.pdf",
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());
        // 读取 PDF 内容为 Document 列表
        List<Document> documents = pdfReader.read();
        System.out.println("Documents size: " + documents.size());
        for (Document doc : documents) {
            System.out.println("Content: [" + doc.getText() + "]");
        }
        // 将文档存入 Chroma 向量数据库
        vectorStore.add(documents);
        System.out.println("PDF 已加载并存储到 Chroma 向量数据库中");
    }
}
