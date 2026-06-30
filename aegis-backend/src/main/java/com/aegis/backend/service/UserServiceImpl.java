package com.aegis.backend.service;

import com.aegis.backend.dto.RegisterRequest;
import com.aegis.backend.entity.Role;
import com.aegis.backend.entity.User;
import com.aegis.backend.exception.EmailAlreadyExistsException;
import com.aegis.backend.exception.UsernameAlreadyExistsException;
import com.aegis.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(final RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username '" + registerRequest.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + registerRequest.getEmail() + "' is already registered");
        }

        final User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User registerAdminCreatedUser(final RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username '" + registerRequest.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + registerRequest.getEmail() + "' is already registered");
        }

        final Role assignedRole = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;

        final User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(assignedRole)
                .build();

        return userRepository.save(user);
    }
}
