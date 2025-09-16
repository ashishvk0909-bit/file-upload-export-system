# File Upload & Export System 📁➡️📤

A simple, pragmatic backend service for uploading files, extracting their textual content, storing metadata, and exporting stored file data as JSON or CSV 


---

## 🚀 What it does
This service lets you:

- Upload one or more files (`txt`, `csv`, `xlsx`/`xls`, `pdf`, and fallback for unknown types).
- Extract text content from uploaded files (CSV/Excel rows, PDF text, plain text).
- Persist file metadata and extracted content (`FileData` entity).
- List files and retrieve metadata.
- Export a stored file's data as **JSON** or **CSV** for download.

Perfect for building lightweight ETL flows, quick document archives, or a file ingestion microservice.


---

## ✨ Features
- Multi-file upload (`multipart/form-data`).
- Automatic file-type detection (by filename extension).
- Content extraction:
  - CSV → rows joined by commas
  - Excel → first sheet parsed row by row
  - PDF → text extraction via PDFBox
  - TXT → raw text
  - Unknown → raw bytes as fallback
- Persisted metadata: filename, logical type, extracted text, upload timestamp.
- Export endpoints:
  - JSON export of the `FileData` entity
  - CSV export of the extracted content

---

## ⚙️ Tech stack
- **Java 17+**
- **Spring Boot** (REST API + DI)
- **Spring Data JPA / Hibernate**
- **MySQL** (configurable DB)
- **OpenCSV** (CSV parsing)
- **Apache POI** (Excel parsing)
- **PDFBox** (PDF text extraction)
- **Jackson** (JSON serialization)
- **Lombok** (boilerplate reduction)

---

## 🗄️ Data model
`FileData` entity contains:

- `id` (Integer, PK)
- `fileName` (String)
- `fileType` (String) — `csv`, `txt`, `excel`, `pdf`, or `unknown`
- `extractedData` (TEXT) — extracted text content
- `uploadedAt` (LocalDateTime)

---

## 🌐 Endpoints
All endpoints are rooted at `/api/files`.

| Method | Endpoint                      | Description |
|--------|--------------------------------|-------------|
| `POST` | `/api/files/upload`            | Upload one or more files (form-data key: `files`) |
| `GET`  | `/api/files`                   | List all uploaded files (metadata only) |
| `GET`  | `/api/files/{id}`              | Retrieve single file metadata by `id` |
| `GET`  | `/api/files/{id}/export/json`  | Download file metadata & content as JSON |
| `GET`  | `/api/files/{id}/export/csv`   | Download extracted file content as CSV |

---

## 💻 Usage examples (curl)

Upload one or more files:
```bash
curl -v -X POST "http://localhost:8080/api/files/upload" \
  -H "Accept: application/json" \
  -F "files=@/path/to/file.pdf" \
  -F "files=@/path/to/spreadsheet.xlsx"
```

List files:
```
curl "http://localhost:8080/api/files"
```

Get metadata for file id 1:
```
curl "http://localhost:8080/api/files/1"
```

Download file 1 as JSON:
```
curl -OJ "http://localhost:8080/api/files/1/export/json"
```

Download file 1 as CSV:
```
curl -OJ "http://localhost:8080/api/files/1/export/csv"
```

📄 Example JSON response
```
{
  "id": 1,
  "fileName": "report.pdf",
  "fileType": "pdf",
  "extractedData": "Full text extracted from the PDF...",
  "uploadedAt": "2025-09-16T10:15:30"
}
```

## 🛠️ How to run locally

Clone the repo:
```
git clone <your-repo-url>
cd file-upload-export-system
```

Configure DB in application.properties:
```
spring.datasource.url=jdbc:mysql://localhost:3306/filedb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Build & run:
```
./mvnw clean package
java -jar target/file-upload-export-system-0.0.1-SNAPSHOT.jar
```

Or run directly via your IDE.
