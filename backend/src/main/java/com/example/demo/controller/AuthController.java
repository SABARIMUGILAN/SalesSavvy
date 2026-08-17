package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(
    origins = "https://salessavvy-frontend-latest.onrender.com",
    allowCredentials = "true"
)
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        try {

            System.out.println("========== LOGIN START ==========");
            System.out.println("Username: " + loginRequest.getUsername());

            System.out.println("STEP 1: Calling authenticate...");

            User user = authService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            System.out.println("STEP 2: Authenticate completed");
            System.out.println("User ID: " + user.getUserId());

            System.out.println("STEP 3: Generating token...");

            String token = authService.generateToken(user);

            System.out.println("STEP 4: Token generated");

            // Create authentication cookie
            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(3600);

            response.addCookie(cookie);

            // SameSite=None is required for cross-site requests
            response.setHeader(
                    "Set-Cookie",
                    String.format(
                            "authToken=%s; HttpOnly; Secure; Path=/; Max-Age=3600; SameSite=None",
                            token
                    )
            );

            Map<String, Object> responseBody = new HashMap<>();

            responseBody.put(
                    "message",
                    "Login successful"
            );

            responseBody.put(
                    "role",
                    user.getRole().name()
            );

            responseBody.put(
                    "username",
                    user.getUsername()
            );

            System.out.println("STEP 5: Sending successful response");
            System.out.println("========== LOGIN END ==========");

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {

            System.out.println("========== LOGIN ERROR ==========");
            System.out.println("LOGIN ERROR: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========== LOGIN ERROR END ==========");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        try {

            User user =
                    (User) request.getAttribute("authenticatedUser");

            authService.logout(user);

            Cookie cookie =
                    new Cookie("authToken", null);

            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);

            response.setHeader(
                    "Set-Cookie",
                    "authToken=; HttpOnly; Secure; Path=/; Max-Age=0; SameSite=None"
            );

            Map<String, String> responseBody =
                    new HashMap<>();

            responseBody.put(
                    "message",
                    "Logout successful"
            );

            return ResponseEntity.ok(responseBody);

        } catch (RuntimeException e) {

            Map<String, String> errorResponse =
                    new HashMap<>();

            errorResponse.put(
                    "message",
                    "Logout failed"
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}