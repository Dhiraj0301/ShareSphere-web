package com.example.ShareSphere.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


import com.cloudinary.Cloudinary;
import com.example.ShareSphere.entity.FileEntity;
import com.example.ShareSphere.model.FileModel;
import com.example.ShareSphere.service.FileService;
//import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
//@RestController
@RequestMapping("/files")
public class controller {

    @Autowired
    private FileService fileService;
    @Autowired
    private Cloudinary cloudinary;



    @GetMapping("/login")
    public String login() {
        return "home";
    }

    @GetMapping("/home")
    public String index(Model model) {
        model.addAttribute("files", fileService.getAllFiles());
        return "list-files";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("uploadedBy") String uploadedBy) throws IOException {
        // Upload the file and save it to the service layer
        fileService.uploadFile(file, uploadedBy);

        return "redirect:/files/home";  // Redirect to the home page after upload
    }


    @GetMapping("/share/{id}")
    public String shareFile(@PathVariable int id, Model model) {
        ResponseEntity<?> response = fileService.shareFile(id);
        if (response.hasBody()) {
            FileModel file = (FileModel) response.getBody();
            String fileType = detectFileType(file.getFileName());

            model.addAttribute("fileType", fileType);
            model.addAttribute("file", file);

            // If you want to share the Cloudinary URL, use this:
            model.addAttribute("shareUrl", file.getFileUrl());  // Cloudinary URL
            if (fileType.equals("video")) {
                model.addAttribute("videoUrl", file.getFileUrl()); // This should be the actual Cloudinary URL

            }
            return "share-files";
        } else {
            return "redirect:/files/home";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable int id) {
        ResponseEntity<?> file = fileService.deleteFile(id);
        if (file.hasBody()) {
            return "redirect:/files/home";
        } else {
            return "redirect:/files";
        }
    }

    @GetMapping("/deleteExpiredFiles")
    public String deleteExpiredFiles() {
        fileService.deleteExpiredFiles();  // Manually invoke cleanup
        return "Expired files deleted.";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(@PathVariable("id") int id) {
        return fileService.getFile(id);
    }

    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> previewFile(@PathVariable int id) {
        FileEntity file = fileService.getFileById(id);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        if (file.getFileData() == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        String contentType = detectContentType(file.getFileName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(file.getFileData());

    }


    private String detectFileType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp3")) return "audio";
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif"))
            return "image";
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "doc";
        if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mov")) return "video";
        if (lower.endsWith(".txt") || lower.endsWith(".csv")) return "text";
        return "unknown";
    }

    private String detectContentType(String fileName) {
        if (fileName.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "application/msword";
        } else if (fileName.endsWith(".mp4")) {
            return "video/mp4";
        }
        return "application/octet-stream";
    }


    @GetMapping("/video/{filename:.+}")
    public ResponseEntity<Void> serveVideo(@PathVariable String filename) {
        try {
            String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
            FileEntity fileEntity = fileService.getFileByName(decodedFilename);
            if (fileEntity == null) {
                return ResponseEntity.notFound().build();
            }

            URI videoUri = new URI(fileEntity.getFileUrl());
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(videoUri)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Conversion endpoints
// Convert JPG to PDF and download
    @GetMapping(value = "/convert/jpg-to-pdf/{fileId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> convertJpgToPdf(@PathVariable int fileId) throws IOException {
        byte[] pdfBytes = fileService.convertJpgToPdf(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);

    }

    // Convert PDF to DOCX and download
    @GetMapping(value = "/convert/pdf-to-docx/{fileId}", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public ResponseEntity<byte[]> convertPdfToDocx(@PathVariable int fileId) throws IOException {
        byte[] docxBytes = fileService.convertPdfToDocx(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.docx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }

    // Convert PDF to PPT and download
    @GetMapping(value = "/convert/pdf-to-ppt/{fileId}", produces = "application/vnd.openxmlformats-officedocument.presentationml.presentation")
    public ResponseEntity<byte[]> convertPdfToPpt(@PathVariable int fileId) throws IOException {
        byte[] pptBytes = fileService.convertPdfToPpt(fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=converted.pptx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .body(pptBytes);
    }



}
