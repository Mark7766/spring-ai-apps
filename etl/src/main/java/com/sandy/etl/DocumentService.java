package com.sandy.etl;

import org.apache.tika.config.TikaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private static final String DIRECTORY = Paths.get(System.getProperty("user.dir"), "docs").toString() + "/";

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private TokenTextSplitter textSplitter;

    public void uploadAndProcessFile(MultipartFile file) throws IOException {
        // 验证文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件为空");
        }

        // 确保 docs/ 目录存在
        Path docsDir = Paths.get(DIRECTORY);
        logger.info("目标目录: {}", docsDir.toAbsolutePath());
        if (!Files.exists(docsDir)) {
            Files.createDirectories(docsDir);
            logger.info("创建目录: {}", docsDir.toAbsolutePath());
        }

        // 保存文件到 docs/ 目录
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetFile = docsDir.resolve(fileName);
        File tmpFile = targetFile.toFile();
        logger.info("保存文件到: {}", tmpFile.getAbsolutePath());

        try {
            file.transferTo(tmpFile);
            FileUrlResource resource = new FileUrlResource(tmpFile.getAbsolutePath());
            // 验证文件是否存在
            if (!tmpFile.exists()) {
                throw new IOException("文件保存失败: " + tmpFile.getAbsolutePath());
            }

            // 根据文件类型选择 DocumentReader
            List<Document> documents;
            String lowerCaseFileName = fileName.toLowerCase();

            if (lowerCaseFileName.endsWith(".pdf")) {
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource,
                        PdfDocumentReaderConfig.builder()
                                .withPageTopMargin(0)
                                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                        .withNumberOfTopTextLinesToDelete(0)
                                        .build())
                                .withPagesPerDocument(1)
                                .build());
                documents = pdfReader.read();
            } else if (lowerCaseFileName.endsWith(".txt")) {
                TextReader reader = new TextReader(resource);
                documents = reader.read();
            } else if (lowerCaseFileName.endsWith(".doc") || lowerCaseFileName.endsWith(".docx") ||
                    lowerCaseFileName.endsWith(".ppt") || lowerCaseFileName.endsWith(".pptx")) {
                // 优化 Tika 配置
                TikaDocumentReader reader = new TikaDocumentReader(resource);
                documents = reader.read();
            } else {
                throw new IllegalArgumentException("不支持的文件类型: " + fileName);
            }
            // 验证文档内容
            if (documents.isEmpty() || documents.stream().allMatch(doc -> Objects.requireNonNull(doc.getText()).isEmpty())) {
                logger.warn("文件内容为空或无法解析: {}", fileName);
                throw new IOException("文件内容为空或无法解析: " + fileName);
            }

//            // 添加元数据（文件名、版本等）
            documents.forEach(doc -> {
                doc.getMetadata().put("filename", fileName);
                doc.getMetadata().put("version", "1");
                doc.getMetadata().put("type", "file");

            });
            /**
             *maxChunkSize=512：每个分片最大 512 个 token，适配常见嵌入模型（如 text-embedding-ada-002 的 8192 token 限制）。
             * chunkOverlap=128：分片间重叠 128 个 token，保留上下文。
             * minChunkSize=100：最小分片 100 个 token，避免过小分片。
             * maxChunkCharCount=100000：限制单文档字符数，防止超大文档。
             * keepSeparator=true：保留换行符，保持文档结构。
             * */
            TokenTextSplitter splitter = new TokenTextSplitter(
                    512, // 最大分片大小
                    128, // 重叠大小
                    100, // 最小分片大小
                    100000, // 最大文档大小
                    true // 保留换行符
            );
            List<Document> splitDocuments = splitter.split(documents);

            // 存储到 Chroma
            vectorStore.add(splitDocuments);
            logger.info("成功处理文件: {}，分片数: {}", fileName, splitDocuments.size());
        } catch (Exception e) {
            // 清理失败的文件
            if (tmpFile.exists()) {
                Files.deleteIfExists(targetFile);
                logger.info("清理失败文件: {}", tmpFile.getAbsolutePath());
            }
            throw new IOException("处理文件失败: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileName) {
        logger.info("删除文件: {}", fileName);
        // 通过元数据查找文档
        SearchRequest searchRequest = SearchRequest.builder()
                .query("*") // 通配符查询，依赖 filterExpression 过滤
                .filterExpression(new FilterExpressionBuilder()
                        .eq("filename", fileName)
                        .build())
                .topK(1000) // 调整为合理值
                .similarityThreshold(0.0) // 忽略语义相似度
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        // 删除向量存储中的文档
        if (documents != null && !documents.isEmpty()) {
            List<String> docIds = documents.stream()
                    .map(Document::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            logger.info("找到文档: {}，共 {} 条，ID 列表: {}", fileName, docIds.size(), docIds);
            try {
                // 批量删除
                if (!docIds.isEmpty()) {
                    vectorStore.delete(docIds);
                    logger.info("从 Chroma 批量删除文档: {}，共 {} 条", fileName, docIds.size());
                }
            } catch (Exception e) {
                logger.error("从 Chroma 删除文档失败: {}", fileName, e);
                // 继续执行物理文件删除
            }
        } else {
            logger.warn("未找到与文件名 {} 相关的文档", fileName);
        }

        // 删除 docs/ 目录中的文件
        try {
            Path filePath = Paths.get(DIRECTORY, fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("删除物理文件: {}", filePath.toAbsolutePath());
            } else {
                logger.info("物理文件不存在，无需删除: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("删除物理文件失败: {}", fileName, e);
            throw new RuntimeException("删除文件失败: " + fileName, e);
        }
    }

    public List<String> listFiles() {
        SearchRequest searchRequest = SearchRequest.builder().query("*:*").topK(1000).build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        if (documents == null) {
            logger.warn("无文件记录");
            return List.of();
        }
        List<String> files = documents.stream()
                .map(doc -> (String) doc.getMetadata().get("filename"))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        logger.info("列出文件: {} 个", files.size());
        return files;
    }
}