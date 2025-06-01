package com.fitconnect.entity;

public enum AppointmentStatus {
    REQUESTED,      // Client has selected this professional from matches
    ACCEPTED_BY_PROFESSIONAL, // Professional has acknowledged and is waiting for client contact
    CONFIRMED,      // Both parties confirmed (maybe not needed if contact is offline)
    COMPLETED,
    CANCELLED_BY_CLIENT,
    CANCELLED_BY_PROFESSIONAL
}
