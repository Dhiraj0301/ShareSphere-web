package com.example.ShareSphere.FileUploader;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class FileUploader {

    private final Cloudinary cloudinary;

    // Inject the Cloudinary bean using constructor injection
    @Autowired
    public FileUploader(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> uploadFile(File file) throws IOException {
        try {
            // Upload the file to Cloudinary
            return cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        } catch (IOException e) {
            // Handle file upload exceptions
            throw new IOException("Error uploading file to Cloudinary", e);
        }
    }
}
