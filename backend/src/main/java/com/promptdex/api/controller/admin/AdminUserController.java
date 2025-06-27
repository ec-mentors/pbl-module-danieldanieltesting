package com.promptdex.api.controller.admin;

import com.promptdex.api.dto.UpdateUserRolesRequest;
import com.promptdex.api.dto.UserAdminViewDto;
import com.promptdex.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserAdminViewDto>> getAllUsers(
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(sort = "username", direction = org.springframework.data.domain.Sort.Direction.ASC) Pageable pageable) {
        Page<UserAdminViewDto> users = userService.getAllUsersAsAdmin(searchTerm, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserAdminViewDto> updateUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        UserAdminViewDto updatedUser = userService.updateUserRoles(userId, request.roles());
        return ResponseEntity.ok(updatedUser);
    }
}