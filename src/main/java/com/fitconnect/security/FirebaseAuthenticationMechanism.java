package com.fitconnect.security;

import com.fitconnect.entity.User;
import com.fitconnect.entity.UserRole;
import com.fitconnect.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class FirebaseAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger LOGGER = LoggerFactory.getLogger(FirebaseAuthenticationMechanism.class);
    private static final String BEARER_PREFIX = "Bearer ";

    @Inject
    UserRepository userRepository; // To fetch user roles

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        String authHeader = context.request().headers().get("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return Uni.createFrom().optional(Optional.empty()); // No Bearer token, anonymous or other mechanism
        }

        String idTokenString = authHeader.substring(BEARER_PREFIX.length());

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idTokenString);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Fetch user from Firestore to get roles
            // Note: This is a blocking call. For a fully reactive mechanism, this should be offloaded.
            // Quarkus security infrastructure might handle some of this, but good to be aware.
            Optional<User> userOptional = userRepository.findById(uid);

            if (userOptional.isPresent()) {
                User appUser = userOptional.get();
                QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
                builder.setPrincipal(() -> uid); // Or a custom Principal object
                builder.addAttribute("uid", uid);
                builder.addAttribute("email", email);
                if (appUser.getRole() != null) {
                    builder.addRole(appUser.getRole().name());
                } else {
                     LOGGER.warn("User {} found in Firestore but has no role.", uid);
                     builder.addRole("DEFAULT_USER_NO_ROLE"); // Example placeholder
                }
                // Add other attributes like name if needed, e.g. builder.addAttribute("name", appUser.getName());
                return Uni.createFrom().item(builder.build());
            } else {
                // User authenticated by Firebase but not in our DB.
                LOGGER.warn("Firebase token for UID {} verified, but user not found in local DB.", uid);
                // Option 1: Deny access (current)
                return Uni.createFrom().failure(new io.quarkus.security.AuthenticationFailedException("User not found in application database. Please complete your profile."));

                // Option 2: Allow with a special role for profile creation
                // QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder();
                // builder.setPrincipal(() -> uid);
                // builder.addAttribute("uid", uid);
                // builder.addAttribute("email", email);
                // builder.addRole("FIREBASE_AUTHENTICATED_NO_PROFILE");
                // return Uni.createFrom().item(builder.build());
            }

        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            LOGGER.error("Firebase token verification failed: " + e.getMessage(), e);
            if (e.getAuthErrorCode() == com.google.firebase.auth.AuthErrorCode.REVOKED_ID_TOKEN) {
                 return Uni.createFrom().failure(new io.quarkus.security.AuthenticationCompletionException("Token revoked", e));
            }
            return Uni.createFrom().failure(new io.quarkus.security.AuthenticationFailedException("Firebase token verification failed.", e));
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Error fetching user from Firestore: " + e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupt status
            return Uni.createFrom().failure(new io.quarkus.security.AuthenticationFailedException("Error fetching user details.", e));
        } catch (Exception e) { // Catch-all for other unexpected errors
            LOGGER.error("Unexpected error during Firebase authentication: " + e.getMessage(), e);
            return Uni.createFrom().failure(new io.quarkus.security.AuthenticationFailedException("Unexpected authentication error.", e));
        }
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        ChallengeData challenge = new ChallengeData(401, "WWW-Authenticate", "Bearer realm="Protected Area"");
        return Uni.createFrom().item(challenge);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(TokenAuthenticationRequest.class);
    }
}
