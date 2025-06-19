package com.fitconnect.repository;

import com.fitconnect.entity.Skill;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@ApplicationScoped
public class SkillRepository {

    private static final String COLLECTION_NAME = "skills";
    private Firestore db;

    public SkillRepository() {
        this.db = FirestoreClient.getFirestore();
    }

    private CollectionReference getSkillsCollection() {
        return db.collection(COLLECTION_NAME);
    }

    public Skill save(Skill skill) throws ExecutionException, InterruptedException {
        if (skill.getId() == null || skill.getId().isEmpty()) {
            DocumentReference docRef = getSkillsCollection().document();
            skill.setId(docRef.getId());
            docRef.set(skill).get();
        } else {
            getSkillsCollection().document(skill.getId()).set(skill).get();
        }
        return skill;
    }

    public Optional<Skill> findById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = getSkillsCollection().document(id).get().get();
        if (documentSnapshot.exists()) {
            return Optional.ofNullable(documentSnapshot.toObject(Skill.class));
        }
        return Optional.empty();
    }

    public Optional<Skill> findByName(String name) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getSkillsCollection().whereEqualTo("name", name).limit(1).get();
        List<Skill> skills = future.get().toObjects(Skill.class);
        if (!skills.isEmpty()) {
            return Optional.of(skills.get(0));
        }
        return Optional.empty();
    }

    public List<Skill> findAll() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = getSkillsCollection().get();
        return future.get().toObjects(Skill.class);
    }
}
