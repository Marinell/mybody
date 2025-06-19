package com.fitconnect.resource;

import com.fitconnect.IntegrationTestProfile;
import com.fitconnect.dto.ProfessionalProfileUpdateDTO;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.UserRole;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.repository.ProfessionalRepository;
import com.fitconnect.repository.UserRepository;
import com.fitconnect.security.TestAuthMechanism;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class ProfessionalResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    ProfessionalRepository professionalRepository;

    String testProUid;
    String testProEmail;

    @BeforeEach
    void setUp() throws Exception {
        testProUid = "pro-uid-" + UUID.randomUUID().toString();
        testProEmail = "pro-" + UUID.randomUUID().toString() + "@example.com";

        // Base professional for most tests
        Professional professional = createProfessional(testProUid, testProEmail, "Test Professional", UserRole.PROFESSIONAL, ProfileStatus.PENDING_VERIFICATION, "Tester");

        // Ensure this professional is saved for tests that modify/fetch this specific one
        saveProfessional(professional);
    }

    // Helper to create professional consistently
    private Professional createProfessional(String id, String email, String name, UserRole role, ProfileStatus status, String profession) {
        Professional professional = new Professional();
        professional.setId(id);
        professional.setEmail(email);
        professional.setName(name);
        professional.setRole(role);
        professional.setProfileStatus(status);
        professional.setProfession(profession);
        professional.setCreatedAt(java.time.LocalDateTime.now().minusDays(2)); // Ensure createdAt is distinct
        professional.setUpdatedAt(java.time.LocalDateTime.now().minusDays(1));
        return professional;
    }

    // Helper to save/update professional in both repositories
    private void saveProfessional(Professional professional) throws ExecutionException, InterruptedException {
        userRepository.save(professional);
        professionalRepository.save(professional);
    }


    @AfterEach
    void tearDown() throws Exception {
        // Cleanup
        try {
            professionalRepository.delete(testProUid);
        } catch (Exception e) {
            System.err.println("Error deleting professional " + testProUid + ": " + e.getMessage());
        }
        try {
            userRepository.delete(testProUid);
        } catch (Exception e) {
            System.err.println("Error deleting user " + testProUid + ": " + e.getMessage());
        }
    }


    @Test
    void testGetMyFullProfile_success() {
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testProUid)
            .header(TestAuthMechanism.TEST_EMAIL_HEADER, testProEmail)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.PROFESSIONAL.name())
        .when()
            .get("/api/professionals/me")
        .then()
            .statusCode(200)
            .body("id", equalTo(testProUid))
            .body("email", equalTo(testProEmail))
            .body("name", equalTo("Test Professional"))
            .body("profession", equalTo("Tester"))
            .body("profileStatus", equalTo(ProfileStatus.PENDING_VERIFICATION.name()));
    }

    @Test
    void testGetMyFullProfile_unauthorized() {
        given()
            // No auth headers
        .when()
            .get("/api/professionals/me")
        .then()
            .statusCode(401); // Should be challenged by TestAuthMechanism
    }

    @Test
    void testGetMyFullProfile_notAProfessional() {
         String clientUid = "client-uid-" + UUID.randomUUID().toString();
        // No need to create this user in DB for this test, as TestAuthMechanism creates identity on the fly
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, clientUid)
            .header(TestAuthMechanism.TEST_EMAIL_HEADER, "client@example.com")
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name()) // Wrong role
        .when()
            .get("/api/professionals/me")
        .then()
            .statusCode(403); // Quarkus default is 403 Forbidden if @RolesAllowed fails
    }


    @Test
    void testUpdateMyProfile_success() throws ExecutionException, InterruptedException {
        ProfessionalProfileUpdateDTO updateDTO = new ProfessionalProfileUpdateDTO();
        updateDTO.setName("Updated Test Professional");
        updateDTO.setProfession("Senior Tester");
        updateDTO.setAboutYou("Updated about section.");
        Map<String, String> socialLinks = new HashMap<>();
        socialLinks.put("LINKEDIN", "http://linkedin.com/updated");
        updateDTO.setSocialMediaLinks(socialLinks);

        // Fetch original updatedAt time
        Professional originalPro = professionalRepository.findById(testProUid)
                                    .orElseThrow(() -> new AssertionError("Test professional not found before update"));
        java.time.LocalDateTime originalUpdatedAt = originalPro.getUpdatedAt();
        Assertions.assertNotNull(originalUpdatedAt, "Original updatedAt should not be null");


        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testProUid)
            .header(TestAuthMechanism.TEST_EMAIL_HEADER, testProEmail)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.PROFESSIONAL.name())
            .contentType("application/json")
            .body(updateDTO)
        .when()
            .put("/api/professionals/me")
        .then()
            .statusCode(200)
            .body("id", equalTo(testProUid))
            .body("name", equalTo("Updated Test Professional"))
            .body("profession", equalTo("Senior Tester"))
            .body("aboutYou", equalTo("Updated about section."))
            .body("socialMediaLinks.LINKEDIN", equalTo("http://linkedin.com/updated"));

        // Verify in DB
        Optional<Professional> proOptional = professionalRepository.findById(testProUid);
        assertTrue(proOptional.isPresent(), "Professional should exist in DB after update");
        Professional updatedPro = proOptional.get();
        assertEquals("Updated Test Professional", updatedPro.getName());
        assertEquals("Senior Tester", updatedPro.getProfession());

        // Verify User part was also updated (since Professional extends User)
        Optional<User> userOptional = userRepository.findById(testProUid);
        assertTrue(userOptional.isPresent(), "User part should exist in DB after update");
        assertEquals("Updated Test Professional", userOptional.get().getName());

        // Verify updatedAt was changed
        java.time.LocalDateTime newUpdatedAt = updatedPro.getUpdatedAt();
        Assertions.assertNotNull(newUpdatedAt, "New updatedAt should not be null");
        // Ensure originalUpdatedAt was fetched *before* the update call.
        // If originalPro was fetched after the update, this assertion might be tricky due to millisecond differences or if save is too fast.
        // The current setup fetches originalPro before the PUT call, so this should be mostly reliable.
        Assertions.assertTrue(newUpdatedAt.isAfter(originalUpdatedAt), "New updatedAt ("+ newUpdatedAt +") should be after original updatedAt ("+ originalUpdatedAt +").");
    }

    // --- Tests for GET /api/professionals/{id}/client-view ---

    @Test
    void testGetProfessionalPublicProfile_verified() throws Exception {
        // Ensure professional is VERIFIED for this test
        Professional pro = professionalRepository.findById(testProUid).orElseThrow();
        pro.setProfileStatus(ProfileStatus.VERIFIED);
        pro.setAboutYou("Public about me.");
        // pro.setSkillNames(List.of("Skill1", "Skill2")); // Assuming skillNames is handled
        saveProfessional(pro);

        String clientViewingUid = "client-viewer-uid-" + UUID.randomUUID().toString();

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, clientViewingUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .pathParam("id", testProUid)
        .when()
            .get("/api/professionals/{id}/client-view")
        .then()
            .statusCode(200)
            .body("id", equalTo(testProUid))
            .body("name", equalTo("Test Professional"))
            .body("profession", equalTo("Tester"))
            .body("aboutYou", equalTo("Public about me."))
            // .body("skills", containsInAnyOrder("Skill1", "Skill2")) // If skills are part of PublicProfileDTO
            .body("email", nullValue()) // Email should not be in public profile
            .body("phoneNumber", nullValue()); // Phone number should not be in public profile
    }

    @Test
    void testGetProfessionalPublicProfile_nonVerified() throws Exception {
        // Ensure professional is PENDING_VERIFICATION (default from setUp)
        Professional pro = professionalRepository.findById(testProUid).orElseThrow();
        assertEquals(ProfileStatus.PENDING_VERIFICATION, pro.getProfileStatus(), "Professional should be PENDING_VERIFICATION for this test");

        String clientViewingUid = "client-viewer-uid-" + UUID.randomUUID().toString();

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, clientViewingUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .pathParam("id", testProUid)
        .when()
            .get("/api/professionals/{id}/client-view")
        .then()
            .statusCode(404); // As per resource logic: "Professional profile not found or not verified."
    }

    @Test
    void testGetProfessionalPublicProfile_notFound() {
        String nonExistentUid = "non-existent-uid-" + UUID.randomUUID().toString();
        String clientViewingUid = "client-viewer-uid-" + UUID.randomUUID().toString();

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, clientViewingUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .pathParam("id", nonExistentUid)
        .when()
            .get("/api/professionals/{id}/client-view")
        .then()
            .statusCode(404); // Service getProfessionalById throws NotFoundException
    }
}
