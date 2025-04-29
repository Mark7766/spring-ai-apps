package com.sandy.prototype.design;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class DesignService {
//    @Autowired
//    private ChatModel chatModel;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ChatModeService chatModeService;

    public String generateDesign(Long templateId, String requirements,PrototypeChatModel prototypeChatModel) throws IOException {
        log.info("Generating design for templateId={} with requirements={}", templateId, requirements);

        var template = templateService.getTemplateById(templateId);
        String templateContent = readTemplateContent(template.getFilePath(), template.getType());
        log.info("Template content read successfully for file: {}", template.getFilePath());

        String prompt = String.format(
                "Based on the following template content:\n\n%s\n\n" +
                        "And the user's requirements:\n%s\n\n" +
                        "Generate an HTML file that incorporates the template and meets the requirements. " +
                        "Return only the complete HTML code without any explanations.",
                templateContent, requirements
        );
        ChatModel chatModel = chatModeService.getChatModel(prototypeChatModel);
        log.info("Sending prompt to ChatModel[{}:{}], prompt length={}",chatModel.toString(),chatModel.getDefaultOptions(), prompt.length());
        String rsp = chatModel.call(prompt);
        log.info("Received response from ChatModel, response length={}", rsp.length());

        String cleanedRsp = rsp
                .replaceAll("(?s)^```html\\n(.*?)\\n```$", "$1")
                .replaceAll("(?s)^```\\n(.*?)\\n```$", "$1")
                .trim();
        log.info("Cleaned response length={}", cleanedRsp.length());

        return cleanedRsp;
    }

    public String adjustDesign(String currentDesign, String adjustments,PrototypeChatModel prototypeChatModel) {
        log.info("Adjusting design with adjustments={}", adjustments);

        String prompt = String.format(
                "Based on the following current HTML design:\n\n%s\n\n" +
                        "Apply the following adjustments as specified:\n%s\n\n" +
                        "Generate a new HTML file that incorporates the adjustments. " +
                        "Return only the complete HTML code without any explanations.",
                currentDesign, adjustments
        );
        ChatModel chatModel = chatModeService.getChatModel(PrototypeChatModel.AZURE_GPT_4O);
        log.info("Sending adjustment prompt to ChatModel[{}:{}], prompt length={}",chatModel.toString(),chatModel.getDefaultOptions(), prompt.length());

        String rsp = chatModel.call(prompt);
        log.info("Received adjustment response from ChatModel, response length={}", rsp.length());

        String cleanedRsp = rsp
                .replaceAll("(?s)^```html\\n(.*?)\\n```$", "$1")
                .replaceAll("(?s)^```\\n(.*?)\\n```$", "$1")
                .trim();
        log.info("Cleaned adjustment response length={}", cleanedRsp.length());

        return cleanedRsp;
    }

    private String readTemplateContent(String filePath, String type) throws IOException {
        log.info("Reading content for file: {}, type: {}", filePath, type);
        if ("pdf".equals(type)) {
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(filePath,
                    PdfDocumentReaderConfig.builder()
                            .withPageTopMargin(0)
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                    .withNumberOfTopTextLinesToDelete(0)
                                    .build())
                            .withPagesPerDocument(1)
                            .build());
            List<Document> documents = pdfReader.read();
            StringBuilder content = new StringBuilder();
            for (Document doc : documents) {
                content.append(doc.getFormattedContent()).append("\n");
            }
            log.info("PDF content extracted for file: {}", filePath);
            return content.toString();
        } else {
            String content = Files.readString(Paths.get(filePath));
            log.info("HTML content read for file: {}", filePath);
            return content;
        }
    }
}