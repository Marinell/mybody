package com.fitconnect.service;

import com.fitconnect.entity.Professional;
import com.fitconnect.entity.ProfessionalDocument;
import com.fitconnect.entity.Skill;
import com.fitconnect.llm.ProfessionalProfileAnalyzer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.tika.Tika;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfessionalScreeningService {

    private static final Logger LOG = Logger.getLogger(ProfessionalScreeningService.class);

    @Inject
    ProfessionalProfileAnalyzer profileAnalyzer;

    @Inject
    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key")
    String openaiApiKey;


    @Transactional
    public void screenProfessionalProfile(Long professionalId) {
        LOG.infof("Starting screening process for professional ID: %d", professionalId);

        if (openaiApiKey == null || openaiApiKey.isEmpty() || "YOUR_OPENAI_API_KEY".equals(openaiApiKey.trim())) {
            LOG.warn("OpenAI API key is not configured or is using the placeholder value. Skipping LLM screening.");
            Professional pro = Professional.findById(professionalId);
            if (pro != null) {
                pro.summarizedSkills = "LLM screening skipped: API key not configured.";
                pro.persist();
            }
            return;
        }

        Professional professional = Professional.findById(professionalId);
        if (professional == null) {
            LOG.errorf("Professional with ID %d not found.", professionalId);
            return;
        }

        StringBuilder documentTexts = new StringBuilder();
        Tika tika = new Tika();
        if (professional.documents != null) {
            for (ProfessionalDocument doc : professional.documents) {
                try {
                    Path docPath = Paths.get(doc.storagePath);
                    if (Files.exists(docPath)) {
                        try (InputStream stream = Files.newInputStream(docPath)) {
                            String text = tika.parseToString(stream);
                            documentTexts.append(text).append(" --- ");
                            LOG.infof("Successfully extracted text from document: %s", doc.fileName);
                        }
                    } else {
                        LOG.warnf("Document not found at path: %s for professional ID: %d", doc.storagePath, professionalId);
                    }
                } catch (Exception e) {
                    LOG.errorf(e, "Failed to read or parse document %s for professional ID: %d", doc.fileName, professionalId);
                    documentTexts.append("Error extracting text from document: ").append(doc.fileName).append(" --- ");
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
        professional.summarizedSkills = summary;
        LOG.infof("LLM Summary for professional ID %d: %s", professionalId, summary);

        String skillsListString = profileAnalyzer.extractSkillsList(profileData, documentTexts.toString());
        LOG.infof("LLM Extracted Skills List for professional ID %d: %s", professionalId, skillsListString);

        if (skillsListString != null && !skillsListString.isEmpty()) {
            List<String> skillNames = Arrays.stream(skillsListString.split(","))
                                           .map(String::trim)
                                           .filter(s -> !s.isEmpty())
                                           .distinct()
                                           .collect(Collectors.toList());

            List<Skill> skillsToAssociate = new ArrayList<>();
            for (String skillName : skillNames) {
                Optional<Skill> existingSkillOpt = Skill.find("lower(name)", skillName.toLowerCase()).firstResultOptional();
                Skill skillToPersist;
                if (existingSkillOpt.isPresent()) {
                    skillToPersist = existingSkillOpt.get();
                } else {
                    skillToPersist = new Skill();
                    skillToPersist.name = skillName;
                    skillToPersist.persist();
                }
                skillsToAssociate.add(skillToPersist);
            }
            professional.skills = skillsToAssociate;
        } else {
            professional.skills = new ArrayList<>();
        }

        professional.persist();
        LOG.infof("Successfully screened and updated profile for professional ID: %d", professionalId);
    }
}
