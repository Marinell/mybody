package com.fitconnect.repository;

import com.fitconnect.entity.Appointment;
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
public class AppointmentRepository {

    private static final String COLLECTION_NAME = "appointments";
    private final Firestore db;

    public AppointmentRepository() {
        this.db = FirestoreClient.getFirestore();
    }

    private CollectionReference getAppointmentsCollection() {
        return db.collection(COLLECTION_NAME);
    }

    public Appointment save(Appointment appointment) throws ExecutionException, InterruptedException {
        if (appointment.getId() == null || appointment.getId().isEmpty()) {
            DocumentReference docRef = getAppointmentsCollection().document();
            appointment.setId(docRef.getId());
            // Set createdAt and initial status here if not already set by service
            if (appointment.getCreatedAt() == null) {
                appointment.setCreatedAt(java.time.LocalDateTime.now());
            }
            if (appointment.getStatus() == null) {
                appointment.setStatus(com.fitconnect.entity.AppointmentStatus.REQUESTED);
            }
            docRef.set(appointment).get();
        } else {
            // For updates, ensure updatedAt is handled if necessary
            getAppointmentsCollection().document(appointment.getId()).set(appointment).get();
        }
        return appointment;
    }

    public Optional<Appointment> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = getAppointmentsCollection().document(id).get().get();
        if (documentSnapshot.exists()) {
            return Optional.ofNullable(documentSnapshot.toObject(Appointment.class));
        }
        return Optional.empty();
    }

    public List<Appointment> findByProfessionalId(String professionalId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getAppointmentsCollection().whereEqualTo("professionalId", professionalId).get();
        return future.get().toObjects(Appointment.class);
    }

    public List<Appointment> findByClientId(String clientId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getAppointmentsCollection().whereEqualTo("clientId", clientId).get();
        return future.get().toObjects(Appointment.class);
    }

    public Optional<Appointment> findByServiceRequestId(String serviceRequestId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getAppointmentsCollection().whereEqualTo("serviceRequestId", serviceRequestId).limit(1).get();
        List<Appointment> appointments = future.get().toObjects(Appointment.class);
        if (!appointments.isEmpty()) {
            return Optional.of(appointments.get(0));
        }
        return Optional.empty();
    }

    public List<Appointment> findByProfessionalIdAndStatus(String professionalId, com.fitconnect.entity.AppointmentStatus status) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getAppointmentsCollection()
                .whereEqualTo("professionalId", professionalId)
                .whereEqualTo("status", status.name()) // Assuming status is stored as String
                .get();
        return future.get().toObjects(Appointment.class);
    }

    public List<Appointment> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getAppointmentsCollection().get();
        return future.get().toObjects(Appointment.class);
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        getAppointmentsCollection().document(id).delete().get();
    }
}
