package com.fitconnect.repository;

import com.fitconnect.entity.ProfessionalDocument;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProfessionalDocumentRepository {

    private static final String COLLECTION_NAME = "professionalDocuments";
    private final Firestore db;

    public ProfessionalDocumentRepository() {
        this.db = FirestoreClient.getFirestore();
    }

    private CollectionReference getDocumentsCollection() {
        return db.collection(COLLECTION_NAME);
    }

    public ProfessionalDocument save(ProfessionalDocument document) throws ExecutionException, InterruptedException {
        if (document.getId() == null || document.getId().isEmpty()) {
            DocumentReference docRef = getDocumentsCollection().document();
            document.setId(docRef.getId());
            docRef.set(document).get();
        } else {
            getDocumentsCollection().document(document.getId()).set(document).get();
        }
        return document;
    }

    public Optional<ProfessionalDocument> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = getDocumentsCollection().document(id).get().get();
        if (documentSnapshot.exists()) {
            return Optional.ofNullable(documentSnapshot.toObject(ProfessionalDocument.class));
        }
        return Optional.empty();
    }

    public List<ProfessionalDocument> findByProfessionalId(String professionalId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getDocumentsCollection().whereEqualTo("professionalId", professionalId).get();
        return future.get().toObjects(ProfessionalDocument.class);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        getDocumentsCollection().document(id).delete().get();
    }
}
