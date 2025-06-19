package com.fitconnect.service;

import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument;
import com.fitconnect.entity.ProfileStatus;
import com.fitconnect.entity.Skill;
import com.fitconnect.llm.ProfessionalProfileAnalyzer;
import com.fitconnect.repository.ProfessionalDocumentRepository; // Added
import com.fitconnect.repository.ProfessionalRepository; // Added
import com.fitconnect.repository.SkillRepository; // Added

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
// import jakarta.transaction.Transactional; // Removed
import org.apache.tika.Tika;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

// import java.io.InputStream; // Tika InputStream not used directly with GCS path like this
// import java.nio.file.Files; // Removed
// import java.nio.file.Path; // Removed
// import java.nio.file.Paths; // Removed
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException; // Added
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfessionalScreeningService {

    private static final Logger LOG = Logger.getLogger(ProfessionalScreeningService.class);

    @Inject
    ProfessionalProfileAnalyzer profileAnalyzer;

    @Inject
    ProfessionalRepository professionalRepository; // Added

    @Inject
    SkillRepository skillRepository; // Added

    @Inject
    ProfessionalDocumentRepository professionalDocumentRepository; // Added

    @Inject
    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String openaiApiKey;

    public void screenProfessionalProfile(String professionalId) throws ExecutionException, InterruptedException { // Changed Long to String, removed @Transactional, added throws
        LOG.infof("Starting screening process for professional ID: %s", professionalId);

        Optional<Professional> proOptional = professionalRepository.findById(professionalId);
        if (proOptional.isEmpty()) {
            LOG.errorf("Professional with ID %s not found.", professionalId);
            // Optionally throw an exception or handle as appropriate
            return;
        }
        Professional professional = proOptional.get();

        if (openaiApiKey == null || openaiApiKey.isEmpty() || "YOUR_OPENAI_API_KEY".equals(openaiApiKey.trim())) {
            LOG.warn("OpenAI API key is not configured or is using the placeholder value. Skipping LLM screening.");
            professional.setSummarizedSkills("LLM screening skipped: API key not configured.");
            professional.setUpdatedAt(java.time.LocalDateTime.now());
            professionalRepository.save(professional);
            return;
        }

        StringBuilder documentTexts = new StringBuilder();
        // Tika tika = new Tika(); // Tika might still be useful if fetching GCS file bytes
        if (professional.getProfessionalDocumentReferences() != null && !professional.getProfessionalDocumentReferences().isEmpty()) {
            for (String docId : professional.getProfessionalDocumentReferences()) {
                try {
                    Optional<ProfessionalDocument> docOptional = professionalDocumentRepository.findById(docId);
                    if (docOptional.isPresent()) {
                        ProfessionalDocument doc = docOptional.get();
                        // Actual file reading from GCS and text extraction is complex and out of scope here.
                        // For now, append a placeholder.
                        LOG.infof("Placeholder: Would extract text from document: %s at GCS path: %s", doc.getFileName(), doc.getStoragePath());
                        documentTexts.append(String.format("Document content for %s from %s not extracted in this version. --- ", doc.getFileName(), doc.getStoragePath()));
                    } else {
                        LOG.warnf("ProfessionalDocument metadata not found for ID: %s, linked to professional ID: %s", docId, professionalId);
                    }
                } catch (Exception e) {
                    LOG.errorf(e, "Failed to process document reference %s for professional ID: %s", docId, professionalId);
                    documentTexts.append(String.format("Error processing document reference %s. --- ", docId));
                }
            }
        }

        String profileData = String.format("Profession: %s Years of Experience: %s Qualifications: %s About: %s Social Media/Links: %s",
            professional.profession != null ? professional.profession : "N/A",
            professional.yearsOfExperience != null ? professional.yearsOfExperience.toString() : "N/A",
            professional.qualifications != null ? professional.qualifications : "N/A",
            professional.aboutYou != null ? professional.aboutYou : "N/A",
            professional.socialMediaLinks != null && !professional.socialMediaLinks.isEmpty() ? professional.socialMediaLinks.toString() : "Not provided"
        );

        LOG.info("Sending data to LLM for summarization and skill extraction...");
        String summary = profileAnalyzer.summarizeExpertiseAndSkills(profileData, documentTexts.toString());
        professional.setSummarizedSkills(summary); // Changed to setter
        LOG.infof("LLM Summary for professional ID %s: %s", professionalId, summary);

        String skillsListString = profileAnalyzer.extractSkillsList(profileData, documentTexts.toString());
        LOG.infof("LLM Extracted Skills List for professional ID %s: %s", professionalId, skillsListString);

        if (skillsListString != null && !skillsListString.isEmpty()) {
            List<String> skillNames = Arrays.stream(skillsListString.split(","))
                                           .map(String::trim)
                                           .filter(s -> !s.isEmpty())
                                           .distinct()
                                           .collect(Collectors.toList());

            // Ensure skills exist in the 'skills' collection
            for (String skillName : skillNames) {
                Optional<Skill> existingSkillOpt = skillRepository.findByName(skillName); // Assumes findByName is case-sensitive or exact match
                if (existingSkillOpt.isEmpty()) {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillName);
                    skillRepository.save(newSkill); // Save new skill
                }
            }
            professional.setSkillNames(skillNames); // Set the list of skill names
        } else {
            professional.setSkillNames(new ArrayList<>()); // Set to empty list
        }

        professional.setProfileStatus(ProfileStatus.VERIFIED);
        professional.setUpdatedAt(java.time.LocalDateTime.now());
        professionalRepository.save(professional); // Changed from persist
        LOG.infof("Successfully screened and updated profile for professional ID: %s", professionalId);
    }
}
