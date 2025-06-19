package com.fitconnect.resource;

import com.fitconnect.IntegrationTestProfile;
import com.fitconnect.dto.ClientSelectProfessionalRequestDTO;
import com.fitconnect.dto.ServiceRequestInputDTO;
import com.fitconnect.dto.AppointmentDTO;
import com.fitconnect.entity.*;
import com.fitconnect.repository.*;
import com.fitconnect.security.TestAuthMechanism;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class ServiceRequestResourceTest {

    @Inject UserRepository userRepository;
    @Inject ProfessionalRepository professionalRepository;
    @Inject ServiceRequestRepository serviceRequestRepository;
    @Inject AppointmentRepository appointmentRepository;

    String testClientUid;
    String testClientEmail;
    User testClient;

    String testProUid;
    String testProEmail;
    Professional testProfessional;

    List<String> serviceRequestIdsToCleanup = new ArrayList<>();
    List<String> appointmentIdsToCleanup = new ArrayList<>();


    @BeforeEach
    void setUp() throws Exception {
        testClientUid = "client-uid-" + UUID.randomUUID().toString();
        testClientEmail = "client-" + UUID.randomUUID().toString() + "@example.com";
        testClient = new Client(); // Client extends User and sets role
        testClient.setId(testClientUid);
        testClient.setEmail(testClientEmail);
        testClient.setName("Test Client");
        // testClient.setRole(UserRole.CLIENT); // Set by Client constructor
        userRepository.save(testClient);

        testProUid = "pro-uid-" + UUID.randomUUID().toString();
        testProEmail = "pro-" + UUID.randomUUID().toString() + "@example.com";
        testProfessional = new Professional(); // Professional extends User and sets role
        testProfessional.setId(testProUid);
        testProfessional.setEmail(testProEmail);
        testProfessional.setName("Test Professional");
        // testProfessional.setRole(UserRole.PROFESSIONAL); // Set by Professional constructor
        testProfessional.setProfileStatus(ProfileStatus.VERIFIED); // Verified professional
        testProfessional.setProfession("Testology");
        userRepository.save(testProfessional);
        professionalRepository.save(testProfessional);
    }

    @AfterEach
    void tearDown() throws Exception {
        for (String id : appointmentIdsToCleanup) { try { appointmentRepository.delete(id); } catch (Exception e) {System.err.println("Cleanup error for appointment " + id + ": " + e.getMessage());} }
        appointmentIdsToCleanup.clear();
        for (String id : serviceRequestIdsToCleanup) { try { serviceRequestRepository.delete(id); } catch (Exception e) {System.err.println("Cleanup error for service request " + id + ": " + e.getMessage());} }
        serviceRequestIdsToCleanup.clear();

        if (testClientUid != null) try { userRepository.delete(testClientUid); } catch (Exception e) {System.err.println("Cleanup error for client " + testClientUid + ": " + e.getMessage());}
        if (testProUid != null) {
             try { professionalRepository.delete(testProUid); } catch (Exception e) {System.err.println("Cleanup error for professional " + testProUid + ": " + e.getMessage());}
             try { userRepository.delete(testProUid); } catch (Exception e) {System.err.println("Cleanup error for user (pro) " + testProUid + ": " + e.getMessage());}
        }
    }

    private ServiceRequest createAndSaveServiceRequest(String clientId, String category, String description, ServiceRequestStatus status) throws Exception {
        ServiceRequest sr = new ServiceRequest();
        sr.setClientId(clientId);
        sr.setCategory(category);
        sr.setServiceDescription(description);
        sr.setStatus(status);
        sr.setCreatedAt(LocalDateTime.now().minusDays(1));
        ServiceRequest savedSr = serviceRequestRepository.save(sr);
        serviceRequestIdsToCleanup.add(savedSr.getId());
        return savedSr;
    }

    @Test
    void testSubmitServiceRequest_success() throws ExecutionException, InterruptedException {
        ServiceRequestInputDTO inputDTO = new ServiceRequestInputDTO();
        inputDTO.setCategory("Wellness");
        inputDTO.setServiceDescription("Need a wellness coach");
        inputDTO.setBudget("$100-200");

        ServiceRequest createdRequest = given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testClientUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .contentType("application/json")
            .body(inputDTO)
        .when()
            .post("/api/service-requests")
        .then()
            .statusCode(201)
            .body("clientId", equalTo(testClientUid))
            .body("category", equalTo("Wellness"))
            .body("status", equalTo(ServiceRequestStatus.OPEN.name()))
            .extract().as(ServiceRequest.class);

        serviceRequestIdsToCleanup.add(createdRequest.getId()); // Ensure cleanup

        Optional<ServiceRequest> srOptional = serviceRequestRepository.findById(createdRequest.getId());
        assertTrue(srOptional.isPresent(), "Service request should be saved in DB");
        assertEquals(testClientUid, srOptional.get().getClientId());
    }

    @Test
    void testSubmitServiceRequest_unauthorized() {
        ServiceRequestInputDTO inputDTO = new ServiceRequestInputDTO();
        inputDTO.setCategory("Fitness");
        inputDTO.setServiceDescription("Looking for a personal trainer");

        given() // No auth headers
            .contentType("application/json")
            .body(inputDTO)
        .when()
            .post("/api/service-requests")
        .then()
            .statusCode(401);
    }

    @Test
    void testGetMyServiceRequests_client_success() throws Exception {
        // Create a couple of service requests for testClientUid
        createAndSaveServiceRequest(testClientUid, "Yoga", "Need yoga instructor", ServiceRequestStatus.OPEN);
        createAndSaveServiceRequest(testClientUid, "Personal Training", "Weight loss program", ServiceRequestStatus.MATCHED);

        // Create a service request for another client (should not be returned)
        String otherClientUid = "other-client-for-sr-test-" + UUID.randomUUID().toString();
        User otherClient = new Client(); otherClient.setId(otherClientUid); otherClient.setEmail(otherClientUid + "@example.com"); userRepository.save(otherClient);
        serviceRequestIdsToCleanup.add(createAndSaveServiceRequest(otherClientUid, "Nutrition", "Diet plan", ServiceRequestStatus.OPEN).getId());


        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testClientUid)
            .header(TestAuthMechanism.TEST_EMAIL_HEADER, testClientEmail) // Added email for consistency
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
        .when()
            .get("/api/service-requests/client/me")
        .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].clientId", equalTo(testClientUid))
            .body("[1].clientId", equalTo(testClientUid));

        userRepository.delete(otherClientUid); // clean up other client
    }

    @Test
    void testGetOpenServiceRequests_professional_success() throws Exception {
        // SR by the test client - should be visible to professional
        createAndSaveServiceRequest(testClientUid, "Coaching", "Life coach needed", ServiceRequestStatus.OPEN);

        // SR by another client - should also be visible
        String anotherClientUid = "another-client-" + UUID.randomUUID().toString();
        User anotherClient = new Client();
        anotherClient.setId(anotherClientUid);
        anotherClient.setEmail(anotherClientUid + "@example.com");
        userRepository.save(anotherClient);
        serviceRequestIdsToCleanup.add(createAndSaveServiceRequest(anotherClientUid, "Wellness", "Wellness program", ServiceRequestStatus.OPEN).getId());

        // SR by the professional themselves (acting as a client) - should NOT be visible due to service logic
        serviceRequestIdsToCleanup.add(createAndSaveServiceRequest(testProUid, "Consulting", "Pro needs consulting", ServiceRequestStatus.OPEN).getId());

        // SR that is not OPEN - should not be visible
        serviceRequestIdsToCleanup.add(createAndSaveServiceRequest(testClientUid, "Therapy", "Therapist needed", ServiceRequestStatus.MATCHED).getId());

        List<ServiceRequest> response = given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testProUid)
            .header(TestAuthMechanism.TEST_EMAIL_HEADER, testProEmail) // Added email for consistency
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.PROFESSIONAL.name())
        .when()
            .get("/api/service-requests/professional/open")
        .then()
            .statusCode(200)
            .extract().body().jsonPath().getList(".", ServiceRequest.class);

        assertEquals(2, response.size());
        assertTrue(response.stream().allMatch(sr -> sr.getStatus() == ServiceRequestStatus.OPEN));
        assertTrue(response.stream().anyMatch(sr -> sr.getClientId().equals(testClientUid) && sr.getCategory().equals("Coaching")));
        assertTrue(response.stream().anyMatch(sr -> sr.getClientId().equals(anotherClientUid) && sr.getCategory().equals("Wellness")));

        userRepository.delete(anotherClientUid);
    }

    @Test
    void testSelectProfessionalForServiceRequest_success() throws Exception {
        ServiceRequest sr = createAndSaveServiceRequest(testClientUid, "Coaching", "Career coaching", ServiceRequestStatus.OPEN);

        ClientSelectProfessionalRequestDTO selectionDTO = new ClientSelectProfessionalRequestDTO();
        selectionDTO.setProfessionalId(testProUid);

        AppointmentDTO appointmentDto = given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testClientUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .contentType("application/json")
            .body(selectionDTO)
            .pathParam("serviceRequestId", sr.getId())
        .when()
            .post("/api/service-requests/{serviceRequestId}/select-professional")
        .then()
            .statusCode(200)
            .body("clientId", equalTo(testClientUid))
            .body("professionalId", equalTo(testProUid))
            .body("serviceRequestId", equalTo(sr.getId()))
            .body("status", equalTo(AppointmentStatus.REQUESTED.name()))
            .extract().as(AppointmentDTO.class);

        appointmentIdsToCleanup.add(appointmentDto.getId());

        ServiceRequest updatedSr = serviceRequestRepository.findById(sr.getId()).orElseThrow();
        assertEquals(ServiceRequestStatus.PENDING_CONTACT, updatedSr.getStatus());
        assertNotNull(updatedSr.getUpdatedAt());
    }

    @Test
    void testSelectProfessionalForServiceRequest_forbidden_notOwner() throws Exception {
        ServiceRequest sr = createAndSaveServiceRequest(testClientUid, "Other Service", "Details", ServiceRequestStatus.OPEN);

        String maliciousClientUid = "malicious-uid-" + UUID.randomUUID().toString();
        // TestAuthMechanism will create this identity on the fly, no DB entry needed for this specific test of authz

        ClientSelectProfessionalRequestDTO selectionDTO = new ClientSelectProfessionalRequestDTO();
        selectionDTO.setProfessionalId(testProUid);

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, maliciousClientUid) // Different client
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .contentType("application/json")
            .body(selectionDTO)
            .pathParam("serviceRequestId", sr.getId())
        .when()
            .post("/api/service-requests/{serviceRequestId}/select-professional")
        .then()
            .statusCode(403); // Forbidden
    }

    @Test
    void testSelectProfessionalForServiceRequest_alreadyMatched() throws Exception {
        ServiceRequest sr = createAndSaveServiceRequest(testClientUid, "Coaching", "Life coaching", ServiceRequestStatus.OPEN);

        // First, select a professional successfully
        ClientSelectProfessionalRequestDTO selectionDTO1 = new ClientSelectProfessionalRequestDTO();
        selectionDTO1.setProfessionalId(testProUid);
        AppointmentDTO firstAppointment = given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testClientUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .contentType("application/json")
            .body(selectionDTO1)
            .pathParam("serviceRequestId", sr.getId())
        .when()
            .post("/api/service-requests/{serviceRequestId}/select-professional")
        .then().extract().as(AppointmentDTO.class);
        appointmentIdsToCleanup.add(firstAppointment.getId());


        // Attempt to select another professional (or the same one again)
        String anotherProUid = "another-pro-" + UUID.randomUUID().toString(); // Could be testProUid again too
        Professional anotherPro = new Professional(); anotherPro.setId(anotherProUid); /* setup pro */
        // For this test, the professional doesn't strictly need to exist in DB if the check for existing appointment comes first

        ClientSelectProfessionalRequestDTO selectionDTO2 = new ClientSelectProfessionalRequestDTO();
        selectionDTO2.setProfessionalId(anotherProUid);

        given()
            .header(TestAuthMechanism.TEST_UID_HEADER, testClientUid)
            .header(TestAuthMechanism.TEST_ROLES_HEADER, UserRole.CLIENT.name())
            .contentType("application/json")
            .body(selectionDTO2)
            .pathParam("serviceRequestId", sr.getId())
        .when()
            .post("/api/service-requests/{serviceRequestId}/select-professional")
        .then()
            .statusCode(409); // Conflict - as an appointment already exists
    }
}
