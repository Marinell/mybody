package com.fitconnect.entity;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class FirestoreEntity {
    @DocumentId
    private String id;
}
