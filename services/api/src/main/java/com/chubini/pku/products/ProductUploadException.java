package com.chubini.pku.products;

public class ProductUploadException extends RuntimeException {

  public ProductUploadException(String message) {
    super(message);
  }

  public ProductUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
