package com.fitconnect.resource;

import com.fitconnect.dto.ProfileVerificationRequest;
import com.fitconnect.entity.Professional;
import com.fitconnect.service.AdminService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    private static final Logger LOG = Logger.getLogger(AdminResource.class);

    @Inject
    AdminService adminService;

    @POST
    @Path("/professionals/{id}/verify")
    @RolesAllowed("ADMIN")
    public Response verifyProfessional(@PathParam("id") Long professionalId, ProfileVerificationRequest request) {
        if (request == null || request.getNewStatus() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Verification request body or new status is missing.")
                           .build();
        }
        try {
            Professional updatedProfessional = adminService.verifyProfessionalProfile(professionalId, request.getNewStatus());
            return Response.ok(updatedProfessional).build();
        } catch (NotFoundException e) {
            LOG.warnf("Admin verification failed for professional ID %d: %s", professionalId, e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            LOG.warnf("Admin verification failed for professional ID %d due to bad argument: %s", professionalId, e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during professional verification for ID %d by admin", professionalId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred.")
                           .build();
        }
    }
}
