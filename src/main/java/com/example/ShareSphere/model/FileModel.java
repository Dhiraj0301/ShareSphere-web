package com.example.ShareSphere.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FileModel {
    private int id;
    private String fileName;
    private String uploadedBy;
    private String contentType;
    private LocalDateTime uploadTime;
    private LocalDateTime expiryTime;
    private String fileUrl;







}
