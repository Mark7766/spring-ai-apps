package com.sandy.newston;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test the Agent
 */
@RestController
@RequestMapping("/newston")
public class HackerNewsController {
    @Autowired
    private HackerNewsProcessor hackerNewsProcessor;
    @GetMapping("/start")
    public String start() {
        hackerNewsProcessor.process();
        return "success";
    }
}
