package com.fitconnect.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class User extends FirestoreEntity {

    public String email;

    public String password; // Hashing will be handled by the application logic

    public UserRole role;

    public String name; // Common for both, though professional might have a more formal 'fullName'
    public String phoneNumber;
    private java.time.LocalDateTime updatedAt;
}
