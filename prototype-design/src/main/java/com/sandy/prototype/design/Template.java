package com.sandy.prototype.design;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path")
    private String filePath;

    private String name;

    private String type;
}