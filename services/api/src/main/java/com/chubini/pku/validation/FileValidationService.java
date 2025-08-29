package com.chubini.pku.validation;

import com.chubini.pku.config.FileUploadConfig;
import com.chubini.pku.products.ProductUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@Service
public class FileValidationService {

    private final FileUploadConfig fileUploadConfig;

    public FileValidationService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProductUploadException("File is required and cannot be empty");
        }

        validateFileSize(file);
        validateFileType(file);
        validateFileName(file);
    }

    private void validateFileSize(MultipartFile file) {
        long maxSizeBytes = fileUploadConfig.getMaxFileSize().toBytes();
        if (file.getSize() > maxSizeBytes) {
            throw new ProductUploadException(
                String.format("File size %d bytes exceeds maximum allowed size %d bytes", 
                    file.getSize(), maxSizeBytes));
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        String[] allowedMimeTypes = fileUploadConfig.getAllowedMimeTypes();
        
        if (contentType == null || !Arrays.asList(allowedMimeTypes).contains(contentType)) {
            throw new ProductUploadException(
                String.format("File type '%s' is not allowed. Allowed types: %s", 
                    contentType, Arrays.toString(allowedMimeTypes)));
        }
    }

    private void validateFileName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ProductUploadException("File name is required");
        }

        // Check for directory traversal attacks
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ProductUploadException("Invalid file name: path traversal not allowed");
        }

        // Validate file extension
        String[] allowedExtensions = fileUploadConfig.getAllowedExtensions();
        boolean hasValidExtension = Arrays.stream(allowedExtensions)
            .anyMatch(ext -> fileName.toLowerCase().endsWith(ext.toLowerCase()));
        
        if (!hasValidExtension) {
            throw new ProductUploadException(
                String.format("File extension not allowed. Allowed extensions: %s", 
                    Arrays.toString(allowedExtensions)));
        }

        // Check for suspicious file names
        String lowerFileName = fileName.toLowerCase();
        String[] suspiciousPatterns = {".exe", ".bat", ".cmd", ".scr", ".js", ".vbs", ".ps1"};
        for (String pattern : suspiciousPatterns) {
            if (lowerFileName.contains(pattern)) {
                throw new ProductUploadException("Suspicious file name detected");
            }
        }
    }
}
