package com.chubini.pku.labelscan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/** ImageStorageService for storing and managing label scan images */
@Service
@Slf4j
public class ImageStorageService {

  @Value("${app.storage.label-scans.path:data/label-scans}")
  private String baseStoragePath;

  @Value("${app.storage.label-scans.max-file-size:10485760}") // 10MB default
  private long maxFileSize;

  @Value("${app.storage.label-scans.allowed-extensions:jpeg,jpg,png,webp}")
  private String allowedExtensions;

  @Value("${app.storage.s3.enabled:false}")
  private boolean s3Enabled;

  @Value("${app.storage.s3.bucket:#{null}}")
  private String s3Bucket;

  @Value("${app.storage.signed-url.ttl-hours:24}")
  private int signedUrlTtlHours;

  /** Store an uploaded image file */
  public ImageStorageResult storeImage(MultipartFile file, UUID patientId) throws IOException {
    // Validate file
    validateImageFile(file);

    // Generate unique filename
    String fileName = generateUniqueFileName(file.getOriginalFilename(), patientId);

    // Create directory structure
    Path storagePath = Paths.get(baseStoragePath, getPatientDirectory(patientId));
    Files.createDirectories(storagePath);

    // Store file locally
    Path targetPath = storagePath.resolve(fileName);
    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

    // Generate relative path for database storage
    String relativePath = getPatientDirectory(patientId) + "/" + fileName;

    log.info("Stored image for patient {}: {}", patientId, relativePath);

    return ImageStorageResult.builder()
        .storedPath(relativePath)
        .fileName(fileName)
        .fileSize(file.getSize())
        .contentType(file.getContentType())
        .success(true)
        .build();
  }

  /** Generate a signed URL for secure access */
  public String generateSignedUrl(String relativePath, String accessToken) {
    if (s3Enabled && s3Bucket != null) {
      // TODO: Implement S3 signed URL generation
      return generateS3SignedUrl(relativePath, accessToken);
    } else {
      // For local storage, return a secure access path
      return generateLocalSecureUrl(relativePath, accessToken);
    }
  }

  /** Check if image file exists */
  public boolean imageExists(String relativePath) {
    Path fullPath = Paths.get(baseStoragePath, relativePath);
    return Files.exists(fullPath) && Files.isRegularFile(fullPath);
  }

  /** Delete an image file */
  public void deleteImage(String relativePath) throws IOException {
    Path fullPath = Paths.get(baseStoragePath, relativePath);
    if (Files.exists(fullPath)) {
      Files.delete(fullPath);
      log.info("Deleted image: {}", relativePath);
    }
  }

  /** Get image metadata */
  public ImageMetadata getImageMetadata(String relativePath) throws IOException {
    Path fullPath = Paths.get(baseStoragePath, relativePath);
    if (!Files.exists(fullPath)) {
      throw new IOException("Image file not found: " + relativePath);
    }

    return ImageMetadata.builder()
        .fileSize(Files.size(fullPath))
        .lastModified(Files.getLastModifiedTime(fullPath).toInstant())
        .exists(true)
        .build();
  }

  /** Validate uploaded image file */
  private void validateImageFile(MultipartFile file) throws IOException {
    // Check if file is empty
    if (file.isEmpty()) {
      throw new IOException("Uploaded file is empty");
    }

    // Check file size
    if (file.getSize() > maxFileSize) {
      throw new IOException(
          String.format(
              "File size %d exceeds maximum allowed size %d", file.getSize(), maxFileSize));
    }

    // Check file extension
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new IOException("Original filename is null");
    }

    String extension = getFileExtension(originalFilename).toLowerCase();
    String[] allowedExts = allowedExtensions.split(",");
    boolean isAllowed = false;
    for (String allowed : allowedExts) {
      if (allowed.trim().equals(extension)) {
        isAllowed = true;
        break;
      }
    }

    if (!isAllowed) {
      throw new IOException(
          String.format(
              "File extension '%s' is not allowed. Allowed extensions: %s",
              extension, allowedExtensions));
    }

