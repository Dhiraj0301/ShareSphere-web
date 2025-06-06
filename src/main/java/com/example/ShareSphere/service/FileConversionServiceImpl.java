package com.example.ShareSphere.service;

import com.example.ShareSphere.service.FileConversionService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class FileConversionServiceImpl implements FileConversionService {

    @Override
    public byte[] convertJpgToPdf(byte[] jpgData) throws IOException {
        if (jpgData == null || jpgData.length == 0) {
            throw new IllegalArgumentException("Input JPG data is null or empty");
        }
        // Dummy implementation (replace with actual conversion logic)
        return jpgData;
    }

    @Override
    public byte[] convertPdfToDocx(byte[] pdfData) throws IOException {
        if (pdfData == null || pdfData.length == 0) {
            throw new IllegalArgumentException("Input PDF data is null or empty");
        }
        // Dummy implementation (replace with actual conversion logic)
        return pdfData;
    }

    @Override
    public byte[] convertPdfToPpt(byte[] pdfData) throws IOException {
        if (pdfData == null || pdfData.length == 0) {
            throw new IllegalArgumentException("Input PDF data is null or empty");
        }
        // Dummy implementation (replace with actual conversion logic)
        return pdfData;
    }
}
