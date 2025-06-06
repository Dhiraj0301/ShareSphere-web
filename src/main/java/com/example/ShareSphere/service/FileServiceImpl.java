package com.example.ShareSphere.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ShareSphere.entity.FileEntity;
import com.example.ShareSphere.exception.FileNotFoundException;
import com.example.ShareSphere.model.FileModel;
import com.example.ShareSphere.repository.FileRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;
    private final Cloudinary cloudinary;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public FileServiceImpl(FileRepository fileRepository, Cloudinary cloudinary, CloudinaryService cloudinaryService) {
        this.fileRepository = fileRepository;
        this.cloudinary = cloudinary;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadedBy) throws IOException {
        logger.info("Uploading file: {}", file.getOriginalFilename());
//        // 1. Create the directory if not exists
//        Path uploadPath = Paths.get(uploadDir);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
        Map<String, Object> uploadResult = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

        String fileUrl = (String) uploadResult.get("url");
        String publicId = (String) uploadResult.get("public_id");
//        Path filePath = uploadPath.resolve(fileName);


        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setUploadedBy(uploadedBy);
        fileEntity.setContentType(file.getContentType());
        fileEntity.setFileUrl(fileUrl);
        fileEntity.setCloudinaryPublicId(publicId);
        fileEntity.setFileData(file.getBytes());
        fileEntity.setExpiryTime(LocalDateTime.now().plusHours(12));
        fileEntity.setUploadTime(LocalDateTime.now());
//        fileEntity.setFilePath(filePath.toAbsolutePath().toString());
        fileRepository.save(fileEntity);

        FileModel fileModel = new FileModel();
        BeanUtils.copyProperties(fileEntity, fileModel);

        logger.info("File uploaded successfully: {}", fileEntity.getFileName());

        return ResponseEntity.ok(fileModel);
    }

    @Override
    public ResponseEntity<?> getFile(int id) {
        logger.info("Fetching file with ID: {}", id);
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + id));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, fileEntity.getContentType())
                .body(fileEntity.getFileUrl());
    }

    @Override
    public ResponseEntity<?> deleteFile(int id) {
        logger.info("Deleting file with ID: {}", id);
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + id));

        String publicId = fileEntity.getCloudinaryPublicId();
        if (publicId != null && !publicId.isEmpty()) {
            cloudinaryService.deleteFile(publicId);
            logger.info("Deleted file from Cloudinary with public ID: {}", publicId);
        }

        fileRepository.delete(fileEntity);
        logger.info("Deleted file from database: {}", fileEntity.getFileName());
        return ResponseEntity.ok("File deleted successfully");
    }

    @Override
    @Scheduled(fixedRate = 43_200_000) // Every 12 hours
    public void deleteExpiredFiles() {
        List<FileEntity> expiredFiles = fileRepository.findByExpiryTimeBefore(LocalDateTime.now());

        if (expiredFiles.isEmpty()) {
            logger.info("No expired files to delete.");
            return;
        }

        expiredFiles.forEach(file -> {
            String publicId = file.getCloudinaryPublicId();
            if (publicId != null && !publicId.isEmpty()) {
                cloudinaryService.deleteFile(publicId);
                logger.info("Deleted expired file from Cloudinary: {}", publicId);
            }
            fileRepository.delete(file);
            logger.info("Deleted expired file from database: {}", file.getFileName());
        });
    }

    @Override
    public List<FileModel> getAllFiles() {
        return fileRepository.findAll().stream().map(file -> {
            FileModel model = new FileModel();
            BeanUtils.copyProperties(file, model);
            return model;
        }).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> shareFile(int id) {
        logger.info("Sharing file with ID: {}", id);
        FileEntity fileEntity = fileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with ID: " + id));

        FileModel fileModel = new FileModel();
        BeanUtils.copyProperties(fileEntity, fileModel);

        return ResponseEntity.ok(fileModel);
    }

    @Override
    public FileEntity getFileById(Integer id) {
        logger.debug("Getting file by ID: {}", id);
        return fileRepository.findById(id).orElse(null);
    }

    @Override
    public FileEntity getFileByName(String filename) {
        logger.debug("Getting file by name: {}", filename);
        return fileRepository.findByFileName(filename);
    }

    @Override
    public byte[] convertJpgToPdf(int fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        if (fileEntity == null || fileEntity.getFileData() == null) {
            throw new FileNotFoundException("File data not found for ID: " + fileId);
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDImageXObject image = PDImageXObject.createFromByteArray(document, fileEntity.getFileData(), fileEntity.getFileName());
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float width = page.getMediaBox().getWidth() - 2 * margin;
                float height = page.getMediaBox().getHeight() - 2 * margin;
                contentStream.drawImage(image, margin, margin, width, height);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                document.save(out);
                logger.info("Converted JPG to PDF for file ID: {}", fileId);
                return out.toByteArray();
            }
        }
    }

    @Override
    public byte[] convertPdfToDocx(int fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        if (fileEntity == null || fileEntity.getFileData() == null) {
            throw new FileNotFoundException("File data not found for ID: " + fileId);
        }

        try (PDDocument pdfDoc = PDDocument.load(fileEntity.getFileData())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDoc);

            try (XWPFDocument docx = new XWPFDocument();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                XWPFParagraph paragraph = docx.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(text);

                docx.write(out);
                logger.info("Converted PDF to DOCX for file ID: {}", fileId);
                return out.toByteArray();
            }
        }
    }

    @Override
    public byte[] convertPdfToPpt(int fileId) throws IOException {
        FileEntity fileEntity = getFileById(fileId);
        if (fileEntity == null || fileEntity.getFileData() == null) {
            throw new FileNotFoundException("File data not found for ID: " + fileId);
        }

        try (PDDocument pdfDoc = PDDocument.load(fileEntity.getFileData())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDoc);

            try (XMLSlideShow ppt = new XMLSlideShow();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                XSLFSlide slide = ppt.createSlide();
                XSLFTextShape textShape = slide.createTextBox();
                textShape.setAnchor(new java.awt.Rectangle(50, 50, 600, 400));
                textShape.setText(text);

                ppt.write(out);
                logger.info("Converted PDF to PPT for file ID: {}", fileId);
                return out.toByteArray();
            }
        }
    }



}
