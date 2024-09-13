package com.dlnl.deliveryguard;


import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.LoginRequest;
import com.dlnl.deliveryguard.web.DTO.LoginResponse;
import com.dlnl.deliveryguard.web.DTO.RegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    @DisplayName("회원가입 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("정상적인 회원가입 요청")
        void testRegisterUser_Success() {
            // given
            RegistrationRequest request = new RegistrationRequest();
            request.setLoginID("testuser");
            request.setPassword("password123");  // 비밀번호 조건 만족 (영문, 숫자 포함, 8자리 이상)
            request.setUsername("Test User");
            request.setPhoneNumber("01012345678");
            request.setEmail("test@test.com");
            request.setStoreName("Test Store");
            request.setStoreAddress("123 Test Street");

            when(userRepository.existsByLoginID("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

            // when
            userService.registerUser(request);

            // then
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("회원가입 실패 케이스")
    class FailureCases {

        @Test
        @DisplayName("중복된 ID로 회원가입 실패")
        void testRegisterUser_DuplicateId() {
            // given
            RegistrationRequest request = new RegistrationRequest();
            request.setLoginID("duplicateuser");
            request.setPassword("password123");

            when(userRepository.existsByLoginID("duplicateuser")).thenReturn(true);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);
            });

            // then
            assertEquals("이미 존재하는 id입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("중복된 이메일로 회원가입 실패")
        void testRegisterUser_DuplicateEmail() {
            // given
            RegistrationRequest request = new RegistrationRequest();
            request.setLoginID("testuser");
            request.setEmail("duplicate@test.com");

            when(userRepository.existsByLoginID("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);
            });

            // then
            assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("비밀번호가 8자리 미만일 때 회원가입 실패")
        void testRegisterUser_PasswordTooShort() {
            // given
            RegistrationRequest request = new RegistrationRequest();
            request.setLoginID("testuser");
            request.setPassword("pass1");  // 8자리 미만
            request.setUsername("Test User");
            request.setPhoneNumber("01012345678");
            request.setEmail("test@test.com");
            request.setStoreName("Test Store");
            request.setStoreAddress("123 Test Street");

            when(userRepository.existsByLoginID("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);
            });

            // then
            assertEquals("비밀번호는 영문과 숫자를 포함한 8자리 이상이어야 합니다.", exception.getMessage());
        }

        @Test
        @DisplayName("비밀번호에 숫자가 포함되지 않아서 회원가입 실패")
        void testRegisterUser_PasswordNoNumber() {
            // given
            RegistrationRequest request = new RegistrationRequest();
            request.setLoginID("testuser");
            request.setPassword("password");  // 숫자 없음
            request.setUsername("Test User");
            request.setPhoneNumber("01012345678");
            request.setEmail("test@test.com");
            request.setStoreName("Test Store");
            request.setStoreAddress("123 Test Street");

            when(userRepository.existsByLoginID("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);
            });

            // then
            assertEquals("비밀번호는 영문과 숫자를 포함한 8자리 이상이어야 합니다.", exception.getMessage());
        }

        @Test
        @DisplayName("비밀번호에 영문이 포함되지 않아서 회원가입 실패")
        void testRegisterUser_PasswordNoLetter() {
            // given
            RegistrationRequest request = new RegistrationRequest();
            request.setLoginID("testuser");
            request.setPassword("12345678");  // 영문 없음
            request.setUsername("Test User");
            request.setPhoneNumber("01012345678");
            request.setEmail("test@test.com");
            request.setStoreName("Test Store");
            request.setStoreAddress("123 Test Street");

            when(userRepository.existsByLoginID("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@test.com")).thenReturn(false);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.registerUser(request);
            });

            // then
            assertEquals("비밀번호는 영문과 숫자를 포함한 8자리 이상이어야 합니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("로그인 성공 케이스")
    class LoginSuccessCases {

        @Test
        @DisplayName("정상적인 로그인 요청")
        void testLoginSuccess() {
            // given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setLoginID("testuser");
            loginRequest.setPassword("password123");

            User user = User.builder()
                    .id(1L)
                    .loginID("testuser")
                    .password("encodedPassword")
                    .build();

            when(userRepository.findByLoginID("testuser")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtil.generateAccessToken(1L)).thenReturn("generatedAccessToken");

            // when
            LoginResponse response = userService.login(loginRequest);

            // then
            assertNotNull(response);
            assertEquals("generatedAccessToken", response.getAccessToken());
            verify(userRepository).save(user);
        }
    }

    @Nested
    @DisplayName("로그인 실패 케이스")
    class LoginFailureCases {

        @Test
        @DisplayName("존재하지 않는 사용자")
        void testUserNotFound() {
            // given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setLoginID("unknownuser");
            loginRequest.setPassword("password123");

            when(userRepository.findByLoginID("unknownuser")).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.login(loginRequest);
            });

            assertEquals("존재하지 않는 사용자입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("비밀번호 불일치")
        void testPasswordMismatch() {
            // given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setLoginID("testuser");
            loginRequest.setPassword("wrongpassword");

            User user = User.builder()
                    .id(1L)
                    .loginID("testuser")
                    .password("encodedPassword")
                    .build();

            when(userRepository.findByLoginID("testuser")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.login(loginRequest);
            });

            assertEquals("비밀번호가 일치하지 않습니다.", exception.getMessage());
        }
    }


}