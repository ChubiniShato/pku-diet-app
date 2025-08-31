package com.chubini.pku.security;

import java.util.Map;

import com.chubini.pku.security.dto.AuthenticationResponse;
import com.chubini.pku.security.dto.LoginRequest;
import com.chubini.pku.security.dto.RegisterRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user", description = "Create a new user account")
  public ResponseEntity<AuthenticationResponse> register(
      @Valid @RequestBody RegisterRequest request) {
    try {
      AuthenticationResponse response = authenticationService.register(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate user", description = "Login with username and password")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @Valid @RequestBody LoginRequest request) {
    try {
      AuthenticationResponse response = authenticationService.authenticate(request);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/refresh")
  @Operation(
      summary = "Refresh access token",
      description = "Get a new access token using refresh token")
  public ResponseEntity<AuthenticationResponse> refreshToken(
      @RequestBody Map<String, String> request) {
    try {
      String refreshToken = request.get("refresh_token");
      if (refreshToken == null || refreshToken.isEmpty()) {
        return ResponseEntity.badRequest().build();
      }

      AuthenticationResponse response = authenticationService.refreshToken(refreshToken);
      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Logout user",
      description = "Logout current user (client should discard tokens)")
  public ResponseEntity<Map<String, String>> logout() {
    // In a stateless JWT setup, logout is handled client-side by discarding tokens
    // For enhanced security, you could implement a token blacklist here
    return ResponseEntity.ok(Map.of("message", "Logout successful"));
  }

  @GetMapping("/profile")
  @Operation(
      summary = "Get current user profile",
      description = "Get profile information for the authenticated user")
  public ResponseEntity<AuthenticationResponse.UserDto> getProfile(
      jakarta.servlet.http.HttpServletRequest request) {
    try {
      // Get user from JWT token via authentication
      String authHeader = request.getHeader("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(401).build();
      }

      AuthenticationResponse.UserDto userDto =
          authenticationService.getCurrentUserProfile(authHeader.substring(7));
      return ResponseEntity.ok(userDto);
    } catch (RuntimeException e) {
      return ResponseEntity.status(401).build();
    }
  }

  @GetMapping("/validate")
  @Operation(
      summary = "Validate current token",
      description = "Check if the current token is valid and return user info")
  public ResponseEntity<Map<String, Object>> validateToken(
      jakarta.servlet.http.HttpServletRequest request) {
    try {
      String authHeader = request.getHeader("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.ok(Map.of("valid", false));
      }

      AuthenticationResponse.UserDto userDto =
          authenticationService.getCurrentUserProfile(authHeader.substring(7));
      return ResponseEntity.ok(Map.of("valid", true, "user", userDto));
    } catch (RuntimeException e) {
      return ResponseEntity.ok(Map.of("valid", false));
    }
  }

  @PostMapping("/change-password")
  @Operation(
      summary = "Change user password",
      description = "Change password for the authenticated user")
  public ResponseEntity<Map<String, String>> changePassword(
      @RequestBody Map<String, String> request,
      jakarta.servlet.http.HttpServletRequest httpRequest) {
    try {
      String authHeader = httpRequest.getHeader("Authorization");
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.status(401).build();
      }

      String currentPassword = request.get("currentPassword");
      String newPassword = request.get("newPassword");

      if (currentPassword == null || newPassword == null) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Current password and new password are required"));
      }

      authenticationService.changePassword(authHeader.substring(7), currentPassword, newPassword);
      return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
