package com.fitconnect.repository;

import com.fitconnect.entity.User;
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
public class UserRepository {

    private static final String COLLECTION_NAME = "users";
    private Firestore db;

    public UserRepository() {
        this.db = FirestoreClient.getFirestore();
    }

    private CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_NAME);
    }

    public User save(User user) throws ExecutionException, InterruptedException {
        if (user.getId() == null || user.getId().isEmpty()) {
            // Let Firestore auto-generate ID
            DocumentReference docRef = getUsersCollection().document();
            user.setId(docRef.getId());
            docRef.set(user).get(); // Ensure write is complete
            return user;
        } else {
            getUsersCollection().document(user.getId()).set(user).get();
            return user;
        }
    }

    public Optional<User> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = getUsersCollection().document(id).get().get();
        if (documentSnapshot.exists()) {
            return Optional.ofNullable(documentSnapshot.toObject(User.class));
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getUsersCollection().whereEqualTo("email", email).limit(1).get();
        List<User> users = future.get().toObjects(User.class);
        if (!users.isEmpty()) {
            return Optional.of(users.get(0));
        }
        return Optional.empty();
    }

    public void delete(String id) throws ExecutionException, InterruptedException {
        getUsersCollection().document(id).delete().get();
    }

    // Add update and delete methods if needed, for now save can handle updates too if ID exists
}
