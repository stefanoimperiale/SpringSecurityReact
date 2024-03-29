package com.example.demo.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JwtAuthenticationResponse {
  private final String accessToken;
  private String tokenType = "Bearer";
}
