package com.klb.account_service.service.impl;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.klb.account_service.constant.PredefinedRole;
import com.klb.account_service.dto.request.KeycloakCredential;
import com.klb.account_service.dto.request.KeycloakUserCreationRequest;
import com.klb.account_service.dto.request.TokenExchangeRequest;
import com.klb.account_service.dto.request.UserCreationRequest;
import com.klb.account_service.dto.request.UserUpdateRequest;
import com.klb.account_service.dto.response.TokenExchangeResponse;
import com.klb.account_service.dto.response.UserResponse;
import com.klb.account_service.entity.Role;
import com.klb.account_service.entity.User;
import com.klb.account_service.exception.AppException;
import com.klb.account_service.exception.ErrorCode;
import com.klb.account_service.mapper.ProfileMapper;
import com.klb.account_service.mapper.UserMapper;
import com.klb.account_service.repository.RoleRepository;
import com.klb.account_service.repository.UserRepository;
import com.klb.account_service.repository.httpclient.KeycloakClient;
import com.klb.event.dto.NotificationEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    ProfileMapper profileMapper;
    PasswordEncoder passwordEncoder;
    KafkaTemplate<String, Object> kafkaTemplate;
    KeycloakClient keycloakClient;

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();

        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setEmailVerified(false);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Tạo user trong Keycloak
        try {
            createKeycloakUser(request);
            log.info("User created successfully in Keycloak: {}", request.getUsername());
        } catch (Exception e) {
            log.error("Failed to create user in Keycloak: {}", e.getMessage());
            userRepository.delete(user);
            throw new AppException(ErrorCode.Error_CREATING_KEYCLOAK_USER);
        }

        var profileRequest = profileMapper.toProfileCreationRequest(request);
        profileRequest.setUserId(user.getId());

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(request.getEmail())
                .subject("Welcome to klb")
                .body("Hello, " + request.getUsername())
                .build();

        // Publish message to kafka
        kafkaTemplate.send("notification-delivery", notificationEvent);

        return userMapper.toUserResponse(user);
    }

    private void createKeycloakUser(UserCreationRequest request) {
        // Lấy admin token để có quyền tạo user
        String adminToken = getAdminToken();

        // Tạo credential cho user
        KeycloakCredential credential = KeycloakCredential.builder()
                .type("password")
                .value(request.getPassword())
                .temporary(false)
                .build();

        // Tạo Keycloak user creation request
        KeycloakUserCreationRequest keycloakRequest = KeycloakUserCreationRequest.builder()
                .username(request.getUsername())
                .enabled(true)
                .email(request.getEmail())
                .emailVerified(false)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .credentials(List.of(credential))
                .build();

        // Gọi API tạo user trong Keycloak
        Boolean result = keycloakClient
                .createUser("Bearer " + adminToken, keycloakRequest)
                .block();

        if (result == null || !result) {
            throw new RuntimeException("Failed to create user in Keycloak");
        }
    }

    private String getAdminToken() {
        // Lấy token từ SecurityContext
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            try {
                // Lấy JWT token trực tiếp từ authentication
                if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
                    return jwtAuthToken.getToken().getTokenValue();
                }
            } catch (Exception e) {
                log.warn("Could not extract token from authentication: {}", e.getMessage());
            }
        }

        // Fallback: Tạo request để lấy admin token nếu không có trong SecurityContext
        log.warn("No JWT token found in SecurityContext, falling back to client_credentials");
        TokenExchangeRequest tokenRequest = TokenExchangeRequest.builder()
                .grant_type("client_credentials")
                .client_id("klb")
                .client_secret("qzmX9MnaaNsm37yvDdIqYIPwhu4nrYNy")
                .scope("openid")
                .build();

        return keycloakClient
                .exchangeToken(tokenRequest)
                .map(TokenExchangeResponse::getAccessToken)
                .block();
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
