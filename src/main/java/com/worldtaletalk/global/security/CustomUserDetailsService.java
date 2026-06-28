package com.worldtaletalk.global.security;

import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.mapper.UserMapper;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByLoginId(loginId).orElseThrow(() ->
                new UsernameNotFoundException("해당 유저는 존재하지 않습니다. " + loginId));
        return new CustomUserDetails(userMapper.toDTO(user));
    }
}
