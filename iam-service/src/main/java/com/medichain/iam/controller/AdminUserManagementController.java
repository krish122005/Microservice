package com.medichain.iam.controller;

import com.medichain.iam.dto.CreateUserRequestDTO;
import com.medichain.iam.dto.UserResponseDTO;
import com.medichain.iam.exception.AccessDeniedException;
import com.medichain.iam.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user-management")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<String> createUser(
            @Valid @RequestBody CreateUserRequestDTO dto,
            @RequestHeader("X-Auth-Role") String callerRole) {

        requireAdmin(callerRole);
        userService.createUserByAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestHeader("X-Auth-Role") String callerRole) {

        requireAdmin(callerRole);
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Role") String callerRole) {

        requireAdmin(callerRole);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}