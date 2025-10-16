package com.chubini.pku.security;

import java.time.LocalDateTime;

import com.chubini.pku.security.dto.AuthenticationResponse;
import com.chubini.pku.security.dto.LoginRequest;
import com.chubini.pku.security.dto.RegisterRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Value("${app.security.jwt.expiration}")
  private long jwtExpirationMs;

  public AuthenticationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.authenticationManager = authenticationManager;
  }

  public AuthenticationResponse register(RegisterRequest request) {
    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new RuntimeException("Username already exists");
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("Email already exists");
    }

    var user =
        User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(User.Role.USER)
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

    userRepository.save(user);

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);

    return AuthenticationResponse.builder()
        .token(jwtToken)
        .refreshToken(refreshToken)
        .expiresIn(jwtExpirationMs / 1000) // Convert to seconds
        .user(AuthenticationResponse.UserDto.from(user))
        .build();
  }

  public AuthenticationResponse authenticate(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    var user =
        userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

    // Update last login time
    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);

    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);

    return AuthenticationResponse.builder()
        .token(jwtToken)
        .refreshToken(refreshToken)
        .expiresIn(jwtExpirationMs / 1000) // Convert to seconds
        .user(AuthenticationResponse.UserDto.from(user))
        .build();
  }

  public AuthenticationResponse refreshToken(String refreshToken) {
    final String username = jwtService.extractUsername(refreshToken);

    if (username != null) {
      var user =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new RuntimeException("User not found"));

      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
            .token(accessToken)
            .refreshToken(newRefreshToken)
            .expiresIn(jwtExpirationMs / 1000)
            .user(AuthenticationResponse.UserDto.from(user))
            .build();
      }
    }

    throw new RuntimeException("Invalid refresh token");
  }

  public AuthenticationResponse.UserDto getCurrentUserProfile(String token) {
    final String username = jwtService.extractUsername(token);

    if (username != null) {
      var user =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new RuntimeException("User not found"));

      if (jwtService.isTokenValid(token, user)) {
        return AuthenticationResponse.UserDto.from(user);
      }
    }

    throw new RuntimeException("Invalid token");
  }

  public void changePassword(String token, String currentPassword, String newPassword) {
    final String username = jwtService.extractUsername(token);

    if (username != null) {
      var user =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new RuntimeException("User not found"));

      if (!jwtService.isTokenValid(token, user)) {
        throw new RuntimeException("Invalid token");
      }

      // Verify current password
      if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        throw new RuntimeException("Current password is incorrect");
      }

      // Validate new password
      if (newPassword == null || newPassword.length() < 6) {
        throw new RuntimeException("New password must be at least 6 characters long");
      }

      // Update password
      user.setPassword(passwordEncoder.encode(newPassword));
      user.setUpdatedAt(LocalDateTime.now());
      userRepository.save(user);
    } else {
      throw new RuntimeException("Invalid token");
    }
  }
}
