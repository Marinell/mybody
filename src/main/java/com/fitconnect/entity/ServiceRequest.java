package com.fitconnect.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceRequest extends FirestoreEntity {

    public String clientId;

    public String category;
    public String serviceDescription;
    public String budget; // Could be a range or specific amount, store as String for flexibility

    public ServiceRequestStatus status;

    public LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // @PrePersist and @PreUpdate logic removed, will be handled by service layer
    // protected void onCreate() {
    //     createdAt = LocalDateTime.now();
    //     status = ServiceRequestStatus.OPEN;
    // }

    // protected void onUpdate() {
    //     updatedAt = LocalDateTime.now();
    // }
}
