package com.sandy.prototype.design;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Value("${template.storage.path}")
    private String storagePath;

    public TemplateService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    public Template uploadTemplate(String name, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String fileType = fileName.endsWith(".html") ? "html" : "pdf";
        Path filePath = Paths.get(storagePath, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        Template template = new Template();
        template.setName(name);
        template.setFilePath(filePath.toString());
        template.setType(fileType);
        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id) throws IOException {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
        Files.deleteIfExists(Paths.get(template.getFilePath()));
        templateRepository.deleteById(id);
    }

    public Template getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
    }
}