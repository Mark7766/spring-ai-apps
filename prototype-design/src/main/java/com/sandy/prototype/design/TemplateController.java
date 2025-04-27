package com.sandy.prototype.design;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/templates")
@Slf4j
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public String listTemplates(Model model) {
        log.info("Received GET request to list templates");
        List<Template> templates = templateService.getAllTemplates();
        // Add content for HTML templates
        List<TemplateDTO> templateDTOs = templates.stream().map(template -> {
            TemplateDTO dto = new TemplateDTO();
            dto.setId(template.getId());
            dto.setName(template.getName());
            dto.setType(template.getType());
            dto.setFilePath(template.getFilePath());
            if ("html".equals(template.getType())) {
                try {
                    dto.setContent(Files.readString(Paths.get(template.getFilePath())));
                } catch (IOException e) {
                    log.error("Failed to read content for template {}: {}", template.getId(), e.getMessage());
                    dto.setContent("");
                }
            }
            return dto;
        }).collect(Collectors.toList());
        model.addAttribute("templates", templateDTOs);
        log.info("Returning template list template: template_list");
        return "template_list";
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        log.info("Received GET request to show upload form");
        log.info("Returning upload template: upload_template");
        return "upload_template";
    }

    @PostMapping("/upload")
    public String uploadTemplate(@RequestParam("name") String name,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        log.info("Received POST request to upload template with parameters: name={}, fileName={}, fileSize={}",
                name, file.getOriginalFilename(), file.getSize());

        try {
            templateService.uploadTemplate(name, file);
            log.info("Template uploaded successfully: {}", name);
            redirectAttributes.addFlashAttribute("message", "Template uploaded successfully");
            log.info("Redirecting to template list");
            return "redirect:/templates";
        } catch (IOException e) {
            log.error("Failed to upload template '{}': {}", name, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to upload template: " + e.getMessage());
            log.info("Redirecting to upload form with error message");
            return "redirect:/templates/upload";
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewTemplate(@PathVariable Long id) throws IOException {
        log.info("Received GET request to view template with id={}", id);
        Template template = templateService.getTemplateById(id);
        byte[] content = Files.readAllBytes(Paths.get(template.getFilePath()));
        log.info("Returning template content, type={}, size={}",
                template.getType(), content.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, template.getType().equals("pdf") ? "application/pdf" : "text/html")
                .body(content);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) throws IOException {
        log.info("Received DELETE request for template with id={}", id);
        templateService.deleteTemplate(id);
        log.info("Template deleted successfully: id={}", id);
        return ResponseEntity.ok().build();
    }

    // DTO to include content
    public static class TemplateDTO {
        private Long id;
        private String name;
        private String type;
        private String filePath;
        private String content;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}