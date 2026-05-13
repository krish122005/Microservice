package com.medichain.iam.controller;

import com.medichain.iam.dto.AssignRoleRequestDTO;
import com.medichain.iam.entity.Role;
import com.medichain.iam.exception.AccessDeniedException;
import com.medichain.iam.exception.InvalidRoleException;
import com.medichain.iam.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activate(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Role") String callerRole) {

        requireAdmin(callerRole);
        adminUserService.activateUser(id);
        return ResponseEntity.ok("User activated successfully");
    }
    

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivate(
            @PathVariable Long id,
            @RequestHeader("X-Auth-Role") String callerRole) {

        requireAdmin(callerRole);
        adminUserService.deactivateUser(id);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<String> assignRole(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequestDTO dto,
            @RequestHeader("X-Auth-Role") String callerRole) {

        requireAdmin(callerRole);

        Role role;
        try {
            role = Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRoleException("Unknown role: " + dto.getRole());
        }

        adminUserService.assignRole(id, role);
        return ResponseEntity.ok("Role assigned: " + role);
    }

    private void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Admin access required");
        }
    }
}