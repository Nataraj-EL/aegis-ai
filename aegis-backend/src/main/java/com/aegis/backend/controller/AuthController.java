package com.aegis.backend.controller;

import com.aegis.backend.dto.ApiResponse;
import com.aegis.backend.dto.AuthRequest;
import com.aegis.backend.dto.AuthResponse;
import com.aegis.backend.dto.LogoutRequest;
import com.aegis.backend.dto.RefreshTokenRequest;
import com.aegis.backend.dto.RegisterRequest;
import com.aegis.backend.dto.UserResponse;
import com.aegis.backend.entity.User;
import com.aegis.backend.event.LoginFailureEvent;
import com.aegis.backend.event.LoginSuccessEvent;
import com.aegis.backend.event.LogoutEvent;
import com.aegis.backend.mapper.UserMapper;
import com.aegis.backend.service.AuthService;
import com.aegis.backend.service.UserService;
import com.aegis.backend.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Authentication Controller",
        description =
                "REST endpoints for user registration, authentication, refresh, logout, and current user profile mapping")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String CODE_200 = "200";

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AuthController(
            final AuthenticationManager authenticationManager,
            final UserService userService,
            final AuthService authService,
            final JwtUtil jwtUtil,
            final UserMapper userMapper,
            final ApplicationEventPublisher eventPublisher) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    @Operation(
            summary = "Public user registration",
            description = "Registers a new user, always assigning the USER role to prevent privilege escalation.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "User registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody final RegisterRequest registerRequest) {
        final User registeredUser = userService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(registeredUser, "User registered successfully"));
    }

    @Operation(
            summary = "Administrative user creation",
            description = "Admin-only endpoint to register accounts with custom roles (ADMIN, MANAGER, USER).")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "Administrative user registered successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "Forbidden - Admin authority required")
    })
    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> registerAdminCreated(
            @Valid @RequestBody final RegisterRequest registerRequest) {
        final User registeredUser = userService.registerAdminCreatedUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(registeredUser, "Administrative user registered successfully"));
    }

    @Operation(
            summary = "Authenticate user credentials",
            description = "Validates username and password, returning a JWT access token and a secure refresh token.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Authentication successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody final AuthRequest authRequest) {
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final String accessToken = jwtUtil.generateToken(userDetails);
            final User user = (User) userDetails;

            // Generate database-backed refresh token
            final String refreshToken = authService.createRefreshToken(user);

            final AuthResponse authResponse = userMapper.toAuthResponse(user, accessToken, refreshToken);
            eventPublisher.publishEvent(new LoginSuccessEvent(this, authRequest.getUsername()));

            return ResponseEntity.ok(ApiResponse.success(authResponse, "Authentication successful"));
        } catch (final Exception exception) {
            eventPublisher.publishEvent(new LoginFailureEvent(this, authRequest.getUsername(), exception.getMessage()));
            throw exception;
        }
    }

    @Operation(
            summary = "Refresh access token",
            description = "Accepts a valid refresh token, performs rotation, and returns new tokens.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "Refresh successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody final RefreshTokenRequest request) {
        final AuthResponse authResponse = authService.rotateRefreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Refresh successful"));
    }

    @Operation(summary = "Logout user", description = "Invalidates the provided refresh token in the database.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = CODE_200, description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody final LogoutRequest request) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication != null ? authentication.getName() : "Anonymous";

        authService.revokeRefreshToken(request.getRefreshToken());
        eventPublisher.publishEvent(new LogoutEvent(this, username));

        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @Operation(
            summary = "Get current user profile",
            description = "Returns details of the currently authenticated user context.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = CODE_200,
                description = "User profile loaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = (User) authentication.getPrincipal();

        final UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userResponse, "User profile loaded successfully"));
    }
}
