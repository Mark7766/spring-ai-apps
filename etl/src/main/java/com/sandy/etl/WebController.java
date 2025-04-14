package com.sandy.etl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Controller
public class WebController {
    @Autowired
    private RagChatService ragChatService;
    @Autowired
    private DocumentService documentService;

    @GetMapping("/docs")
    public String docsPage(Model model) {
        List<String> files = documentService.listFiles();
        model.addAttribute("files", files);
        return "docs";
    }
    @GetMapping("/")
    public String chatPage() {
        return "chat"; // 返回chat.html
    }
}