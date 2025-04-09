package com.cs301.client_service.utils;

import com.cs301.client_service.exceptions.UnauthorizedAccessException;
import com.cs301.client_service.models.Account;
import com.cs301.client_service.models.Client;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthorizationUtil {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_AGENT = "ROLE_AGENT";
    private static final String JWT_SUBJECT_CLAIM = "sub";

    private JwtAuthorizationUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if the authenticated user has the ADMIN role
     * @param authentication The authentication object
     * @return true if the user has the ADMIN role
     */
    public static boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
    }

    /**
     * Check if the authenticated user has the AGENT role
     * @param authentication The authentication object
     * @return true if the user has the AGENT role
     */
    public static boolean isAgent(Authentication authentication) {
        if (authentication == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(a -> ROLE_AGENT.equals(a.getAuthority()));
    }

    /**
     * Check if the agent from JWT can access the specified client
     * @param authentication The authentication object
     * @param client The client to check access for
     * @throws UnauthorizedAccessException if the agent does not have access to the client
     */
    public static void validateAgentAccess(Authentication authentication, Client client) {
        if (authentication == null || client == null) {
            throw new UnauthorizedAccessException("Invalid authentication or client");
        }

        // Admin can access any client
        if (isAdmin(authentication)) {
            return;
        }

        // Agent can only access their own clients
        if (isAgent(authentication)) {
            String agentId = JWTUtil.getClaim(authentication, JWT_SUBJECT_CLAIM);
            if (!agentId.equals(client.getAgentId())) {
                throw new UnauthorizedAccessException("Agent does not have access to this client");
            }
            return;
        }

        // If not admin or agent, deny access
        throw new UnauthorizedAccessException("Insufficient permissions to access client data");
    }

    /**
     * Check if the agent from JWT can access the specified account
     * @param authentication The authentication object
     * @param account The account to check access for
     * @throws UnauthorizedAccessException if the agent does not have access to the account
     */
    public static void validateAccountAccess(Authentication authentication, Account account) {
        if (authentication == null || account == null || account.getClient() == null) {
            throw new UnauthorizedAccessException("Invalid authentication or account data");
        }

        // Admin can access any account
        if (isAdmin(authentication)) {
            return;
        }

        // Agent can only access accounts of their clients
        if (isAgent(authentication)) {
            String agentId = JWTUtil.getClaim(authentication, JWT_SUBJECT_CLAIM);
            if (!agentId.equals(account.getClient().getAgentId())) {
                throw new UnauthorizedAccessException("Agent does not have access to this account");
            }
            return;
        }

        // If not admin or agent, deny access
        throw new UnauthorizedAccessException("Insufficient permissions to access account data");
    }

    /**
     * Get the agent ID from the JWT's subject claim
     * @param authentication The authentication object
     * @return The agent ID from the JWT
     */
    public static String getAgentId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication cannot be null");
        }
        
        return JWTUtil.getClaim(authentication, JWT_SUBJECT_CLAIM);
    }
} 