package com.ashish.fileuploadexportsystem.Services;


import com.ashish.fileuploadexportsystem.Models.FileData;
import com.ashish.fileuploadexportsystem.Repositorys.FileDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileProcessingService { // Handles saving and extracting data from files

    private final FileDataRepository fileDataRepository;
    private final ObjectMapper objectMapper;

    /**
     * Save an uploaded file, extract text depending on type, and persist metadata.
     */
    public FileData saveUploadedFile(MultipartFile file) throws IOException, CsvException {
        String originalName = file.getOriginalFilename();
        String logicalType = determineFileType(originalName);
        String extractedText = extractDataFromFile(file, logicalType);

        FileData fileData = new FileData();
        fileData.setFileName(originalName != null ? originalName : "unknown");
        fileData.setFileType(logicalType);
        fileData.setExtractedData(extractedText);
        fileData.setUploadedAt(LocalDateTime.now());

        return fileDataRepository.save(fileData);
    }

    /**
     * Return all stored files
     */
    public List<FileData> getAllFiles() {
        return fileDataRepository.findAll();
    }

    /**
     * Find a file by id
     */
    public Optional<FileData> getFileById(Integer id) {
        return fileDataRepository.findById(id);
    }

    /**
     * Export one file as JSON
     */
    public byte[] exportFileAsJsonBytes(Integer id) throws IOException {
        FileData fileData = fileDataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File with id " + id + " not found"));
        return objectMapper.writeValueAsBytes(fileData);
    }

    /**
     * Export one file as CSV
     */
    public byte[] exportFileAsCsvBytes(Integer id) throws IOException {
        FileData fileData = fileDataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File with id " + id + " not found"));

        String content;
        if (fileData.getExtractedData() == null) {
            content = "";
        } else {
            content = fileData.getExtractedData();
        }


        if (content.contains("\n") && content.contains(",")) {
            return content.getBytes();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("content\n");
        sb.append("\"").append(content.replace("\"", "\"\"")).append("\"\n");
        return sb.toString().getBytes();
    }

    // ---------------- Helper methods ---------------- //

    private String determineFileType(String fileName) {
        if (fileName == null) return "unknown";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".csv")) return "csv";
        if (lower.endsWith(".txt")) return "txt";
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) return "excel";
        if (lower.endsWith(".pdf")) return "pdf";
        return "unknown";
    }

    private String extractDataFromFile(MultipartFile file, String fileType) throws IOException, CsvException {
        return switch (fileType) {
            case "csv" -> extractCsv(file);
            case "txt" -> extractText(file);
            case "excel" -> extractExcel(file);
            case "pdf" -> extractPdf(file);
            default -> new String(file.getBytes());
        };
    }

    private String extractText(MultipartFile file) throws IOException {
        return new String(file.getBytes());
    }

    private String extractCsv(MultipartFile file) throws IOException, CsvException {
        try (InputStream in = file.getInputStream();
             CSVReader csvReader = new CSVReader(new InputStreamReader(in))) {

            List<String[]> allRows = csvReader.readAll();

            StringBuilder sb = new StringBuilder();
            for (String[] row : allRows) {
                sb.append(String.join(",", row)).append("\n");
            }
            return sb.toString();
        }
    }


    private String extractPdf(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream();
             PDDocument document = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractExcel(MultipartFile file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                int lastCellIndex = row.getLastCellNum();
                for (int i = 0; i < lastCellIndex; i++) {
                    if (i > 0) sb.append(",");
                    if (row.getCell(i) != null) {
                        sb.append(row.getCell(i).toString());
                    }
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }


}
