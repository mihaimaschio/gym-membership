package com.gym_membership.service;

import com.gym_membership.dto.JwtResponse;
import com.gym_membership.dto.LoginRequest;
import com.gym_membership.dto.RegisterRequest;
import com.gym_membership.entity.Admin;
import com.gym_membership.entity.Member;
import com.gym_membership.enums.Role;
import com.gym_membership.repository.AdminRepository;
import com.gym_membership.repository.MemberRepository;
import com.gym_membership.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return new JwtResponse(jwt, loginRequest.getEmail(), role);
    }

    @Transactional
    public String register(RegisterRequest registerRequest) {
        if (memberRepository.findByEmail(registerRequest.getEmail()).isPresent() ||
            adminRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        if (registerRequest.getRole() == Role.MEMBER) {
            Member member = new Member();
            member.setEmail(registerRequest.getEmail());
            member.setPassword(encodedPassword);
            member.setRole(Role.MEMBER);
            member.setFirstName(registerRequest.getFirstName());
            member.setLastName(registerRequest.getLastName());
            member.setPhoneNumber(registerRequest.getPhoneNumber());
            memberRepository.save(member);
        } else {
            Admin admin = new Admin();
            admin.setEmail(registerRequest.getEmail());
            admin.setPassword(encodedPassword);
            admin.setRole(Role.ADMIN);
            admin.setFirstName(registerRequest.getFirstName());
            admin.setLastName(registerRequest.getLastName());
            adminRepository.save(admin);
        }

        return "User registered successfully";
    }
}
