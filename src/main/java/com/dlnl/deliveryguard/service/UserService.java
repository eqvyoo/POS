package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.config.PasswordUtil;
import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.jwt.JwtUtil;
import com.dlnl.deliveryguard.repository.UserRepository;
import com.dlnl.deliveryguard.web.DTO.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;

    public User findUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException(id + "번 사용자는 존재하지 않습니다.");
        }
    }

    @Transactional
    public void registerUser(RegistrationRequest registrationRequest) {
        if (userRepository.existsByLoginID(registrationRequest.getLoginID())) {
            throw new IllegalArgumentException("이미 존재하는 id입니다.");
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (!isValidPassword(registrationRequest.getPassword())) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 포함한 8자리 이상이어야 합니다.");
        }

        String encodedPassword = passwordEncoder.encode(registrationRequest.getPassword());

        User user = User.builder()
                .loginID(registrationRequest.getLoginID())
                .password(encodedPassword)
                .userName(registrationRequest.getUsername())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .email(registrationRequest.getEmail())
                .storeName(registrationRequest.getStoreName())
                .storeAddress(registrationRequest.getStoreAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[A-Za-z].*") && password.matches(".*[0-9].*");
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByLoginID(loginRequest.getLoginID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        System.out.println("Input Password: " + loginRequest.getPassword());
        System.out.println("Stored Password: " + user.getPassword());

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        user.updateUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse reissueToken(String expiredAccessToken, String refreshToken) {

        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtUtil.getIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // DB에 저장된 Refresh token과 일치하는지 확인
        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("refresh 토큰이 일치하지 않습니다.");
        }

        // Access token 재발급
        String newAccessToken = jwtUtil.generateAccessToken(user.getId());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 새로운 Refresh token을 업데이트 후 저장
        user.updateRefreshToken(newRefreshToken);
        user.updateUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    public boolean validateAccessToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public void sendUserIdToEmail(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String userId = user.getLoginID();

            sendEmailWithUserId(email, userId);
        } else {
            throw new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다.");
        }
    }

    private void sendEmailWithUserId(String email, String userId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("내 아이디 찾기");
        helper.setText("<p>고객님의 이메일와 일치하는 아이디입니다.<br><strong>" + userId + "</strong></p>", true);

        mailSender.send(message);
    }
    public void resetPasswordAndSendEmail(String email) throws MessagingException {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 랜덤 비밀번호 생성
            String randomPassword = PasswordUtil.generateRandomPassword();
            System.out.println("Generated Password: " + randomPassword);

            // 비밀번호 암호화 후 업데이트
            String encodedPassword = passwordEncoder.encode(randomPassword);
            System.out.println("Encoded Password: " + encodedPassword);
            user.updatePassword(encodedPassword);
            user.updateUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            // 이메일로 비밀번호 전송
            sendEmailWithNewPassword(email, randomPassword);
        } else {
            throw new IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다.");
        }
    }

    private void sendEmailWithNewPassword(String email, String newPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("임시 비밀번호 발급");
        helper.setText("<p>임시 비밀번호를 알려드립니다.<br>" + newPassword + "</p>", true);

        mailSender.send(message);
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        //userId는 고유 번호인 값
        User user = findUserById(userId);

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updateUpdatedAt(LocalDateTime.now());
        user.updatePassword(encodedPassword);

        userRepository.save(user);
    }

    public Long getUserIdFromUserDetails(UserDetails userDetails) {
        try {
            String loginId = userDetails.getUsername();
            User user = userRepository.findByLoginID(loginId)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
            return user.getId();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("UserDetails의 사용자명에서 숫자 형식의 ID를 추출할 수 없습니다: " + userDetails.getUsername());
        }
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLoginID(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return createUserDetails(user);
    }

    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getLoginID(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }


}



