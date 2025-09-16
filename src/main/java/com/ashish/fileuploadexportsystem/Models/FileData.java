package com.ashish.fileuploadexportsystem.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class FileData { // Entity that stores file metadata and extracted text content

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fileName; // original file name
    private String fileType; // logical type: "txt", "csv", "excel", "pdf", "unknown"

    @Column(columnDefinition = "TEXT")
    private String extractedData; // textual content extracted from file

    private LocalDateTime uploadedAt; // timestamp of upload
}
