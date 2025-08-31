package com.chubini.pku.security.dto;

import com.chubini.pku.security.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

  // Frontend expects "token" not "access_token"
  private String token;

  @JsonProperty("refresh_token")
  private String refreshToken;

  @JsonProperty("token_type")
  @Builder.Default
  private String tokenType = "Bearer";

  @JsonProperty("expiresIn")
  private long expiresIn;

  // Frontend expects full user object
  private UserDto user;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UserDto {
    private String id;
    private String username;
    private String email;
    private String role;
    private String createdAt;
    private String updatedAt;
    private String lastLogin;
    private boolean isEnabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;

    public static UserDto from(User user) {
      return UserDto.builder()
          .id(user.getId().toString())
          .username(user.getUsername())
          .email(user.getEmail())
          .role(user.getRole().name())
          .createdAt(user.getCreatedAt().toString())
          .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null)
          .lastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null)
          .isEnabled(user.getEnabled())
          .isAccountNonExpired(user.getAccountNonExpired())
          .isAccountNonLocked(user.getAccountNonLocked())
          .isCredentialsNonExpired(user.getCredentialsNonExpired())
          .build();
    }
  }
}
