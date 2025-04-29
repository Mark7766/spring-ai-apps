package com.sandy.prototype.design;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/design")
@Slf4j
public class DesignController {
    private final DesignService designService;
    private final TemplateService templateService;
    private final GeneratedDesignRepository generatedDesignRepository;

    public DesignController(DesignService designService, TemplateService templateService, GeneratedDesignRepository generatedDesignRepository) {
        this.designService = designService;
        this.templateService = templateService;
        this.generatedDesignRepository = generatedDesignRepository;
    }

    @GetMapping
    public String showDesignForm(Model model) {
        log.info("Received GET request to show design form");
        List<Template> templates = templateService.getAllTemplates();
        // Map templates to include their content
        List<TemplateWithContent> templatesWithContent = templates.stream()
                .map(template -> {
                    String content = "";
                    try {
                        if ("html".equalsIgnoreCase(template.getType())) {
                            content = Files.readString(Path.of(template.getFilePath()));
                        }
                    } catch (Exception e) {
                        log.error("Error reading file content for template {}: {}", template.getName(), e.getMessage());
                    }
                    return new TemplateWithContent(template, content);
                })
                .collect(Collectors.toList());

        model.addAttribute("templates", templatesWithContent);
        log.info("Returning design form template: design");
        return "design";
    }

    @PostMapping("/generate")
    public String generateDesign(@RequestParam("templateId") Long templateId,
                                 @RequestParam("requirements") String requirements,
                                 @RequestParam("prototypeChatModel") PrototypeChatModel prototypeChatModel,
                                 Model model) {
        log.info("Received POST request to generate design with parameters: templateId={}, requirements={}",
                templateId, requirements);

        try {
            String generatedDesign = designService.generateDesign(templateId, requirements,prototypeChatModel);
            GeneratedDesign savedDesign = generatedDesignRepository.save(new GeneratedDesign(generatedDesign));
            log.info("Design generation successful, saved with UUID={}", savedDesign.getUuid());
            model.addAttribute("generatedDesign", generatedDesign);
            model.addAttribute("designUuid", savedDesign.getUuid());
            log.info("Returning generated design template: generated_design");
            return "generated_design";
        } catch (IOException e) {
            log.error("Failed to generate design for templateId={}: {}", templateId, e.getMessage(), e);
            model.addAttribute("error", "Failed to generate design: " + e.getMessage());
            log.info("Returning design form with error message");
            return "design";
        }
    }

    @PostMapping("/adjust")
    @ResponseBody
    public ResponseEntity<?> adjustDesign(@RequestBody AdjustDesignRequest request) {
        log.info("Received POST request to adjust design with adjustments={}, uuid={}",
                request.getAdjustments(), request.getDesignUuid());

        try {
            String adjustedDesign = designService.adjustDesign(request.getCurrentDesign(), request.getAdjustments(),request.getPrototypeChatModel());
            GeneratedDesign design = generatedDesignRepository.findByUuid(request.getDesignUuid())
                    .orElseThrow(() -> new IllegalArgumentException("Design not found for UUID: " + request.getDesignUuid()));
            design.setHtmlContent(adjustedDesign);
            generatedDesignRepository.save(design);
            log.info("Design adjustment successful, updated UUID={}", design.getUuid());
            AdjustDesignResponse response = AdjustDesignResponse.generatedDesign(adjustedDesign);
            response.setDesignUuid(design.getUuid());
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            log.error("Failed to adjust design: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(AdjustDesignResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/view/{uuid}")
    public ResponseEntity<String> viewDesign(@PathVariable String uuid) {
        log.info("Received GET request to view design with UUID={}", uuid);
        return generatedDesignRepository.findByUuid(uuid)
                .map(design -> {
                    log.info("Returning design content for UUID={}", uuid);
                    return ResponseEntity.ok()
                            .header("Content-Type", "text/html")
                            .body(design.getHtmlContent());
                })
                .orElseGet(() -> {
                    log.error("Design not found for UUID={}", uuid);
                    return ResponseEntity.notFound().build();
                });
    }

    // DTO for request
    @Data
    public static class AdjustDesignRequest {
        private String currentDesign;
        private String adjustments;
        private String designUuid;
        private PrototypeChatModel prototypeChatModel;

    }

    // DTO for response
    public static class AdjustDesignResponse {
        private String generatedDesign;
        private String error;
        private String designUuid;

        public static AdjustDesignResponse generatedDesign(String generatedDesign) {
            AdjustDesignResponse response = new AdjustDesignResponse();
            response.generatedDesign = generatedDesign;
            return response;
        }

        public static AdjustDesignResponse error(String error) {
            AdjustDesignResponse response = new AdjustDesignResponse();
            response.error = error;
            return response;
        }

        public String getGeneratedDesign() { return generatedDesign; }
        public void setGeneratedDesign(String generatedDesign) { this.generatedDesign = generatedDesign; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getDesignUuid() { return designUuid; }
        public void setDesignUuid(String designUuid) { this.designUuid = designUuid; }
    }
    // Inner class to hold template and its content
    public static class TemplateWithContent {
        private final Template template;
        private final String content;

        public TemplateWithContent(Template template, String content) {
            this.template = template;
            this.content = content;
        }

        public Template getTemplate() {
            return template;
        }

        public String getContent() {
            return content;
        }
    }
}