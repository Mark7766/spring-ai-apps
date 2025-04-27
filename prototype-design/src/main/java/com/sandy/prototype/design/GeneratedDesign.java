package com.sandy.prototype.design;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class GeneratedDesign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlContent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public GeneratedDesign() {
        this.uuid = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public GeneratedDesign(String htmlContent) {
        this();
        this.htmlContent = htmlContent;
    }
}