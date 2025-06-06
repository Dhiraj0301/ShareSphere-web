package com.example.ShareSphere.service;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.ShareSphere.entity.FileEntity;
import com.example.ShareSphere.model.FileModel;

public interface FileService {

    // Upload a file
    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadedBy) throws IOException;

    // Get a file by its ID (returns file details)
    public ResponseEntity<?> getFile(int id);

    // Delete a file by ID
    public ResponseEntity<?> deleteFile(int id);

    // Delete expired files on a schedule
    public void deleteExpiredFiles();

    // Get all files
    public List<FileModel> getAllFiles();

    // Share file by ID
    public ResponseEntity<?> shareFile(int id);

    // Get file by ID (for internal use or specific details)
    FileEntity getFileById(Integer id);

    // Get file by name
    FileEntity getFileByName(String filename);
    // Convert JPG to PDF
    byte[] convertJpgToPdf(int fileId) throws IOException;

    // Convert PDF to DOCX
    byte[] convertPdfToDocx(int fileId) throws IOException;

    // Convert PDF to PPT
    byte[] convertPdfToPpt(int fileId) throws IOException;




    // Directory where you want to save uploaded files
//    public final String UPLOAD_DIR = "uploads";
}
