package com.codex.intellilearn.controller;

import com.codex.intellilearn.dto.common.CommonResponse;
import com.codex.intellilearn.dto.request.LoginRequestDto;
import com.codex.intellilearn.dto.request.SignUpRequestDto;
import com.codex.intellilearn.dto.response.TokenResponseDto;
import com.codex.intellilearn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequestDto signUpRequestDto) {
        CommonResponse<?> commonResponse = userService.register(signUpRequestDto);
        if (commonResponse.getHasException()) {
            return ResponseEntity.internalServerError().body(commonResponse.getErrorResponse());
        }
        if (!commonResponse.getIsSuccess()) {
            return ResponseEntity.badRequest().body(commonResponse.getErrorResponse());
        }
        return ResponseEntity.ok().body(commonResponse.getResult());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        CommonResponse<?> commonResponse = userService.verifyEmail(token);
        if (commonResponse.getHasException()) {
            return ResponseEntity.internalServerError().body(commonResponse.getErrorResponse());
        }
        if (!commonResponse.getIsSuccess()) {
            return ResponseEntity.badRequest().body(commonResponse.getErrorResponse());
        }
        return ResponseEntity.ok().body(commonResponse.getResult());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
        @RequestBody LoginRequestDto request
    ) {
        CommonResponse<TokenResponseDto> commonResponse = userService.login(request);
        if (commonResponse.getHasException()) {
            return ResponseEntity.internalServerError().body(commonResponse.getErrorResponse());
        }
        if (!commonResponse.getIsSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(commonResponse.getErrorResponse());
        }
        return ResponseEntity.ok().body(commonResponse.getResult());
    }


}
