package com.sportsms.user;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto.UserResponse> listUsers() {
        return userService.findAll().stream()
                .map(user -> new UserDto.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles(), user.getCreatedAt()))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto.UserResponse createUser(@Valid @RequestBody UserDto.CreateUserRequest request) {
        User user = userService.createUser(request);
        return new UserDto.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles(), user.getCreatedAt());
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto.UserResponse updateUser(@PathVariable("userId") UUID userId,
                                           @Valid @RequestBody UserDto.UpdateRolesRequest request) {
        User user = userService.updateRoles(userId, request);
        return new UserDto.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles(), user.getCreatedAt());
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto.UserResponse updateRoles(@PathVariable("userId") UUID userId,
                                            @Valid @RequestBody UserDto.UpdateRolesRequest request) {
        User user = userService.updateRoles(userId, request);
        return new UserDto.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRoles(), user.getCreatedAt());
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable("userId") UUID userId) {
        userService.deleteUser(userId);
    }
}
