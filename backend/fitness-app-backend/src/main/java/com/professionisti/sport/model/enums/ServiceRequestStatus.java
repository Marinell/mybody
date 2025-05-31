package com.professionisti.sport.model.enums;

public enum ServiceRequestStatus {
    OPEN,       // Request submitted by client
    MATCHED,    // System has found potential matches
    IN_PROGRESS, // Client has chosen a professional, pending professional action
    COMPLETED,  // Service completed
    CANCELLED   // Request cancelled
}
