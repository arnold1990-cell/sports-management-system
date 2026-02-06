package com.sportsms.user;

import com.sportsms.common.NotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserRejectsDuplicateEmail() {
        UserDto.CreateUserRequest request = new UserDto.CreateUserRequest("admin@example.com", "Admin", "password123",
                Set.of(Role.ADMIN));
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
    }

    @Test
    void createUserEncodesPassword() {
        UserDto.CreateUserRequest request = new UserDto.CreateUserRequest("admin@example.com", "Admin", "password123",
                Set.of(Role.ADMIN));
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(request);

        Assertions.assertEquals("encoded", result.getPassword());
        Assertions.assertEquals(Set.of(Role.ADMIN), result.getRoles());
    }

    @Test
    void updateRolesThrowsWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class,
                () -> userService.updateRoles(userId, new UserDto.UpdateRolesRequest(Set.of(Role.COACH))));
    }
}
