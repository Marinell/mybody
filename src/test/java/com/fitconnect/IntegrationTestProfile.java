package com.fitconnect;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Set;
import java.util.Map;

public class IntegrationTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        // Enable TestAuthMechanism instead of FirebaseAuthenticationMechanism for tests
        return Set.of(com.fitconnect.security.TestAuthMechanism.class);
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            // Disable actual Firebase SDK initialization if it interferes, or point to emulators
            // "quarkus.firebase.service-account-file", "test-firebase-service-account.json", // if needed
            // Ensure default mechanism is not picked up if TestAuthMechanism is active
            // "quarkus.http.auth.policy.user-authenticated.auth-mechanism", "firebase", // Keep this or use a name TestAuthMechanism provides
            // The line above might not be necessary if @Alternative and @Priority work as expected.
            // If FirebaseAuthenticationMechanism is also ApplicationScoped, @Alternative should replace it.
            "quarkus.profile", "test" // General test profile property
            // To use Firestore emulator, you'd add:
            // "quarkus.google.cloud.firestore.emulator-host", "localhost:8080"
            // (ensure your pom.xml has com.google.cloud:google-cloud-firestore dependency for this property to be effective)
            // and potentially "quarkus.google.cloud.project-id", "test-project"
        );
    }
}
