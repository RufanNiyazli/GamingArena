package com.project.redis.service.impl;

import com.project.redis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("🔍 Loading user by email: {}", email);

        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> {
                    log.warn("❌ User not found: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });
    }
}
