package com.fitconnect.resource;

import com.fitconnect.dto.ProfileVerificationRequest;
import com.fitconnect.entity.Professional;
import com.fitconnect.service.AdminService;
import com.fitconnect.entity.ProfessionalDocument; // Added

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET; // Added
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List; // Added
import java.util.concurrent.ExecutionException; // Added

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // Default for POST, GET won't consume typically
public class AdminResource {

    private static final Logger LOG = Logger.getLogger(AdminResource.class);

    @Inject
    AdminService adminService;

    @POST
    @Path("/professionals/{id}/verify")
    @RolesAllowed("ADMIN")
    public Response verifyProfessional(@PathParam("id") String professionalId, ProfileVerificationRequest request) { // Changed Long to String
        if (request == null || request.getNewStatus() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Verification request body or new status is missing.")
                           .build();
        }
        try {
            Professional updatedProfessional = adminService.verifyProfessionalProfile(professionalId, request.getNewStatus());
            return Response.ok(updatedProfessional).build();
        } catch (NotFoundException e) {
            LOG.warnf("Admin verification failed for professional ID %s: %s", professionalId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            LOG.warnf("Admin verification failed for professional ID %s due to bad argument: %s", professionalId, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Execution/Interruption error during professional verification for ID %s by admin", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during professional verification for ID %s by admin", professionalId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred.")
                           .build();
        }
    }

    @GET
    @Path("/professionals/pending-verification")
    @RolesAllowed("ADMIN")
    @Produces(MediaType.APPLICATION_JSON) // Ensure GET methods specify Produces if class default is JSON
    public Response getAllProfessionalsPendingVerification() {
        try {
            List<Professional> professionals = adminService.getAllProfessionalsPendingVerification();
            return Response.ok(professionals).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching professionals pending verification");
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error fetching professionals pending verification");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred.")
                           .build();
        }
    }

    @GET
    @Path("/professionals/{id}/documents")
    @RolesAllowed("ADMIN")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfessionalDocuments(@PathParam("id") String professionalId) { // Changed Long to String
        try {
            List<ProfessionalDocument> documents = adminService.getProfessionalDocuments(professionalId);
            return Response.ok(documents).build();
        } catch (NotFoundException e) {
            LOG.warnf("Failed to get documents for professional ID %s: %s", professionalId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error fetching documents for professional ID %s", professionalId);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing request.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error fetching documents for professional ID %s", professionalId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred.")
                           .build();
        }
    }
}
