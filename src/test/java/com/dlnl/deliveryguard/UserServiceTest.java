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
import com.dlnl.deliveryguard.web.SubscriptionUpdateRequest;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.apache.commons.lang3.time.DateUtils.addDays;
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
    @DisplayName("관리자의 사용자 회원가입 성공 테스트")
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
    @DisplayName("테스트용 관리자 회원가입 성공 테스트")
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
    @DisplayName("로그인 성공 테스트")
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
    @DisplayName("유저 없는 로그인 실패 테스트")
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
    @DisplayName("비밀번호 틀린 로그인 실패 테스트")
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
    @Test
    @DisplayName("refresh token으로 Login 성공 테스트")
    public void testLoginWithRefreshToken_Success() {
        String refreshToken = "validRefreshToken";
        Long userId = 1L;
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .password("encodedPassword")
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(userId)).thenReturn(newAccessToken);
        when(jwtUtil.generateRefreshToken(userId)).thenReturn(newRefreshToken);

        LoginResponse loginResponse = userService.loginWithRefreshToken(refreshToken);

        assertNotNull(loginResponse);
        assertEquals(newAccessToken, loginResponse.getAccessToken());
        assertEquals(newRefreshToken, loginResponse.getRefreshToken());  // 새로운 refreshToken 검증

        verify(jwtUtil, times(1)).validateToken(refreshToken);
        verify(jwtUtil, times(1)).getIdFromToken(refreshToken);
        verify(userRepository, times(1)).findById(userId);
        verify(jwtUtil, times(1)).generateAccessToken(userId);
        verify(jwtUtil, times(1)).generateRefreshToken(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Invalid refresh token으로 Login 실패 테스트")
    public void testLoginWithRefreshToken_InvalidToken() {
        String refreshToken = "invalidRefreshToken";

        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.loginWithRefreshToken(refreshToken);
        });

        assertEquals("Invalid Refresh Token", exception.getMessage());

        verify(jwtUtil, times(1)).validateToken(refreshToken);
        verify(jwtUtil, never()).getIdFromToken(anyString());
        verify(userRepository, never()).findById(anyLong());
        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(jwtUtil, never()).generateRefreshToken(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("User not found로 Login 실패 테스트")
    public void testLoginWithRefreshToken_UserNotFound() {
        String refreshToken = "validRefreshToken";
        Long userId = 1L;

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.loginWithRefreshToken(refreshToken);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());

        verify(jwtUtil, times(1)).validateToken(refreshToken);
        verify(jwtUtil, times(1)).getIdFromToken(refreshToken);
        verify(userRepository, times(1)).findById(userId);
        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(jwtUtil, never()).generateRefreshToken(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("refresh token으로 토큰 정보 갱신 성공 테스트")
    public void testRefreshAccessToken_Success() {
        String refreshToken = "validRefreshToken";
        Long userId = 1L;
        String newAccessToken = "newAccessToken";

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .password("encodedPassword")
                .build();

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(userId)).thenReturn(newAccessToken);

        LoginResponse loginResponse = userService.refreshAccessToken(refreshToken);

        assertNotNull(loginResponse);
        assertEquals(newAccessToken, loginResponse.getAccessToken());
        assertEquals(refreshToken, loginResponse.getRefreshToken());

        verify(jwtUtil, times(1)).validateToken(refreshToken);
        verify(jwtUtil, times(1)).getIdFromToken(refreshToken);
        verify(userRepository, times(1)).findById(userId);
        verify(jwtUtil, times(1)).generateAccessToken(userId);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("유효하지 않은 refresh token으로 토큰 정보 갱신 실패 테스트")
    public void testRefreshAccessToken_InvalidToken() {
        String refreshToken = "invalidRefreshToken";

        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            userService.refreshAccessToken(refreshToken);
        });

        assertEquals("Invalid Refresh Token", exception.getMessage());

        verify(jwtUtil, times(1)).validateToken(refreshToken);
        verify(jwtUtil, never()).getIdFromToken(anyString());
        verify(userRepository, never()).findById(anyLong());
        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("refresh token로 user 찾기 실패로 토큰 정보 갱신 실패 테스트")
    public void testRefreshAccessToken_UserNotFound() {
        String refreshToken = "validRefreshToken";
        Long userId = 1L;

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.refreshAccessToken(refreshToken);
        });

        assertEquals("User not found with id: " + userId, exception.getMessage());

        verify(jwtUtil, times(1)).validateToken(refreshToken);
        verify(jwtUtil, times(1)).getIdFromToken(refreshToken);
        verify(userRepository, times(1)).findById(userId);
        verify(jwtUtil, never()).generateAccessToken(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("구독 정보 갱신 성공 테스트")
    public void testUpdateSubscriptions_Success() {
        Date currentDate = new Date();
        SubscriptionUpdateRequest request1 = new SubscriptionUpdateRequest(1L, true, addDays(currentDate, 30));
        SubscriptionUpdateRequest request2 = new SubscriptionUpdateRequest(2L, false, new Date());

        User user1 = User.builder().id(1L).username("user1").isSubValid(true).subExpiredAt(currentDate).build();
        User user2 = User.builder().id(2L).username("user2").isSubValid(true).subExpiredAt(currentDate).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        userService.updateSubscriptions(Arrays.asList(request1, request2));

        verify(userRepository, times(2)).save(any(User.class));

        // 변경된 구독 정보를 확인
        assertTrue(user1.getIsSubValid());
        assertEquals(convertToLocalDate(addDays(currentDate, 30)), convertToLocalDate(user1.getSubExpiredAt()));
        assertFalse(user2.getIsSubValid());
        assertEquals(convertToLocalDate(currentDate), convertToLocalDate(user2.getSubExpiredAt()));
    }

    @Test
    @DisplayName("사용자를 찾을 수 없음으로 인한 구독 정보 갱신 실패 테스트")
    public void testUpdateSubscriptions_UserNotFound() {
        SubscriptionUpdateRequest request1 = new SubscriptionUpdateRequest(1L, true, addDays(new Date(), 30));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateSubscriptions(Arrays.asList(request1));
        });

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    private LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}






