package com.boit.boit_leave.service;

import com.boit.boit_leave.entity.LeaveActionToken;
import com.boit.boit_leave.entity.LeaveActionType;
import com.boit.boit_leave.entity.LeaveRequest;
import com.boit.boit_leave.entity.User;
import com.boit.boit_leave.repository.LeaveActionTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeaveActionTokenService {

    private final LeaveActionTokenRepository tokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates a secure action token.
     *
     * The raw token is returned to the caller so it can be
     * placed in the email URL.
     *
     * Only the SHA-256 hash is stored in the database.
     */
    @Transactional
    public String createToken(
            LeaveRequest leaveRequest,
            User manager,
            LeaveActionType action,
            LocalDateTime expiresAt
    ) {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String rawToken =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        LeaveActionToken token =
                LeaveActionToken.builder()
                        .leaveRequest(leaveRequest)
                        .manager(manager)
                        .tokenHash(tokenHash)
                        .action(action)
                        .expiresAt(expiresAt)
                        .used(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        tokenRepository.save(token);

        return rawToken;
    }
    
    @Transactional
    public Map<LeaveActionType, String> createActionTokens(
            LeaveRequest leaveRequest,
            User manager,
            LocalDateTime expiresAt
    ) {

        Map<LeaveActionType, String> tokens =
                new EnumMap<>(LeaveActionType.class);

        for (LeaveActionType action : LeaveActionType.values()) {

            String rawToken = createToken(
                    leaveRequest,
                    manager,
                    action,
                    expiresAt
            );

            tokens.put(action, rawToken);
        }

        return tokens;
    }

    /**
     * Validates and returns an unused token.
     */
    @Transactional(readOnly = true)
    public LeaveActionToken validateToken(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Action token is required");
        }

        String tokenHash = hashToken(rawToken);

        LeaveActionToken token =
                tokenRepository
                        .findByTokenHashAndUsedFalse(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or already used action token"
                                )
                        );

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Action token has expired"
            );
        }

        /*
         * Initialize required lazy relationships
         * while the Hibernate session is still active.
         */
        token.getLeaveRequest().getId();

        token.getLeaveRequest()
                .getUser()
                .getFullName();

        token.getManager().getId();

        return token;
    }

    /**
     * Marks a token as used.
     */
    @Transactional
    public void markAsUsed(
            LeaveActionToken token
    ) {

        token.setUsed(true);

        token.setUsedAt(
                LocalDateTime.now()
        );

        tokenRepository.save(token);
    }
    
   
    /**
     * Invalidates all unused action tokens for a specific
     * leave request and approver.
     */
    @Transactional
    public void invalidateUnusedTokens(
            Long leaveRequestId,
            Long managerId
    ) {
        java.util.List<LeaveActionToken> tokens =
                tokenRepository
                        .findByLeaveRequestIdAndManagerIdAndUsedFalse(
                                leaveRequestId,
                                managerId
                        );

        for (LeaveActionToken token : tokens) {
            token.setUsed(true);
            token.setUsedAt(LocalDateTime.now());
        }

        tokenRepository.saveAll(tokens);
    }
    

    /**
     * SHA-256 hash used for secure database storage.
     */
    private String hashToken(
            String rawToken
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {

                hex.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    ex
            );
        }
    }
}