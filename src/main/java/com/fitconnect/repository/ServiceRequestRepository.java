package com.fitconnect.repository;

import com.fitconnect.entity.ServiceRequest;
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

@ApplicationScoped
public class ServiceRequestRepository {

    private static final String COLLECTION_NAME = "serviceRequests";
    private final Firestore db;

    public ServiceRequestRepository() {
        this.db = FirestoreClient.getFirestore();
    }

    private CollectionReference getServiceRequestsCollection() {
        return db.collection(COLLECTION_NAME);
    }

    public ServiceRequest save(ServiceRequest serviceRequest) throws ExecutionException, InterruptedException {
        if (serviceRequest.getId() == null || serviceRequest.getId().isEmpty()) {
            DocumentReference docRef = getServiceRequestsCollection().document();
            serviceRequest.setId(docRef.getId());
            // Set createdAt and initial status here if not already set by service
            if (serviceRequest.getCreatedAt() == null) {
                serviceRequest.setCreatedAt(java.time.LocalDateTime.now());
            }
             if (serviceRequest.getStatus() == null) {
                serviceRequest.setStatus(com.fitconnect.entity.ServiceRequestStatus.OPEN);
            }
            docRef.set(serviceRequest).get();
        } else {
            // For updates, ensure updatedAt is handled
             if (serviceRequest.getUpdatedAt() == null) { // Basic check, could be more sophisticated
                serviceRequest.setUpdatedAt(java.time.LocalDateTime.now());
            }
            getServiceRequestsCollection().document(serviceRequest.getId()).set(serviceRequest).get();
        }
        return serviceRequest;
    }

    public Optional<ServiceRequest> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = getServiceRequestsCollection().document(id).get().get();
        if (documentSnapshot.exists()) {
            return Optional.ofNullable(documentSnapshot.toObject(ServiceRequest.class));
        }
        return Optional.empty();
    }

    public List<ServiceRequest> findByClientId(String clientId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getServiceRequestsCollection().whereEqualTo("clientId", clientId).get();
        return future.get().toObjects(ServiceRequest.class);
    }

    public List<ServiceRequest> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getServiceRequestsCollection().get();
        return future.get().toObjects(ServiceRequest.class);
    }
     public List<ServiceRequest> findOpenByNotClientId(String clientId) throws ExecutionException, InterruptedException {
        // Find OPEN requests not made by the given client (typically a professional)
        ApiFuture<QuerySnapshot> future = getServiceRequestsCollection()
                                                .whereEqualTo("status", com.fitconnect.entity.ServiceRequestStatus.OPEN.name()) // Assuming status is stored as string
                                                .whereNotEqualTo("clientId", clientId)
                                                .get();
        return future.get().toObjects(ServiceRequest.class);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        getServiceRequestsCollection().document(id).delete().get();
    }
}
