package com.fitconnect.resource;

import com.fitconnect.IntegrationTestProfile;
import com.fitconnect.entity.User;
import com.fitconnect.entity.UserRole;
import com.fitconnect.repository.UserRepository; // To verify DB state
// import com.google.firebase.cloud.FirestoreClient; // For direct DB access if needed - not typically used in RestAssured tests
// import io.quarkus.test.common.QuarkusTestResource; // Not using a specific QuarkusTestResource for Firestore emulator in this example
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
// import io.quarkus.test.h2.H2DatabaseTestResource; // Not using H2

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
// If using Firestore emulator and have a QuarkusTestResource for it:
// @QuarkusTestResource(FirestoreTestResource.class) // Make sure this is set up in pom.xml and properties
public class AuthResourceTest {

    @Inject
    UserRepository userRepository; // To check DB state

    // Store UIDs to delete them after each test to ensure test isolation
    private String userToDelete = null;

    @AfterEach
    void cleanup() throws ExecutionException, InterruptedException {
        if (userToDelete != null) {
            // Basic check if UserRepository has a delete method.
            // If not, this cleanup won't work without direct Firestore client or adding delete to repo.
            // For more robust tests, a proper delete or emulator reset is needed.
            try {
                 // userRepository.deleteById(userToDelete); // Assuming a deleteById method exists or is added
                 Optional<User> userOptional = userRepository.findById(userToDelete);
                 if(userOptional.isPresent()){
                    // This is a workaround if deleteById is not available.
                    // Not ideal as it means test users might pollute the dev DB if not cleaned.
                    // Best practice: Use Firestore emulator and clear it, or implement delete.
                    System.out.println("Test user " + userToDelete + " was created/used. Manual cleanup or delete method in repository needed for full isolation if not using emulator with reset.");
                 }
            } catch (UnsupportedOperationException e) {
                System.err.println("UserRepository does not support delete. Test users might persist: " + userToDelete);
            } catch (Exception e) {
                 System.err.println("Error during cleanup for user " + userToDelete + ": " + e.getMessage());
            }
            finally {
                userToDelete = null;
            }
        }
    }


    @Test
    public void testInitializeUser_newUser() {
        String testUid = "test-uid-" + UUID.randomUUID().toString();
        String testEmail = "testuser-" + UUID.randomUUID().toString() + "@example.com";
        userToDelete = testUid; // Mark for cleanup

        // Ensure user does not exist (optional pre-check, AfterEach should handle cleanup)
        try {
             Optional<User> existingUser = userRepository.findById(testUid);
             assertFalse(existingUser.isPresent(), "User should not exist before testInitializeUser_newUser");
        } catch (Exception e) {
            // This is fine, means user doesn't exist or repo access failed (which test would also catch)
            System.out.println("Pre-check for user " + testUid + " (optional): " + e.getMessage());
        }


        given()
            .header(com.fitconnect.security.TestAuthMechanism.TEST_UID_HEADER, testUid)
            .header(com.fitconnect.security.TestAuthMechanism.TEST_EMAIL_HEADER, testEmail)
            // TestAuthMechanism will provide roles if TEST_ROLES_HEADER is set.
            // initializeUser service itself assigns CLIENT role for new users.
            // So, roles from header are for SecurityIdentity, not for what initializeUser *persists* for new users.
            .header(com.fitconnect.security.TestAuthMechanism.TEST_ROLES_HEADER, "USER") // Role for TestAuthMechanism
        .when()
            .post("/api/auth/initialize-user")
        .then()
            .statusCode(200)
            .body("id", equalTo(testUid))
            .body("email", equalTo(testEmail))
            .body("role", equalTo(UserRole.CLIENT.name())); // initializeUser sets CLIENT role for new users

        // Verify user is created in Firestore
        try {
            Optional<User> userOptional = userRepository.findById(testUid);
            assertTrue(userOptional.isPresent(), "User should be created in Firestore");
            User createdUser = userOptional.get();
            assertEquals(testEmail, createdUser.getEmail());
            assertEquals(UserRole.CLIENT, createdUser.getRole());
        } catch (Exception e) {
            fail("Error fetching user from repository: " + e.getMessage());
        }
    }

    @Test
    public void testInitializeUser_existingUser() {
        String existingUid = "existing-uid-" + UUID.randomUUID().toString();
        String existingEmail = "existing-" + UUID.randomUUID().toString() + "@example.com";
        userToDelete = existingUid; // Mark for cleanup

        // Pre-create user
        User user = new User();
        user.setId(existingUid);
        user.setEmail(existingEmail);
        user.setRole(UserRole.PROFESSIONAL);
        // user.setName("Existing Pro"); // Name is not set by initializeUser unless passed or from token attribute
        try {
            userRepository.save(user); // This save will set createdAt, and potentially updatedAt if repo logic does that
        } catch (Exception e) {
            fail("Failed to pre-create user: " + e.getMessage());
        }

        given()
            .header(com.fitconnect.security.TestAuthMechanism.TEST_UID_HEADER, existingUid)
            .header(com.fitconnect.security.TestAuthMechanism.TEST_EMAIL_HEADER, existingEmail)
             // Roles in header are for TestAuthMechanism to build identity,
             // initializeUser service should use role from DB if user exists.
            .header(com.fitconnect.security.TestAuthMechanism.TEST_ROLES_HEADER, "PROFESSIONAL") // Role for TestAuthMechanism
        .when()
            .post("/api/auth/initialize-user")
        .then()
            .statusCode(200)
            .body("id", equalTo(existingUid))
            .body("email", equalTo(existingEmail))
            .body("role", equalTo(UserRole.PROFESSIONAL.name()));
            // .body("profileStatus", equalTo(ProfileStatus.PENDING_VERIFICATION.name())); // If Professional status is returned
                                                                                        // and professional part is created by initializeUser
                                                                                        // current initializeUser doesn't create professional part
    }
}
