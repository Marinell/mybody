package com.fitconnect.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Appointment extends FirestoreEntity {

    public String professionalId;

    public String clientId;

    public String serviceRequestId; // Link to the original request

    public LocalDateTime appointmentDateTime; // Proposed or confirmed date/time
    public String communicationDetails; // e.g., "Client will contact via email provided"

    public AppointmentStatus status;

    public LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // @PrePersist logic removed, will be handled by service layer
    // protected void onCreate() {
    //     createdAt = LocalDateTime.now();
    //     status = AppointmentStatus.REQUESTED; // Initial status when client picks a professional
    // }
}
