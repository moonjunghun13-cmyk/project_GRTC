package com.grtc.main.login.controller;

import com.grtc.main.login.dto.LoginRequestDto;
import com.grtc.main.login.dto.SignUpRequestDto;
import com.grtc.main.login.dto.SignUpResponseDto;
import com.grtc.main.login.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginRestController {

    private final LoginService loginService;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/login")
    public ResponseEntity<SignUpResponseDto> login(
            @Valid
            @RequestBody
            LoginRequestDto request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    )
    {
        SignUpResponseDto user = loginService.login(request);

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(user.getId(), null, authorities);
        SecurityContext context =
                SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, httpServletRequest, httpServletResponse);

        return ResponseEntity.ok(user);
    }
   @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(
            @Valid
            @RequestBody
            SignUpRequestDto request
   )
   {
       SignUpResponseDto response = loginService.signUp(request);

       return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }
}
