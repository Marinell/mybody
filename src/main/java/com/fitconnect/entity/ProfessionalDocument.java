package com.fitconnect.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalDocument extends FirestoreEntity {

    private String professionalId; // Changed from Professional professional

    private String fileName;
    private String fileType; // e.g., application/pdf, image/jpeg
    private String storagePath; // Path to the file in Firebase Storage

    // fileContent (byte[]) removed, will be stored in Firebase Storage
}
