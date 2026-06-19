package com.planbridge.api.security;

import com.planbridge.api.entity.PbUser;
import com.planbridge.api.repository.PbUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanBridgeUserDetailsService implements UserDetailsService {

    private final PbUserRepository pbUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PbUser pbUser = pbUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(pbUser.getUsername())
                .password(pbUser.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + pbUser.getRole())))
                .build();
    }
}
