package com.planbridge.api.controller;

import com.planbridge.api.dto.request.LoginRequest;
import com.planbridge.api.dto.request.RegisterRequest;
import com.planbridge.api.dto.response.ApiResponse;
import com.planbridge.api.dto.response.LoginResponse;
import com.planbridge.api.entity.PbUser;
import com.planbridge.api.repository.PbUserRepository;
import com.planbridge.api.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PbUserRepository pbUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${planbridge.jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${planbridge.security.enabled:false}")
    private boolean securityEnabled;

    /**
     * POST /api/auth/login
     * body: { username, password }
     * response: { token, username, role, expiresIn }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            PbUser user = pbUserRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole());

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .expiresIn(jwtExpiration)
                    .build();

            log.info("User logged in: {}", user.getUsername());
            return ResponseEntity.ok(ApiResponse.ok(response));

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    /**
     * POST /api/auth/register
     * body: { username, password, role }
     * 보안 활성화 시 ADMIN만 등록 가능, 비활성화 시 누구나 가능
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest request) {
        // 보안 활성화 시 현재 사용자가 ADMIN인지 확인
        if (securityEnabled) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.isAuthenticated()
                    && auth.getAuthorities().stream()
                           .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("사용자 등록은 ADMIN만 가능합니다."));
            }
        }

        if (pbUserRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("이미 사용 중인 username입니다."));
        }

        String role = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole()
                : "DEVELOPER";

        PbUser newUser = PbUser.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status("ACTIVE")
                .build();

        pbUserRepository.save(newUser);

        log.info("New user registered: {} (role={})", newUser.getUsername(), newUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(Map.of(
                        "username", newUser.getUsername(),
                        "role", newUser.getRole()
                )));
    }
}
