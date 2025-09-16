package com.ashish.fileuploadexportsystem.Controllers;


import com.ashish.fileuploadexportsystem.Models.FileData;
import com.ashish.fileuploadexportsystem.Services.FileProcessingService;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController { // Controller exposing file upload, listing and export endpoints

    private final FileProcessingService fileProcessingService; // injected service

    /**
     * Upload one or more files.
     * - Endpoint: POST /api/files/upload
     * - form-data key: "files" (can supply multiple)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileData>> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException, CsvException {
        List<FileData> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            FileData fileData = fileProcessingService.saveUploadedFile(file);
            saved.add(fileData);
        }
        return ResponseEntity.ok(saved);
    }

    /**
     * List all uploaded files metadata.
     * - Endpoint: GET /api/files
     */
    @GetMapping
    public ResponseEntity<List<FileData>> getAllFiles() {
        return ResponseEntity.ok(fileProcessingService.getAllFiles());
    }

    /**
     * Get single file metadata by id.
     * - Endpoint: GET /api/files/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileData> getFile(@PathVariable Integer id) {
        return fileProcessingService.getFileById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Export file data as JSON (download).
     * - Endpoint: GET /api/files/{id}/export/json
     */
    @GetMapping("/{id}/export/json")
    public ResponseEntity<byte[]> exportJson(@PathVariable Integer id) throws IOException {
        byte[] data = fileProcessingService.exportFileAsJsonBytes(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDisposition(ContentDisposition.attachment().filename("file-" + id + ".json").build());
        return ResponseEntity.ok().headers(headers).body(data);
    }

    /**
     * Export file data as CSV (download).
     * - Endpoint: GET /api/files/{id}/export/csv
     */
    @GetMapping("/{id}/export/csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Integer id) throws IOException {
        byte[] data = fileProcessingService.exportFileAsCsvBytes(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("file-" + id + ".csv").build());
        return ResponseEntity.ok().headers(headers).body(data);
    }


}
