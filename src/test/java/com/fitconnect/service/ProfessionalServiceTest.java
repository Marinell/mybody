package com.fitconnect.service;

import com.fitconnect.IntegrationTestProfile;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument;
import com.fitconnect.entity.UserRole;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.repository.ProfessionalDocumentRepository;
import com.fitconnect.repository.ProfessionalRepository;
import com.fitconnect.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class ProfessionalServiceTest {

    @Inject
    ProfessionalService professionalService;

    @InjectMock // Mock the storage service
    FirebaseStorageService mockStorageService;

    @Inject
    UserRepository userRepository;
    @Inject
    ProfessionalRepository professionalRepository;
    @Inject
    ProfessionalDocumentRepository documentRepository;

    String testEmail;
    String testProId; // Will be set after user part is saved

    List<String> userIdsToCleanup = new ArrayList<>();
    // proIdsToCleanup is not strictly needed if professional IDs are same as user IDs and userRepository.delete handles it.
    // For ProfessionalServiceTest, we create Professional which also creates User part.
    // Cleaning up the user ID should be enough if Professional uses same ID.
    // ProfessionalRepository.delete(id) will be called on the same ID if needed.
    List<String> docIdsToCleanup = new ArrayList<>();

    @BeforeEach
    void setUp() {
        testEmail = "pro-service-test-" + UUID.randomUUID().toString() + "@example.com";
        // No user pre-creation here, as registerProfessional handles it.
        // Mock storage service behavior
        try {
            Mockito.when(mockStorageService.uploadFile(any(FileUpload.class), anyString()))
                   .thenAnswer(invocation -> {
                       FileUpload fu = invocation.getArgument(0);
                       String destDir = invocation.getArgument(1);
                       // Ensure a unique path for each mock upload to avoid potential collisions if names were identical
                       return String.format("gs://test-bucket/%s/%s-%s", destDir, UUID.randomUUID().toString(), fu.fileName());
                   });
        } catch (IOException e) {
            fail("Failed to setup mock FirebaseStorageService: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        for (String id : docIdsToCleanup) {
            try { documentRepository.delete(id); }
            catch (Exception e) { System.err.println("Cleanup error for doc " + id + ": " + e.getMessage());}
        }
        docIdsToCleanup.clear();

        // userIdsToCleanup will contain the ID of the professional (since Professional extends User)
        for (String id : userIdsToCleanup) {
            try {
                // Attempt to delete from professional collection first, in case of foreign key like relations or just good practice
                Optional<Professional> proOptional = professionalRepository.findById(id);
                if (proOptional.isPresent()) {
                    professionalRepository.delete(id);
                }
                // Then delete from user collection
                userRepository.delete(id);
            } catch (Exception e) {System.err.println("Cleanup error for user/pro " + id + ": " + e.getMessage());}
        }
        userIdsToCleanup.clear();
    }

    // Mock FileUpload implementation for testing
    static class MockFileUpload implements FileUpload {
        private final String name; // Corresponds to the form field name
        private final Path path; // Path to the (potentially temporary) file on disk
        private final String originalFileName; // The original file name from the client
        private final String contentType;
        private final String charSet;
        private final long size;

        public MockFileUpload(String name, String originalFileName, Path path, String contentType) {
            this.name = name;
            this.originalFileName = originalFileName;
            this.path = path;
            this.contentType = contentType;
            this.charSet = "UTF-8";
            this.size = 0; // Not critical for this mock's purpose if not used by service
        }

        @Override public String name() { return name; } // Form field name
        @Override public Path uploadedFile() { return path; } // Path to temp file
        @Override public String fileName() { return originalFileName; } // Original file name
        @Override public long size() { return size; }
        @Override public String contentType() { return contentType; }
        @Override public String charSet() { return charSet; }
    }


    @Test
    void testRegisterProfessional_withDocuments() throws Exception {
        ProfessionalRegisterRequest request = new ProfessionalRegisterRequest();
        request.setEmail(testEmail);
        request.setName("Service Test Pro");
        request.setPassword("password123");
        request.setProfession("Service Testing");
        request.setAboutYou("About service test pro.");
        request.setQualifications("Highly qualified in testing.");
        request.setYearsOfExperience(5);
        request.setPhoneNumber("1234567890");
        request.setAddress("123 Test St");
        request.setPostalCode("12345");
        // request.setSocialMediaLinksJson("{}"); // Optional


        // Create mock FileUploads
        // These paths don't need to point to actual files on disk for this test,
        // as FirebaseStorageService.uploadFile is mocked and doesn't read from these paths.
        // However, if it did, we'd need to create temporary files.
        Path tempFile1Path = Paths.get("dummy-doc1.pdf");
        Path tempFile2Path = Paths.get("dummy-doc2.jpg");
        FileUpload mockDoc1 = new MockFileUpload("documents", "dummy-doc1.pdf", tempFile1Path, "application/pdf");
        FileUpload mockDoc2 = new MockFileUpload("documents", "dummy-doc2.jpg", tempFile2Path, "image/jpeg");

        List<FileUpload> documents = new ArrayList<>();
        documents.add(mockDoc1);
        documents.add(mockDoc2);
        request.setDocuments(documents);

        Professional registeredPro = professionalService.registerProfessional(request);
        assertNotNull(registeredPro, "Registered professional should not be null");
        assertNotNull(registeredPro.getId(), "Registered professional ID should not be null");
        testProId = registeredPro.getId();
        userIdsToCleanup.add(testProId); // Add ID for cleanup


        assertEquals(testEmail, registeredPro.getEmail());
        assertEquals("Service Test Pro", registeredPro.getName());
        assertEquals(UserRole.PROFESSIONAL, registeredPro.getRole());
        assertEquals(ProfileStatus.PENDING_VERIFICATION, registeredPro.getProfileStatus());

        // Verify FirebaseStorageService.uploadFile was called for each document
        Mockito.verify(mockStorageService, Mockito.times(2)).uploadFile(any(FileUpload.class), Mockito.eq("professional-documents"));

        // Verify ProfessionalDocument metadata in Firestore
        assertNotNull(registeredPro.getProfessionalDocumentReferences(), "Document references list should not be null");
        assertEquals(2, registeredPro.getProfessionalDocumentReferences().size(), "Should have two document references");

        for (String docId : registeredPro.getProfessionalDocumentReferences()) {
            docIdsToCleanup.add(docId);
            Optional<ProfessionalDocument> docOptional = documentRepository.findById(docId);
            assertTrue(docOptional.isPresent(), "Document metadata with ID " + docId + " should be saved in Firestore.");
            ProfessionalDocument savedDoc = docOptional.get();
            assertEquals(testProId, savedDoc.getProfessionalId());
            assertTrue(savedDoc.getStoragePath().startsWith("gs://test-bucket/professional-documents/"), "Storage path should start with gs:// prefix and bucket");
            // Check original file names based on the mock setup
            boolean nameMatch = savedDoc.getFileName().equals("dummy-doc1.pdf") || savedDoc.getFileName().equals("dummy-doc2.jpg");
            assertTrue(nameMatch, "Unexpected document filename: " + savedDoc.getFileName());
        }

        // Verify User and Professional entities were created
        assertTrue(userRepository.findById(testProId).isPresent(), "User part of professional should be in DB");
        assertTrue(professionalRepository.findById(testProId).isPresent(), "Professional part should be in DB");
    }
}
