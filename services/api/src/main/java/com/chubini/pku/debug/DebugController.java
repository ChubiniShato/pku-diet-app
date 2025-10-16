package com.chubini.pku.debug;

import java.util.Map;
import java.util.Optional;

import com.chubini.pku.security.User;
import com.chubini.pku.security.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @GetMapping("/check-admin")
  public Map<String, Object> checkAdmin() {
    Optional<User> adminUser = userRepository.findByUsername("admin");

    if (adminUser.isPresent()) {
      User user = adminUser.get();
      return Map.of(
          "exists", true,
          "username", user.getUsername(),
          "email", user.getEmail(),
          "role", user.getRole(),
          "enabled", user.getEnabled(),
          "passwordHash", user.getPassword().substring(0, 20) + "..." // Show first 20 chars
          );
    } else {
      return Map.of("exists", false);
    }
  }

  @PostMapping("/create-admin")
  public Map<String, String> createAdmin() {
    try {
      // Check if admin already exists
      if (userRepository.findByUsername("admin").isPresent()) {
        return Map.of("message", "Admin user already exists");
      }

      // Create admin user
      User admin =
          User.builder()
              .username("admin")
              .email("admin@pkudiet.app")
              .password(passwordEncoder.encode("admin123"))
              .role(User.Role.ADMIN)
              .enabled(true)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .build();

      userRepository.save(admin);
      return Map.of("message", "Admin user created successfully");
    } catch (Exception e) {
      return Map.of("error", e.getMessage());
    }
  }

  @PostMapping("/test-password")
  public Map<String, Object> testPassword(@RequestBody Map<String, String> request) {
    String username = request.get("username");
    String password = request.get("password");

    Optional<User> user = userRepository.findByUsername(username);
    if (user.isPresent()) {
      boolean matches = passwordEncoder.matches(password, user.get().getPassword());
      return Map.of(
          "username", username,
          "passwordMatches", matches,
          "storedHash", user.get().getPassword().substring(0, 20) + "...");
    } else {
      return Map.of("error", "User not found");
    }
  }

  @PostMapping("/reset-admin-password")
  public Map<String, String> resetAdminPassword() {
    try {
      Optional<User> adminUser = userRepository.findByUsername("admin");
      if (adminUser.isPresent()) {
        User admin = adminUser.get();
        admin.setPassword(passwordEncoder.encode("admin123"));
        userRepository.save(admin);
        return Map.of("message", "Admin password reset to 'admin123' successfully");
      } else {
        return Map.of("error", "Admin user not found");
      }
    } catch (Exception e) {
      return Map.of("error", e.getMessage());
    }
  }
}
