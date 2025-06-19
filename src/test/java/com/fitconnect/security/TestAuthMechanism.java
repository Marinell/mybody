package com.fitconnect.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Alternative // Marks this as an alternative to be used in specific profiles
@Priority(1) // Ensures it's preferred over the default FirebaseAuthenticationMechanism when active
@ApplicationScoped
public class TestAuthMechanism implements HttpAuthenticationMechanism {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAuthMechanism.class);
    public static final String TEST_UID_HEADER = "X-Test-UID";
    public static final String TEST_EMAIL_HEADER = "X-Test-Email";
    public static final String TEST_ROLES_HEADER = "X-Test-Roles"; // Comma-separated

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        String uid = context.request().headers().get(TEST_UID_HEADER);
        String email = context.request().headers().get(TEST_EMAIL_HEADER);
        String rolesHeader = context.request().headers().get(TEST_ROLES_HEADER);

        if (uid != null) {
            LOGGER.info("TestAuthMechanism: Authenticating test user UID: {}, Email: {}, Roles: {}", uid, email, rolesHeader);
            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
            builder.setPrincipal(() -> uid);
            builder.addAttribute("uid", uid);
            if (email != null) {
                builder.addAttribute("email", email);
            }
            if (rolesHeader != null && !rolesHeader.isEmpty()) {
                Arrays.stream(rolesHeader.split(","))
                      .map(String::trim)
                      .forEach(builder::addRole);
            } else {
                builder.addRole("USER"); // Default role if none specified for test
            }
            return Uni.createFrom().item(builder.build());
        }
        // No test headers, proceed as anonymous or let other mechanisms try
        return Uni.createFrom().optional(Optional.empty());
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(401, "WWW-Authenticate", "Bearer realm="Test""));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Set.of(); // Not using standard credential types
    }
}
