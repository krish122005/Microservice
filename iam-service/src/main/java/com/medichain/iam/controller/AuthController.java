package com.medichain.iam.controller;

import com.medichain.iam.dto.LoginRequestDTO;
import com.medichain.iam.dto.LoginResponseDTO;
import com.medichain.iam.dto.RegisterRequestDTO;
import com.medichain.iam.entity.User;
import com.medichain.iam.exception.InvalidCredentialsException;
import com.medichain.iam.exception.UserNotActiveException;
import com.medichain.iam.repository.UserRepository;
import com.medichain.iam.security.JwtUtil;
import com.medichain.iam.service.AuditLogService;
import com.medichain.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil               jwtUtil;
    private final UserService           userService;
    private final UserRepository        userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuditLogService       auditLogService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDTO dto) {
        userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration successful. Await admin approval.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            auditLogService.log(user.getId(), "LOGIN_FAILED", "SESSION",
                    user.getEmail(), "Bad password");
            throw new InvalidCredentialsException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new UserNotActiveException("Account not yet activated by admin");
        }

        // Pass userId to token so downstream services can use it
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getId()
        );

        auditLogService.log(user.getId(), "LOGIN_SUCCESS", "SESSION",
                user.getEmail(), "Successful login");
        log.info("Login: userId={} role={}", user.getId(), user.getRole());

        return ResponseEntity.ok(new LoginResponseDTO(
                user.getId(),
                user.getRole().name(),
                token,
                user.getName()
        ));
    }
}