    // Basic content type validation
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IOException("Uploaded file is not a valid image");
    }
  }

  /** Generate unique filename */
  private String generateUniqueFileName(String originalFilename, UUID patientId) {
    String extension = getFileExtension(originalFilename);
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String uuid = UUID.randomUUID().toString().substring(0, 8);

    return String.format(
        "%s_%s_%s.%s", patientId.toString().substring(0, 8), timestamp, uuid, extension);
  }

  /** Get patient-specific directory */
  private String getPatientDirectory(UUID patientId) {
    // Use first 2 chars of UUID for directory partitioning
    String patientIdStr = patientId.toString();
    return patientIdStr.substring(0, 2) + "/" + patientIdStr.substring(2, 4) + "/" + patientIdStr;
  }

  /** Get file extension */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return "";
    }
    return filename.substring(lastDotIndex + 1);
  }

  /** Generate S3 signed URL (placeholder) */
  private String generateS3SignedUrl(String relativePath, String accessToken) {
    // TODO: Implement actual S3 signed URL generation
    // This would use AWS SDK to generate a pre-signed URL
    log.warn("S3 signed URL generation not implemented, returning placeholder");
    return String.format(
        "https://%s.s3.amazonaws.com/%s?token=%s&expires=%d",
        s3Bucket,
        relativePath,
        accessToken,
        System.currentTimeMillis() + (signedUrlTtlHours * 3600000));
  }

  /** Generate secure local URL (placeholder) */
  private String generateLocalSecureUrl(String relativePath, String accessToken) {
    // TODO: Implement secure local URL generation with token validation
    // This could use JWT tokens or database-backed access tokens
    log.warn("Local secure URL generation not implemented, returning placeholder");
    return String.format("/api/v1/label-scan/images/%s?token=%s", relativePath, accessToken);
  }

  /** Result of image storage operation */
  public static class ImageStorageResult {
    private final String storedPath;
    private final String fileName;
    private final long fileSize;
    private final String contentType;
    private final boolean success;
    private final String errorMessage;

    public ImageStorageResult(
        String storedPath,
        String fileName,
        long fileSize,
        String contentType,
        boolean success,
        String errorMessage) {
      this.storedPath = storedPath;
      this.fileName = fileName;
      this.fileSize = fileSize;
      this.contentType = contentType;
      this.success = success;
      this.errorMessage = errorMessage;
    }

    public static Builder builder() {
      return new Builder();
    }

    // Getters
    public String getStoredPath() {
      return storedPath;
    }

    public String getFileName() {
      return fileName;
    }

    public long getFileSize() {
      return fileSize;
    }

    public String getContentType() {
      return contentType;
    }

    public boolean isSuccess() {
      return success;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public static class Builder {
      private String storedPath;
      private String fileName;
      private long fileSize;
      private String contentType;
      private boolean success = true;
      private String errorMessage;

      public Builder storedPath(String storedPath) {
        this.storedPath = storedPath;
        return this;
      }

      public Builder fileName(String fileName) {
        this.fileName = fileName;
        return this;
      }

      public Builder fileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
      }

      public Builder contentType(String contentType) {
        this.contentType = contentType;
        return this;
      }

      public Builder success(boolean success) {
        this.success = success;
        return this;
      }

      public Builder errorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
      }

      public ImageStorageResult build() {
        return new ImageStorageResult(
            storedPath, fileName, fileSize, contentType, success, errorMessage);
      }
    }
  }

  /** Image metadata */
  public static class ImageMetadata {
    private final long fileSize;
    private final java.time.Instant lastModified;
    private final boolean exists;

    public ImageMetadata(long fileSize, java.time.Instant lastModified, boolean exists) {
      this.fileSize = fileSize;
      this.lastModified = lastModified;
      this.exists = exists;
    }

    public static Builder builder() {
      return new Builder();
    }

    // Getters
    public long getFileSize() {
      return fileSize;
    }

    public java.time.Instant getLastModified() {
      return lastModified;
    }

    public boolean isExists() {
      return exists;
    }

    public static class Builder {
      private long fileSize;
      private java.time.Instant lastModified;
      private boolean exists = false;

      public Builder fileSize(long fileSize) {
        this.fileSize = fileSize;
        return this;
      }

      public Builder lastModified(java.time.Instant lastModified) {
        this.lastModified = lastModified;
        return this;
      }

      public Builder exists(boolean exists) {
        this.exists = exists;
        return this;
      }

      public ImageMetadata build() {
        return new ImageMetadata(fileSize, lastModified, exists);
      }
    }
  }
}
