package com.professionisti.sport.rest;

import com.professionisti.sport.dto.servicerequest.ServiceRequestCreateDTO;
import com.professionisti.sport.dto.servicerequest.ServiceRequestViewDTO;
import com.professionisti.sport.service.ServiceRequestService;
import com.professionisti.sport.service.MatchingService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
// import jakarta.ws.rs.core.Context;
// import jakarta.ws.rs.core.SecurityContext;

@Path("/api/client/service-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
// @RolesAllowed("ROLE_CLIENT")
public class ServiceRequestResource {

    @Inject
    ServiceRequestService serviceRequestService;

    @Inject
    MatchingService matchingService;

    private String getClientEmailFromContext() {
        return "test.client@example.com"; // Placeholder
    }

    @POST
    public Response createServiceRequest(@Valid ServiceRequestCreateDTO createDTO) {
        String clientEmail = getClientEmailFromContext();
        try {
            ServiceRequestViewDTO viewDTO = serviceRequestService.createServiceRequest(createDTO, clientEmail);
            return Response.status(Response.Status.CREATED).entity(viewDTO).build();
        } catch (WebApplicationException e) { return e.getResponse(); }
        catch (Exception e) { e.printStackTrace(); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build(); }
    }

    @GET
    @Path("/{id}")
    public Response getServiceRequest(@PathParam("id") Long id) {
        String clientEmail = getClientEmailFromContext();
        try {
            ServiceRequestViewDTO viewDTO = serviceRequestService.findServiceRequestById(id, clientEmail);
            return Response.ok(viewDTO).build();
        } catch (WebApplicationException e) { return e.getResponse(); }
        catch (Exception e) { e.printStackTrace(); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build(); }
    }

    @POST
    @Path("/{id}/find-matches")
    public Response findMatchesForServiceRequest(@PathParam("id") Long serviceRequestId) {
        String clientEmail = getClientEmailFromContext();
        try {
            ServiceRequestViewDTO resultDTO = matchingService.findAndAssignMatches(serviceRequestId, clientEmail);
            return Response.ok(resultDTO).build();
        } catch (WebApplicationException e) { return e.getResponse(); }
        catch (Exception e) { e.printStackTrace(); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build(); }
    }

    @POST
    @Path("/{requestId}/choose-professional/{professionalId}")
    public Response chooseProfessional(@PathParam("requestId") Long requestId, @PathParam("professionalId") Long professionalId) {
        String clientEmail = getClientEmailFromContext();
        try {
            ServiceRequestViewDTO resultDTO = serviceRequestService.chooseProfessional(requestId, professionalId, clientEmail);
            return Response.ok(resultDTO).build();
        } catch (WebApplicationException e) { return e.getResponse(); }
        catch (Exception e) { e.printStackTrace(); return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).build(); }
    }
}
