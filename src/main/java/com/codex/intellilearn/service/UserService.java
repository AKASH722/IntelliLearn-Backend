package com.codex.intellilearn.service;

import com.codex.intellilearn.dto.common.CommonErrorResponse;
import com.codex.intellilearn.dto.common.CommonResponse;
import com.codex.intellilearn.dto.request.LoginRequestDto;
import com.codex.intellilearn.dto.request.SignUpRequestDto;
import com.codex.intellilearn.dto.response.TokenResponseDto;
import com.codex.intellilearn.model.Role;
import com.codex.intellilearn.model.User_;
import com.codex.intellilearn.model.VerificationToken;
import com.codex.intellilearn.repo.TokenRepo;
import com.codex.intellilearn.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final EmailService emailService;
    private final UserRepo userRepo;
    private final TokenRepo tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${intellilearn.ipaddress}")
    private String IP_ADDRESS;

    @Transactional
    public CommonResponse<?> register(SignUpRequestDto request) {
        if (request.getEmail() == null || request.getEmail().isBlank() ||
            request.getUsername() == null || request.getUsername().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank()
        ) {
            return new CommonResponse<>(
                new CommonErrorResponse(
                    new Date(),
                    "blank_filed",
                    "please provide all fields",
                    "some fields are blank"
                )
            );
        }
        if (userRepo.findByEmail(request.getEmail()).isPresent() ||
            userRepo.findByUsername(request.getUsername()).isPresent()
        ) {
            if ((userRepo.findByUsername(request.getUsername()).get()).getIsEnabled()) {
                return new CommonResponse<>(
                    new CommonErrorResponse(
                        new Date(),
                        "user_already_exists",
                        "user already exists",
                        "user already exists"
                    )
                );
            } else {
                return sendVerificationToken(request.getUsername());
            }
        }
        userRepo.save(
            User_.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .registrationDate(new Date())
                .isEnabled(false)
                .build()
        );
        return sendVerificationToken(request.getUsername());
    }

    private CommonResponse<String> sendVerificationToken(String username) {
        Optional<User_> userOptional = userRepo.findByUsername(username);

        if (userOptional.isPresent()) {
            User_ user = userOptional.get();

            VerificationToken existingToken = tokenRepo.findByUser_Id(user.getId());

            String token = UUID.randomUUID().toString();

            if (existingToken != null) {
                existingToken.setToken(token);
            } else {
                existingToken = VerificationToken.builder()
                    .token(token)
                    .user(user)
                    .build();
            }

            tokenRepo.save(existingToken);

            String verifyUserUrl = "http://" + IP_ADDRESS + ":8080/v1/auth/verify-email?token=" + token;

            sendEmail(user.getEmail(), verifyUserUrl);
            return new CommonResponse<>(
                "Email Sent Successfully"
            );
        } else {
            return new CommonResponse<>(
                new CommonErrorResponse(
                    new Date(),
                    HttpStatus.NOT_FOUND.toString(),
                    "user not found",
                    "user not found"
                )
            );
        }
    }


    private void sendEmail(String to, String setPasswordUrl) {
        emailService.sendEmail(to, "Verify Email", "Please click on the below link to verify your email:\n\n" + setPasswordUrl);
    }

    @Transactional
    public CommonResponse<?> verifyEmail(String token) {
        VerificationToken verificationToken = tokenRepo.findByToken(token);
        if (verificationToken == null) {
            return new CommonResponse<>(
                new CommonErrorResponse(
                    new Date(),
                    "invalid_request",
                    "invalid_request",
                    "invalid_request"
                )
            );
        }
        User_ user = verificationToken.getUser();
        user.setIsEnabled(true);
        tokenRepo.delete(verificationToken);
        return new CommonResponse<>(
            "You can login now"
        );
    }

    @Transactional
    public CommonResponse<TokenResponseDto> login(LoginRequestDto request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
            || request.getPassword() == null || request.getPassword().isBlank()
        ) {
            return new CommonResponse<>(
                new CommonErrorResponse(
                    new Date(),
                    "blank_filed",
                    "please provide all fields",
                    "some fields are blank"
                )
            );
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
        } catch (AuthenticationException e) {
            return new CommonResponse<>(
                new CommonErrorResponse(
                    new Date(),
                    "login_failed",
                    "please enter valid credentials",
                    "invalid username or password"
                )
            );
        }
        var user = userRepo.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return new CommonResponse<>(
                new CommonErrorResponse(
                    new Date(),
                    "login_failed",
                    "please enter valid credentials",
                    "invalid email or password"
                )
            );
        }
        if (!user.getIsEnabled()) {
            new CommonErrorResponse(
                new Date(),
                "account not enabled please verify",
                "account not enabled please verify",
                "account not enabled please verify"
            );
        }
        var jwtToken = jwtService.generateToken(user);
        return new CommonResponse<>(
            TokenResponseDto.builder()
                .token(jwtToken)
                .build()
        );
    }
}
