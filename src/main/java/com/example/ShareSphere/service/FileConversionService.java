package com.example.ShareSphere.service;

import java.io.IOException;

public interface FileConversionService {
    byte[] convertJpgToPdf(byte[] jpgData) throws IOException;

    byte[] convertPdfToDocx(byte[] pdfData) throws IOException;

    byte[] convertPdfToPpt(byte[] pdfData) throws IOException;
}
