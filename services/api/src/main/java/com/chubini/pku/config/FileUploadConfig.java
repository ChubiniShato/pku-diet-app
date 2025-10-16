package com.chubini.pku.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

@Configuration
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadConfig {

  private DataSize maxFileSize = DataSize.ofMegabytes(10); // 10MB default
  private DataSize maxRequestSize = DataSize.ofMegabytes(10); // 10MB default
  private String[] allowedMimeTypes = {"text/csv", "application/csv", "text/plain"};
  private String[] allowedExtensions = {".csv", ".txt"};

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    factory.setMaxFileSize(maxFileSize);
    factory.setMaxRequestSize(maxRequestSize);
    return factory.createMultipartConfig();
  }

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver();
  }

  // Getters and setters for configuration properties
  public DataSize getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(DataSize maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  public DataSize getMaxRequestSize() {
    return maxRequestSize;
  }

  public void setMaxRequestSize(DataSize maxRequestSize) {
    this.maxRequestSize = maxRequestSize;
  }

  public String[] getAllowedMimeTypes() {
    return allowedMimeTypes;
  }

  public void setAllowedMimeTypes(String[] allowedMimeTypes) {
    this.allowedMimeTypes = allowedMimeTypes;
  }

  public String[] getAllowedExtensions() {
    return allowedExtensions;
  }

  public void setAllowedExtensions(String[] allowedExtensions) {
    this.allowedExtensions = allowedExtensions;
  }
}
