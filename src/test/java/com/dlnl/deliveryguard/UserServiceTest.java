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
import com.dlnl.deliveryguard.web.LoginRequest;
import com.dlnl.deliveryguard.web.LoginResponse;
import com.dlnl.deliveryguard.web.UserRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("관리자의 사용자 회원가입 테스트")
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
    @Test
    @DisplayName("테스트용 관리자 회원가입 테스트")
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

    @Test
    @DisplayName("성공 로그인 테스트")
    public void testAuthenticateUser_Success() throws Exception {
        String username = "testuser";
        String password = "password";
        Long userId = 1L;
        String encodedPassword = "encodedPassword";
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        User user = User.builder()
                .id(userId)
                .username(username)
                .password(encodedPassword)
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateAccessToken(userId)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userId)).thenReturn(refreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        LoginResponse loginResponse = userService.authenticateUser(loginRequest);

        assertNotNull(loginResponse);
        assertEquals(accessToken, loginResponse.getAccessToken());
        assertEquals(refreshToken, loginResponse.getRefreshToken());

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, times(1)).generateAccessToken(userId);
        verify(jwtUtil, times(1)).generateRefreshToken(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("유저 없는 로그인 테스트")
    public void testAuthenticateUser_UserNotFound() {
        String username = "testuser";
        String password = "password";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.authenticateUser(loginRequest);
        });

        assertEquals(username + " User not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(jwtUtil, never()).generateRefreshToken(anyLong());
    }

    @Test
    @DisplayName("비밀 번호 틀린 로그인 테스트")
    public void testAuthenticateUser_InvalidPassword() {
        String username = "testuser";
        String password = "password";
        Long userId = 1L;
        String encodedPassword = "encodedPassword";

        User user = User.builder()
                .id(userId)
                .username(username)
                .password(encodedPassword)
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        Exception exception = assertThrows(BadCredentialsException.class, () -> {
            userService.authenticateUser(loginRequest);
        });

        assertEquals("아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(jwtUtil, never()).generateRefreshToken(anyLong());
    }
}