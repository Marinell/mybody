package com.fitconnect.entity;

public enum ServiceRequestStatus {
    OPEN,       // Newly created request by client
    MATCHED,    // LLM has found potential matches, client needs to pick
    PENDING_CONTACT, // Client has selected a professional, waiting for professional to acknowledge
    ACCEPTED,   // Professional has accepted the request (becomes an appointment)
    REJECTED_BY_PROFESSIONAL, // Professional declined
    COMPLETED,  // Service delivered
    CANCELLED   // Cancelled by client or system
}
