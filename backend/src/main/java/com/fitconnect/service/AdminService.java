package com.fitconnect.service;

import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfileStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AdminService {

    private static final Logger LOG = Logger.getLogger(AdminService.class);

    @Transactional
    public Professional verifyProfessionalProfile(Long professionalId, ProfileStatus newStatus) {
        if (newStatus != ProfileStatus.VERIFIED && newStatus != ProfileStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid status for verification. Must be VERIFIED or REJECTED.");
        }

        Professional professional = Professional.findById(professionalId);
        if (professional == null) {
            LOG.warnf("Professional with ID %d not found for verification.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }

        professional.profileStatus = newStatus;
        professional.persist();
        LOG.infof("Professional ID %d status updated to %s by admin.", professionalId, newStatus);
        return professional;
    }
}
