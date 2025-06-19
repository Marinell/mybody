package com.fitconnect.repository;

import com.fitconnect.entity.Professional;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfessionalRepository {

    private static final String COLLECTION_NAME = "professionals";
    private Firestore db;

    public ProfessionalRepository() {
        this.db = FirestoreClient.getFirestore();
    }

    private CollectionReference getProfessionalsCollection() {
        return db.collection(COLLECTION_NAME);
    }

    public Professional save(Professional professional) throws ExecutionException, InterruptedException {
        if (professional.getId() == null || professional.getId().isEmpty()) {
            // User part should have been saved first by UserRepository if it's a new professional
            // or ID should be Firebase Auth UID.
            // This save is for the professional-specific data.
            // Assuming ID is already set (e.g., from Firebase Auth UID or after saving User part).
            if (professional.getId() == null || professional.getId().isEmpty()) {
                 throw new IllegalArgumentException("Professional ID must be set before saving to professionals collection. Save User part first or use Firebase Auth UID.");
            }
        }
        // Professionals are stored in a separate collection, identified by the same ID as their User entry.
        getProfessionalsCollection().document(professional.getId()).set(professional).get();
        return professional;
    }

    public Optional<Professional> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = getProfessionalsCollection().document(id).get().get();
        if (documentSnapshot.exists()) {
            return Optional.ofNullable(documentSnapshot.toObject(Professional.class));
        }
        return Optional.empty();
    }

    public List<Professional> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getProfessionalsCollection().get();
        return future.get().toObjects(Professional.class);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        getProfessionalsCollection().document(id).delete().get();
    }

    // Add other methods like findByEmail if professionals collection duplicates email
    // or methods to query by profession, skills, etc.
}
