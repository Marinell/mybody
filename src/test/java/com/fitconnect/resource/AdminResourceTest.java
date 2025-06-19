package com.fitconnect.resource;

import com.fitconnect.IntegrationTestProfile;
import com.fitconnect.dto.ProfileVerificationRequest;
import com.fitconnect.entity.*;
import com.fitconnect.repository.*;
import com.fitconnect.security.TestAuthMechanism;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class AdminResourceTest {

    @Inject UserRepository userRepository;
    @Inject ProfessionalRepository professionalRepository;
    @Inject ProfessionalDocumentRepository documentRepository;

    User testAdmin;
    User nonAdminUser;
    Professional proPending;
    Professional proVerified;
    Professional proRejected;
    Professional proWithDocs;

    List<String> userIdsToCleanup = new ArrayList<>();
    List<String> proIdsToCleanup = new ArrayList<>(); // professionalRepository uses same ID as user
    List<String> docIdsToCleanup = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        // Admin User
        String adminId = "admin-uid-" + UUID.randomUUID().toString();
        testAdmin = new User();
        testAdmin.setId(adminId);
        testAdmin.setEmail("admin@example.com");
        testAdmin.setRole(UserRole.ADMIN);
        testAdmin.setName("Test Admin");
        userRepository.save(testAdmin);
        userIdsToCleanup.add(adminId);

        // Non-Admin User (Client)
        String nonAdminId = "client-uid-" + UUID.randomUUID().toString();
        nonAdminUser = new User();
        nonAdminUser.setId(nonAdminId);
        nonAdminUser.setEmail("client@example.com");
        nonAdminUser.setRole(UserRole.CLIENT);
        nonAdminUser.setName("Test Client");
        userRepository.save(nonAdminUser);
        userIdsToCleanup.add(nonAdminId);


        // Professional - Pending
        proPending = createAndSaveProfessional("pending-" + UUID.randomUUID().toString(), "Pending Pro", ProfileStatus.PENDING_VERIFICATION);

        // Professional - Verified
        proVerified = createAndSaveProfessional("verified-" + UUID.randomUUID().toString(), "Verified Pro", ProfileStatus.VERIFIED);

        // Professional - Rejected
        proRejected = createAndSaveProfessional("rejected-" + UUID.randomUUID().toString(), "Rejected Pro", ProfileStatus.REJECTED);

        // Professional - With Documents
        proWithDocs = createAndSaveProfessional("docs-" + UUID.randomUUID().toString(), "Pro With Docs", ProfileStatus.PENDING_VERIFICATION);
        ProfessionalDocument doc1 = createAndSaveDocument(proWithDocs.getId(), "doc1.pdf", "dummy/path1");
        ProfessionalDocument doc2 = createAndSaveDocument(proWithDocs.getId(), "doc2.jpg", "dummy/path2");

        // Simulating how professionalDocumentReferences would be populated
        List<String> docRefs = new ArrayList<>();
        if (doc1 != null && doc1.getId() != null) docRefs.add(doc1.getId());
        if (doc2 != null && doc2.getId() != null) docRefs.add(doc2.getId());
        if (!docRefs.isEmpty()) {
            proWithDocs.setProfessionalDocumentReferences(docRefs);
            professionalRepository.save(proWithDocs); // Resave with doc references
        }
    }

    Professional createAndSaveProfessional(String id, String name, ProfileStatus status) throws Exception {
        Professional pro = new Professional();
        pro.setId(id);
        pro.setEmail(id + "@example.com");
        pro.setName(name);
        pro.setRole(UserRole.PROFESSIONAL); // Professional constructor sets this
        pro.setProfileStatus(status);       // Professional constructor sets this to PENDING if not overridden
        pro.setProfession("Test Profession");
        pro.setCreatedAt(LocalDateTime.now().minusDays(1));
        pro.setUpdatedAt(LocalDateTime.now().minusDays(1));

        userRepository.save(pro);
        professionalRepository.save(pro);
        userIdsToCleanup.add(id);
        // proIdsToCleanup is not strictly needed if professional IDs are same as user IDs and userRepository.delete handles it.
        // However, if professional collection might have different lifecycle or specific cleanup, keep it.
        // For now, assuming professional ID = user ID.
        return pro;
    }

    ProfessionalDocument createAndSaveDocument(String professionalId, String fileName, String storagePath) throws Exception {
        ProfessionalDocument doc = new ProfessionalDocument();
        doc.setProfessionalId(professionalId);
        doc.setFileName(fileName);
        doc.setFileType("application/octet-stream"); // Dummy type
        doc.setStoragePath(storagePath);
        ProfessionalDocument savedDoc = documentRepository.save(doc);
        docIdsToCleanup.add(savedDoc.getId());
        return savedDoc;
    }

    @AfterEach
    void tearDown() throws Exception {
        for(String id : docIdsToCleanup) { try { documentRepository.delete(id); } catch (Exception e) { System.err.println("Cleanup error for doc " + id + ": " + e.getMessage());} }
        docIdsToCleanup.clear();
        // proIdsToCleanup is not iterated because professional IDs are same as user IDs being cleaned up
        // If they were different, professionalRepository.delete would be needed here for each proId.
        for(String id : userIdsToCleanup) {
            try {
                // If professional data is separate and uses same ID, delete it first or ensure cascade if relations existed
                Optional<Professional> proOptional = professionalRepository.findById(id);
                if (proOptional.isPresent()) {
                    professionalRepository.delete(id);
                }
                userRepository.delete(id);
            } catch (Exception e) {System.err.println("Cleanup error for user/pro " + id + ": " + e.getMessage());}
        }
        userIdsToCleanup.clear();
    }

    @Test
    void testGetProfessionalsPendingVerification_asAdmin_success() {
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testAdmin.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.ADMIN.name())
        .when()
            .get("/api/admin/professionals/pending-verification")
        .then()
            .statusCode(200)
            .body("$", hasSize(2)) // proPending and proWithDocs
            .body("find { it.id == '%s' }.name", withArgs(proPending.getId()), equalTo("Pending Pro"))
            .body("find { it.id == '%s' }.name", withArgs(proWithDocs.getId()), equalTo("Pro With Docs"));
    }

    @Test
    void testGetProfessionalsPendingVerification_asNonAdmin_forbidden() {
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, nonAdminUser.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
        .when()
            .get("/api/admin/professionals/pending-verification")
        .then()
            .statusCode(403);
    }

    @Test
    void testVerifyProfessionalProfile_asAdmin_success() throws Exception {
        ProfileVerificationRequest request = new ProfileVerificationRequest();
        request.setNewStatus(ProfileStatus.VERIFIED);

        Professional proBeforeUpdate = professionalRepository.findById(proPending.getId()).orElseThrow();
        LocalDateTime originalUpdatedAt = proBeforeUpdate.getUpdatedAt();

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testAdmin.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.ADMIN.name())
            .contentType("application/json")
            .body(request)
            .pathParam("id", proPending.getId())
        .when()
            .post("/api/admin/professionals/{id}/verify")
        .then()
            .statusCode(200)
            .body("id", equalTo(proPending.getId()))
            .body("profileStatus", equalTo(ProfileStatus.VERIFIED.name()));

        Professional proAfterUpdate = professionalRepository.findById(proPending.getId()).orElseThrow();
        assertEquals(ProfileStatus.VERIFIED, proAfterUpdate.getProfileStatus());
        assertNotNull(proAfterUpdate.getUpdatedAt());
        assertTrue(proAfterUpdate.getUpdatedAt().isAfter(originalUpdatedAt), "UpdatedAt should be more recent.");
    }

    @Test
    void testVerifyProfessionalProfile_asNonAdmin_forbidden() {
        ProfileVerificationRequest request = new ProfileVerificationRequest();
        request.setNewStatus(ProfileStatus.VERIFIED);

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, nonAdminUser.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .contentType("application/json")
            .body(request)
            .pathParam("id", proPending.getId())
        .when()
            .post("/api/admin/professionals/{id}/verify")
        .then()
            .statusCode(403);
    }

    @Test
    void testVerifyProfessionalProfile_notFound() {
        ProfileVerificationRequest request = new ProfileVerificationRequest();
        request.setNewStatus(ProfileStatus.VERIFIED);
        String nonExistentId = "non-existent-pro-id";

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testAdmin.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.ADMIN.name())
            .contentType("application/json")
            .body(request)
            .pathParam("id", nonExistentId)
        .when()
            .post("/api/admin/professionals/{id}/verify")
        .then()
            .statusCode(404);
    }

    @Test
    void testVerifyProfessionalProfile_invalidStatus() {
        // ProfileVerificationRequest does not have a direct status field to set to something invalid like "INVALID_STATUS"
        // The validation for newStatus (VERIFIED or REJECTED) is in AdminService.verifyProfessionalProfile
        // So, here we test the service's IllegalArgumentException if it were possible to send a bad status value
        // However, ProfileVerificationRequest uses ProfileStatus enum, so it's hard to send an "invalid" enum value via JSON directly
        // unless the enum deserialization fails or a different DTO is used.
        // For now, this case is more about the service logic than resource input validation if DTO uses enum.
        // If the DTO took a String, then we could test invalid string.
        // Let's test the "null status" case which is checked by the resource.
         given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testAdmin.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.ADMIN.name())
            .contentType("application/json")
            .body("{\"newStatus\":null}") // Send null status
            .pathParam("id", proPending.getId())
        .when()
            .post("/api/admin/professionals/{id}/verify")
        .then()
            .statusCode(400); // Bad Request due to missing newStatus
    }


    @Test
    void testGetProfessionalDocuments_asAdmin_success() {
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testAdmin.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.ADMIN.name())
            .pathParam("id", proWithDocs.getId())
        .when()
            .get("/api/admin/professionals/{id}/documents")
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("fileName", hasItems("doc1.pdf", "doc2.jpg"));
    }

    @Test
    void testGetProfessionalDocuments_asNonAdmin_forbidden() {
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, nonAdminUser.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .pathParam("id", proWithDocs.getId())
        .when()
            .get("/api/admin/professionals/{id}/documents")
        .then()
            .statusCode(403);
    }
     @Test
    void testGetProfessionalDocuments_professionalNotFound() {
        String nonExistentProId = "pro-id-does-not-exist";
        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testAdmin.getId())
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.ADMIN.name())
            .pathParam("id", nonExistentProId)
        .when()
            .get("/api/admin/professionals/{id}/documents")
        .then()
            .statusCode(404); // AdminService.getProfessionalDocuments throws NotFoundException
    }
}
