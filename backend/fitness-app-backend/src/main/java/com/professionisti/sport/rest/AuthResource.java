package com.professionisti.sport.rest;

import com.professionisti.sport.dto.auth.ClientRegistrationRequest;
import com.professionisti.sport.dto.auth.LoginRequest; // Added
import com.professionisti.sport.dto.auth.LoginResponse; // Added
import com.professionisti.sport.dto.auth.ProfessionalRegistrationDetails;
import com.professionisti.sport.model.Client;
import com.professionisti.sport.model.Professional;
import com.professionisti.sport.service.UserService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.Collections;
import java.util.List;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    UserService userService;

    public static class ProfessionalMultipartForm {
        @FormParam("firstName") public String firstName;
        @FormParam("lastName") public String lastName;
        @FormParam("email") public String email;
        @FormParam("password") public String password;
        @FormParam("phoneNumber") public String phoneNumber;
        @FormParam("specialization") public String specialization;
        @FormParam("experienceYears") public Integer experienceYears;
        @FormParam("blogUrl") public String blogUrl;
        @FormParam("socialMediaLink") public String socialMediaLink;

        @FormParam("documents")
        public List<FileUpload> documentUploads;

        public ProfessionalRegistrationDetails toDetailsDTO() {
            ProfessionalRegistrationDetails dto = new ProfessionalRegistrationDetails();
            dto.setFirstName(firstName);
            dto.setLastName(lastName);
            dto.setEmail(email);
            dto.setPassword(password);
            dto.setPhoneNumber(phoneNumber);
            dto.setSpecialization(specialization);
            dto.setExperienceYears(experienceYears);
            dto.setBlogUrl(blogUrl);
            dto.setSocialMediaLink(socialMediaLink);
            return dto;
        }
    }

    @POST
    @Path("/register/client")
    public Response registerClient(@Valid ClientRegistrationRequest request) {
        try {
            Client client = userService.registerClient(request);
            return Response.status(Response.Status.CREATED)
                           .entity("Client registered successfully with ID: " + client.getId())
                           .build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred: " + e.getMessage())
                           .build();
        }
    }

    @POST
    @Path("/register/professional")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registerProfessional(@MultipartForm ProfessionalMultipartForm form) {
        if (form == null) { // Basic check for form itself
             return Response.status(Response.Status.BAD_REQUEST).entity("Form data is missing.").build();
        }
        // It's good practice to validate individual string fields from the form if they are mandatory
        if (form.email == null || form.email.trim().isEmpty() ||
            form.password == null || form.password.trim().isEmpty() ||
            form.firstName == null || form.firstName.trim().isEmpty() ||
            form.lastName == null || form.lastName.trim().isEmpty() ||
            form.phoneNumber == null || form.phoneNumber.trim().isEmpty() ||
            form.specialization == null || form.specialization.trim().isEmpty() ||
            form.experienceYears == null ) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Required professional details are missing.").build();
        }

        ProfessionalRegistrationDetails details = form.toDetailsDTO();
        // Consider adding @Valid for 'details' if using a JSON part for it,
        // or manual validation via Validator service for individual form fields.

        try {
            Professional professional = userService.registerProfessional(details, form.documentUploads != null ? form.documentUploads : Collections.emptyList());
            return Response.status(Response.Status.CREATED)
                           .entity("Professional registered successfully with ID: " + professional.getId() + ". Profile pending review.")
                           .build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred: " + e.getMessage())
                           .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        try {
            LoginResponse loginResponse = userService.login(request);
            return Response.ok(loginResponse).build();
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("An unexpected error occurred during login: " + e.getMessage())
                           .build();
        }
    }
}
