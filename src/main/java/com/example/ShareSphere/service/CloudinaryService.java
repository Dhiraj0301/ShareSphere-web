package com.example.ShareSphere.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    // Constructor to initialize Cloudinary instance if needed
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Method to delete file from Cloudinary using its public ID
//    public void deleteFile(String publicId) {
//        try {
//            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
//            System.out.println("Cloudinary file deleted: " + result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


