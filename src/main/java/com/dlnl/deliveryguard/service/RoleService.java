package com.dlnl.deliveryguard.service;

import lombok.extern.slf4j.Slf4j;
import com.dlnl.deliveryguard.domain.Role;
import com.dlnl.deliveryguard.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Slf4j
@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role findById(Long id) {
        Optional<Role> role = roleRepository.findById(id);
        if (role.isPresent())
            return role.get();
        else {
            throw new RuntimeException("Role not found with Role id: " + id);
        }
    }

    public Role findByName(String name) {
        Optional<Role> role = roleRepository.findByName(name);
        if (role.isPresent())
            return role.get();
        else {
            throw new RuntimeException("Role not found with Role name: " + name);
        }
    }
    public Role createRole(String name) {
        Role role = Role.builder().name(name).build();
        return roleRepository.save(role);
    }
}
