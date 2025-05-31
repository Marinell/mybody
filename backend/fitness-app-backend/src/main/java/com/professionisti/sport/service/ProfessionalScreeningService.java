package com.professionisti.sport.service;

import com.professionisti.sport.model.Document;
import com.professionisti.sport.model.Professional;
import com.professionisti.sport.model.enums.ScreeningStatus;
// import com.professionisti.sport.llm.DocumentSummarizer; // Placeholder for LLM service

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

// import org.apache.tika.Tika; // Would be used for document text extraction
// import org.apache.tika.exception.TikaException;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfessionalScreeningService {

    @Inject
    EntityManager entityManager;

    // @Inject
    // DocumentSummarizer documentSummarizer; // Placeholder for LLM

    // private final Tika tika = new Tika(); // For text extraction

    @Transactional
    public Professional screenProfessionalProfile(Long professionalId) {
        Professional professional = entityManager.find(Professional.class, professionalId);
        if (professional == null) {
            throw new WebApplicationException("Professional not found with ID: " + professionalId, Response.Status.NOT_FOUND);
        }

        StringBuilder extractedTexts = new StringBuilder();
        List<Document> documents = professional.getDocuments();

        if (documents == null || documents.isEmpty()) { // Added null check for documents
            professional.setSkillsSummary("No documents provided for screening.");
            // Potentially set status to REJECTED or PENDING_MORE_INFO if docs are mandatory
            entityManager.merge(professional);
            return professional;
        }

        for (Document doc : documents) {
            try {
                // Placeholder for Tika text extraction
                // String text = tika.parseToString(new ByteArrayInputStream(doc.getData()));
                if (doc.getData() != null) { // Check if doc data is null
                    String text = new String(doc.getData()); // Highly simplified: assumes document data is plain text
                    extractedTexts.append(text).append("\n\n---\n\n");
                } else {
                    extractedTexts.append("Document: ").append(doc.getFileName()).append(" has no content.\n\n---\n\n");
                }
            } catch (Exception e) { // Replace with specific exceptions like TikaException, IOException
                System.err.println("Error extracting text from document " + doc.getFileName() + ": " + e.getMessage());
                extractedTexts.append("Error processing document: ").append(doc.getFileName()).append("\n\n---\n\n");
            }
        }

        String combinedText = extractedTexts.toString();
        String summary = "Placeholder Summary: LLM integration and document parsing are not functional due to POM issues. ";

        if (combinedText.trim().isEmpty() && (professional.getBlogUrl() == null || professional.getBlogUrl().trim().isEmpty()) && (professional.getSocialMediaLink() == null || professional.getSocialMediaLink().trim().isEmpty())) {
            summary += "No text content could be extracted from documents or links.";
        } else {
            summary += "Extracted text (first 500 chars if any): " + (combinedText.length() > 500 ? combinedText.substring(0, 500) + "..." : combinedText);
        }

        professional.setSkillsSummary(summary);
        entityManager.merge(professional);
        return professional;
    }

    @Transactional
    public Professional updateScreeningStatus(Long professionalId, ScreeningStatus newStatus, String adminNotes) {
        Professional professional = entityManager.find(Professional.class, professionalId);
        if (professional == null) {
            throw new WebApplicationException("Professional not found with ID: " + professionalId, Response.Status.NOT_FOUND);
        }
        professional.setScreeningStatus(newStatus);
        // Optionally, store adminNotes (e.g., in a new field or a separate audit log entity)
        // For now, adminNotes are not persisted on the Professional entity itself.
        entityManager.merge(professional);
        return professional;
    }
}
