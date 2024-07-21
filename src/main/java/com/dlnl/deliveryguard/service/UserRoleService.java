package com.dlnl.deliveryguard.service;

import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.domain.User;
import com.dlnl.deliveryguard.domain.UserRole;
import com.dlnl.deliveryguard.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {
    @Autowired
    private final UserRoleRepository userRoleRepository;
    @Transactional
    public List<UserRole> findByUser(User user) {
        return userRoleRepository.findByUser(user);
    }
    @Transactional
    public void saveUserRole(User user, Role role) {
        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);
        log.info(user.getUsername(),"님 userrole 저장 완료");
    }
}
