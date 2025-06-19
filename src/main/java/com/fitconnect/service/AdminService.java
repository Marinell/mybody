package com.fitconnect.service;

import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument; // Added
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.repository.ProfessionalDocumentRepository; // Added
import com.fitconnect.repository.ProfessionalRepository; // Added
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject; // Added
// import jakarta.transaction.Transactional; // Removed
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List; // Added
import java.util.Optional; // Added
import java.util.concurrent.ExecutionException; // Added
import java.util.stream.Collectors; // Added

@ApplicationScoped
public class AdminService {

    private static final Logger LOG = Logger.getLogger(AdminService.class);

    @Inject
    ProfessionalRepository professionalRepository; // Added

    @Inject
    ProfessionalDocumentRepository professionalDocumentRepository; // Added

    public Professional verifyProfessionalProfile(String professionalId, ProfileStatus newStatus) throws ExecutionException, InterruptedException { // Removed @Transactional, Long to String, added throws
        if (newStatus != ProfileStatus.VERIFIED && newStatus != ProfileStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid status for verification. Must be VERIFIED or REJECTED.");
        }

        Optional<Professional> professionalOptional = professionalRepository.findById(professionalId);
        if (professionalOptional.isEmpty()) {
            LOG.warnf("Professional with ID %s not found for verification.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }
        Professional professional = professionalOptional.get();

        professional.setProfileStatus(newStatus); // Changed to setter
        professional.setUpdatedAt(java.time.LocalDateTime.now()); // Added
        professionalRepository.save(professional); // Changed from persist
        LOG.infof("Professional ID %s status updated to %s by admin.", professionalId, newStatus);
        return professional;
    }

    public List<Professional> getAllProfessionalsPendingVerification() throws ExecutionException, InterruptedException { // Added method
        List<Professional> allProfessionals = professionalRepository.findAll();
        return allProfessionals.stream()
                .filter(p -> p.getProfileStatus() == ProfileStatus.PENDING_VERIFICATION)
                .collect(Collectors.toList());
    }

    public List<ProfessionalDocument> getProfessionalDocuments(String professionalId) throws ExecutionException, InterruptedException { // Added method
        // First check if professional exists, optional
        Optional<Professional> professionalOptional = professionalRepository.findById(professionalId);
        if (professionalOptional.isEmpty()) {
            LOG.warnf("Professional with ID %s not found when trying to fetch documents.", professionalId);
            throw new NotFoundException("Professional not found with ID: " + professionalId);
        }
        return professionalDocumentRepository.findByProfessionalId(professionalId);
    }
}
