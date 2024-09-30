package com.dlnl.deliveryguard;


import com.dlnl.deliveryguard.config.PasswordUtil;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.service.UserService;
import com.dlnl.deliveryguard.web.DTO.LoginRequest;
import com.dlnl.deliveryguard.web.DTO.LoginResponse;
import com.dlnl.deliveryguard.web.DTO.RegistrationRequest;
import com.dlnl.deliveryguard.web.DTO.TokenResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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

    @Mock
    private JavaMailSender mailSender;

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
            when(jwtUtil.generateRefreshToken(1L)).thenReturn("generatedRefreshToken");

            // when
            LoginResponse response = userService.login(loginRequest);

            // then
            assertNotNull(response);
            assertEquals("generatedAccessToken", response.getAccessToken());
            assertEquals("generatedRefreshToken", response.getRefreshToken());
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
    @Nested
    @DisplayName("토큰 재발급 성공 케이스")
    class ReissueTokenSuccessCases {

        @Test
        @DisplayName("유효한 refresh token으로 토큰 재발급 성공")
        void reissueTokenSuccess() {
            // given
            String expiredAccessToken = "expiredAccessToken";
            String refreshToken = "validRefreshToken";
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .loginID("testuser")
                    .refreshToken(refreshToken)
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Mocking behavior
            when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(true);
            when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(jwtUtil.generateAccessToken(userId)).thenReturn("newAccessToken");
            when(jwtUtil.generateRefreshToken(userId)).thenReturn("newRefreshToken");

            // when
            TokenResponse response = userService.reissueToken(expiredAccessToken, refreshToken);

            // then
            assertNotNull(response);
            assertEquals("newAccessToken", response.getAccessToken());
            assertEquals("newRefreshToken", response.getRefreshToken());

            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("토큰 재발급 실패 케이스")
    class ReissueTokenFailureCases {

        @Test
        @DisplayName("유효하지 않은 refresh token으로 토큰 재발급 실패")
        void reissueTokenInvalidRefreshToken() {
            // given
            String expiredAccessToken = "expiredAccessToken";
            String refreshToken = "invalidRefreshToken";

            when(jwtUtil.validateRefreshToken(refreshToken)).thenThrow(new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.reissueToken(expiredAccessToken, refreshToken);
            });

            assertEquals("유효하지 않은 리프레시 토큰입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("사용자를 찾을 수 없을 때 토큰 재발급 실패")
        void reissueTokenUserNotFound() {
            // given
            String expiredAccessToken = "expiredAccessToken";
            String refreshToken = "validRefreshToken";
            Long userId = 1L;

            when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(true);
            when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.reissueToken(expiredAccessToken, refreshToken);
            });

            assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
        }

        @Test
        @DisplayName("Refresh token이 일치하지 않을 때 토큰 재발급 실패")
        void reissueTokenRefreshTokenMismatch() {
            // given
            String expiredAccessToken = "expiredAccessToken";
            String refreshToken = "validRefreshToken";
            Long userId = 1L;

            User user = User.builder()
                    .id(userId)
                    .loginID("testuser")
                    .refreshToken("differentRefreshToken") // 다른 refresh token을 가진 사용자
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(true);
            when(jwtUtil.getIdFromToken(refreshToken)).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.reissueToken(expiredAccessToken, refreshToken);
            });

            assertEquals("리프레시 토큰이 일치하지 않습니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Access token 유효성 검증 케이스")
    class ValidateAccessTokenCases {

        @Test
        @DisplayName("유효한 Access token")
        void validateValidAccessToken() {
            // given
            String accessToken = "validAccessToken";

            when(jwtUtil.validateToken(accessToken)).thenReturn(true);

            // when
            boolean isValid = userService.validateAccessToken(accessToken);

            // then
            assertTrue(isValid);
        }

        @Test
        @DisplayName("만료된 Access token")
        void validateExpiredAccessToken() {
            // given
            String accessToken = "expiredAccessToken";

            when(jwtUtil.validateToken(accessToken)).thenThrow(new BadCredentialsException("유효하지 않은 토큰입니다."));

            // when & then
            BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
                userService.validateAccessToken(accessToken);
            });

            assertEquals("유효하지 않은 토큰입니다.", exception.getMessage());
        }
    }
    @Nested
    @DisplayName("아이디 이메일 전송 성공 케이스")
    class SendEmailSuccessCases {

        @Test
        @DisplayName("존재하는 사용자에게 이메일 전송 성공")
        void sendEmailSuccess() throws MessagingException {
            // given
            String email = "user@example.com";
            String loginID = "testuser";

            User user = User.builder()
                    .loginID(loginID)
                    .email(email)
                    .build();

            MimeMessage mimeMessage = mock(MimeMessage.class); // MimeMessage를 모킹

            // Mocking behavior
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // when
            userService.sendUserIdToEmail(email);

            // then
            verify(userRepository, times(1)).findByEmail(email); // 이메일로 사용자 찾기 검증
            verify(mailSender, times(1)).send(mimeMessage); // 이메일 전송 검증
        }
    }

    @Nested
    @DisplayName("이메일 전송 실패 케이스")
    class SendEmailFailureCases {

        @Test
        @DisplayName("존재하지 않는 이메일로 이메일 전송 시도 실패")
        void sendEmailUserNotFound() {
            // given
            String email = "unknown@example.com";

            // Mocking behavior
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.sendUserIdToEmail(email);
            });

            assertEquals("해당 이메일을 가진 사용자를 찾을 수 없습니다.", exception.getMessage());
        }

    }
    @Nested
    @DisplayName("비밀번호 재설정 성공 케이스")
    class ResetPasswordSuccessCases {

        @Test
        @DisplayName("존재하는 사용자에게 랜덤 비밀번호로 변경 후 이메일 전송 성공")
        void resetPasswordSuccess() throws MessagingException {
            // given
            String email = "user@example.com";
            String loginID = "testuser";
            String randomPassword = "newRandomPassword";
            String encodedPassword = "encodedRandomPassword";

            User user = User.builder()
                    .loginID(loginID)
                    .email(email)
                    .build();

            MimeMessage mimeMessage = mock(MimeMessage.class); // MimeMessage 모킹

            // Mocking behavior
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(randomPassword)).thenReturn(encodedPassword); // 랜덤 비밀번호 암호화
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

            // 랜덤 비밀번호 생성 모킹
            mockStatic(PasswordUtil.class);
            when(PasswordUtil.generateRandomPassword()).thenReturn(randomPassword);

            // when
            userService.resetPasswordAndSendEmail(email);

            // then
            verify(userRepository, times(1)).findByEmail(email); // 이메일로 사용자 찾기 검증
            verify(userRepository, times(1)).save(user); // 비밀번호 업데이트 검증
            verify(mailSender, times(1)).send(mimeMessage); // 이메일 전송 검증
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정 실패 케이스")
    class ResetPasswordFailureCases {

        @Test
        @DisplayName("존재하지 않는 이메일로 비밀번호 재설정 시도 실패")
        void resetPasswordUserNotFound() {
            // given
            String email = "unknown@example.com";

            // Mocking behavior
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.resetPasswordAndSendEmail(email);
            });

            assertEquals("해당 이메일을 가진 사용자를 찾을 수 없습니다.", exception.getMessage());
        }

    }
    @Nested
    @DisplayName("비밀번호 변경 성공 케이스")
    class UpdatePasswordSuccessCases {

        @Test
        @DisplayName("정상적인 비밀번호 변경")
        void updatePasswordSuccess() {
            // given
            Long userId = 1L;
            String newPassword = "newPassword123";
            String encodedPassword = "encodedNewPassword";

            User user = User.builder()
                    .id(userId)
                    .loginID("testuser")
                    .password("oldPassword")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

            // when
            userService.updatePassword(userId, newPassword);

            // then
            verify(userRepository, times(1)).findById(userId);
            verify(passwordEncoder, times(1)).encode(newPassword);
            verify(userRepository, times(1)).save(user);
            assertEquals(encodedPassword, user.getPassword());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 실패 케이스")
    class UpdatePasswordFailureCases {

        @Test
        @DisplayName("사용자를 찾지 못한 경우 실패")
        void updatePasswordUserNotFound() {
            // given
            Long userId = 1L;
            String newPassword = "newPassword123";

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // when & then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                userService.updatePassword(userId, newPassword);
            });

            assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
        }
    }

}