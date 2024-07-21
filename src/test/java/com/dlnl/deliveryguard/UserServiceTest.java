package com.dlnl.deliveryguard;

import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.domain.UserRole;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.RoleRepository;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.service.RoleService;
import com.dlnl.deliveryguard.service.UserRoleService;
import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.UserRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("사용자의 회원가입을 관리자가 하는 API 테스트")
    public void registerNewUser_ShouldRegisterUser() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        User user = User.builder()
                .username(request.getUsername())
                .password("encodedPassword")
                .isSubValid(true)
                .subExpiredAt(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        Role role = Role.builder().name("USER").build();
        String expectedRefreshToken = "testRefreshToken";

        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleService.findByName("USER")).thenReturn(role);
        when(jwtUtil.generateRefreshToken(any(Long.class))).thenReturn(expectedRefreshToken);

        String response = userService.registerNewUser(request);

        assertEquals("사용자 testuser 등록 완료", response);
        verify(userRepository, times(2)).save(any(User.class));
        verify(userRoleService, times(1)).saveUserRole(any(User.class), any(Role.class));
    }

//    @Test
//    public void registerNewUser_ShouldThrowException_WhenRoleNotFound() {
//        UserRegistrationRequest request = new UserRegistrationRequest();
//        request.setUsername("testuser");
//        request.setPassword("password");
//
//        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
//        when(roleService.findByName("USER")).thenThrow(new RuntimeException("Role USER not found"));
//
//        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
//            userService.registerNewUser(request);
//        });
//
//        assertEquals("Role USER not found", exception.getMessage());
//        verify(userRepository, never()).save(any(User.class));
//        verify(userRoleService, never()).saveUserRole(any(User.class), any(Role.class));
//    }

    @Test
    @DisplayName("테스트용 관리자 회원가입 API 테스트")
    public void registerAdminUser_ShouldRegisterAdmin() {
        String username = "admin";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String refreshToken = "refreshToken";

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .isSubValid(true)
                .subExpiredAt(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        Role adminRole = Role.builder().name("ADMIN").build();

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateRefreshToken(any(Long.class))).thenReturn(refreshToken);
        when(roleService.findByName("ADMIN")).thenReturn(adminRole);

        String response = userService.registerAdminUser(username, password);

        assertEquals("관리자 admin 등록 완료", response);
        verify(userRepository, times(2)).save(any(User.class));
        verify(userRoleService, times(1)).saveUserRole(any(User.class), any(Role.class));
    }
}