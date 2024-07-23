package com.dlnl.deliveryguard;

import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.service.RoleService;
import com.dlnl.deliveryguard.service.UserRoleService;
import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.*;
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
    @DisplayName("새 사용자 등록 성공 테스트")
    public void registerNewUser_ShouldRegisterUser() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        String encodedPassword = "encodedPassword";
        String refreshToken = "refreshToken";

        User user = User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .isSubValid(true)
                .subExpiredAt(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        Role role = Role.builder().name("USER").build();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateRefreshToken(any(Long.class))).thenReturn(refreshToken);
        when(roleService.findByName("USER")).thenReturn(Optional.of(role));

        String response = userService.registerNewUser(request);

        assertEquals("사용자 testuser 등록 완료", response);
        verify(userRepository, times(2)).save(any(User.class));
        verify(userRoleService, times(1)).saveUserRole(any(User.class), any(Role.class));
    }

    @Test
    @DisplayName("사용자 등록 실패 테스트 - 중복된 아이디")
    public void registerNewUser_ShouldThrowExceptionWhenUsernameExists() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(Exception.class, () -> {
            userService.registerNewUser(request);
        });

        assertEquals("해당 아이디는 사용하실 수 없습니다.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userRoleService, never()).saveUserRole(any(User.class), any(Role.class));
    }


