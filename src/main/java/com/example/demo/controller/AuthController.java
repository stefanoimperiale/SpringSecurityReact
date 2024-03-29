package com.example.demo.controller;

import java.net.URI;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.exception.AppException;
import com.example.demo.model.Role;
import com.example.demo.model.RoleName;
import com.example.demo.model.User;
import com.example.demo.payload.ApiResponse;
import com.example.demo.payload.JwtAuthenticationResponse;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignUpRequest;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @PostMapping("/signin")
  public ResponseEntity<JwtAuthenticationResponse> authenticateUset(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword()
        )
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    String jwt = tokenProvider.generateToken(authentication);
    return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser (@Valid @RequestBody SignUpRequest signUpRequest) {
    if(userRepository.existsByUsername(signUpRequest.getUsername())) {
      return new ResponseEntity<>(new ApiResponse(false, "Username already taken!"), HttpStatus.BAD_REQUEST);
    }

    if(userRepository.existsByEmail(signUpRequest.getEmail())) {
      return new ResponseEntity<>(new ApiResponse(false, "Email Address already taken"), HttpStatus.BAD_REQUEST);
    }

    //Creating user's account
    User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(), passwordEncoder.encode(signUpRequest.getPassword()));

    Role role = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new AppException("User Role not set."));
    user.setRoles(Set.of(role));
    User result = userRepository.save(user);

    URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/users/{username}")
        .buildAndExpand(result.getUsername()).toUri();

    return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
  }
}
