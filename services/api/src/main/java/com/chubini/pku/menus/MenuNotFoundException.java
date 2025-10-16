package com.chubini.pku.menus;

/** Exception thrown when a menu item is not found */
public class MenuNotFoundException extends RuntimeException {

  public MenuNotFoundException(String message) {
    super(message);
  }

  public MenuNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