@Test
@DisplayName("테스트용 관리자 회원가입 성공 테스트")
public void registerAdminUser_ShouldRegisterAdmin() throws Exception {
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

    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtUtil.generateRefreshToken(any(Long.class))).thenReturn(refreshToken);
    when(roleService.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

    String response = userService.registerAdminUser(username, password);

    assertEquals("관리자 admin 등록 완료", response);
    verify(userRepository, times(2)).save(any(User.class));
    verify(userRoleService, times(1)).saveUserRole(any(User.class), any(Role.class));
}



    @Test
    @DisplayName("사용자 로그인 성공 테스트")
    public void loginUser_ShouldAuthenticateUser() throws Exception {
        String username = "testuser";
        String password = "password";
        String encodedPassword = "encodedPassword";
        Long userId = 1L;
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        User user = User.builder()
                .id(userId)
                .username(username)
                .password(encodedPassword)
                .refreshToken(refreshToken)
                .build();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateAccessToken(userId)).thenReturn(accessToken);

        LoginResponse loginResponse = userService.authenticateUser(loginRequest);

        assertNotNull(loginResponse);
        assertEquals(accessToken, loginResponse.getAccessToken());
        assertEquals(refreshToken, loginResponse.getRefreshToken());

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, encodedPassword);
        verify(jwtUtil, times(1)).generateAccessToken(userId);
    }

    @Test
    @DisplayName("사용자 로그인 실패 테스트 - 잘못된 비밀번호")
    public void loginUser_ShouldThrowExceptionForInvalidPassword() {
        String username = "testuser";
        String password = "password";
        String encodedPassword = "encodedPassword";

        User user = User.builder()
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
    }

    @Test
    @DisplayName("사용자 로그인 실패 테스트 - 사용자 없음")
    public void loginUser_ShouldThrowExceptionForUserNotFound() {
        String username = "testuser";
        String password = "password";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.authenticateUser(loginRequest);
        });

        assertEquals(username + " 해당되는 아이디가 없습니다.", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateAccessToken(anyLong());
    }


    @Test
    @DisplayName("구독 정보 갱신 성공 테스트")
    public void updateSubscriptions_ShouldUpdateSubscriptions() {
        Date currentDate = new Date();
        Date newExpirationDate = addDays(currentDate, 30);

        SubscriptionUpdateRequest request1 = new SubscriptionUpdateRequest(1L, true, newExpirationDate);
        SubscriptionUpdateRequest request2 = new SubscriptionUpdateRequest(2L, false, new Date());

        User user1 = User.builder().id(1L).username("user1").isSubValid(true).subExpiredAt(currentDate).build();
        User user2 = User.builder().id(2L).username("user2").isSubValid(true).subExpiredAt(currentDate).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        userService.updateSubscriptions(Arrays.asList(request1, request2));

        verify(userRepository, times(2)).save(any(User.class));

        // 변경된 구독 정보를 확인
        assertTrue(user1.getIsSubValid());
        assertEquals(convertToLocalDate(newExpirationDate), convertToLocalDate(user1.getSubExpiredAt()));
        assertFalse(user2.getIsSubValid());
        assertEquals(convertToLocalDate(currentDate), convertToLocalDate(user2.getSubExpiredAt()));
    }

    @Test
    @DisplayName("구독 정보 갱신 실패 테스트 - 사용자를 찾을 수 없음")
    public void updateSubscriptions_ShouldThrowExceptionWhenUserNotFound() {
        SubscriptionUpdateRequest request1 = new SubscriptionUpdateRequest(1L, true, addDays(new Date(), 30));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateSubscriptions(Arrays.asList(request1));
        });

        assertEquals("1번 사용자는 존재하지 않습니다.", exception.getMessage());
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
    @Test
    @DisplayName("사용자 정보 조회 성공 테스트")
    public void getUserInfo_ShouldReturnUserInfo() {
        String token = "Bearer validToken";
        String jwtToken = "validToken";
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .isSubValid(true)
                .subExpiredAt(new Date())
                .build();

        when(jwtUtil.validateToken(jwtToken)).thenReturn(true);
        when(jwtUtil.getIdFromToken(jwtToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserInfoResponse userInfoResponse = userService.getUserInfo(token);

        assertNotNull(userInfoResponse);
        assertEquals(userId, userInfoResponse.getId());
        assertEquals("testuser", userInfoResponse.getUsername());
        assertEquals("참", userInfoResponse.getIsvalid());
        assertEquals(user.getSubExpiredAt(), userInfoResponse.getSubExpiredAt());

        verify(jwtUtil, times(1)).validateToken(jwtToken);
        verify(jwtUtil, times(1)).getIdFromToken(jwtToken);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 테스트 - 사용자 없음")
    public void getUserInfo_ShouldThrowExceptionWhenUserNotFound() {
        String token = "Bearer validToken";
        String jwtToken = "validToken";
        Long userId = 1L;

        when(jwtUtil.validateToken(jwtToken)).thenReturn(true);
        when(jwtUtil.getIdFromToken(jwtToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserInfo(token);
        });

        assertEquals(userId + "번 사용자는 존재하지 않습니다.", exception.getMessage());

        verify(jwtUtil, times(1)).validateToken(jwtToken);
        verify(jwtUtil, times(1)).getIdFromToken(jwtToken);
        verify(userRepository, times(1)).findById(userId);
    }
    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    public void updatePassword_ShouldUpdatePassword() {
        String token = "Bearer validToken";
        String jwtToken = "validToken";
        String username = "testuser";
        Long userId = 1L;
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";

        User user = User.builder()
                .id(userId)
                .username(username)
                .password("oldPassword")
                .build();

        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setUsername(username);
        request.setNewPassword(newPassword);

        when(jwtUtil.validateToken(jwtToken)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        PasswordUpdateResponse response = userService.updatePassword(token, request);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(username, response.getUsername());
        assertEquals(encodedPassword, user.getPassword());

        verify(jwtUtil, times(1)).validateToken(jwtToken);
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 테스트 - 사용자 없음")
    public void updatePassword_ShouldThrowExceptionWhenUserNotFound() {
        String token = "Bearer validToken";
        String jwtToken = "validToken";
        String username = "testuser";
        String newPassword = "newPassword";

        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setUsername(username);
        request.setNewPassword(newPassword);

        when(jwtUtil.validateToken(jwtToken)).thenReturn(true);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword(token, request);
        });

        assertEquals(username + " 해당되는 아이디가 없습니다.", exception.getMessage());

        verify(jwtUtil, times(1)).validateToken(jwtToken);
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}







