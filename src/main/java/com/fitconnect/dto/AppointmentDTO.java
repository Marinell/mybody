package com.fitconnect.dto;

import com.fitconnect.entity.Appointment;
import com.fitconnect.entity.AppointmentStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AppointmentDTO {
    public Long id;
    public Long serviceRequestId;
    public String clientName; // To show the professional who is interested
    public String clientEmail; // For contact
    public String clientPhoneNumber; // For contact
    public String serviceRequestCategory;
    public String serviceRequestDescription;
    public LocalDateTime createdAt;
    public AppointmentStatus status;

    public AppointmentDTO(Appointment appointment) {
        this.id = appointment.id;
        this.serviceRequestId = appointment.getServiceRequest().id;
        if (appointment.getClient() != null) {
            this.clientName = appointment.getClient().getName();
            this.clientEmail = appointment.getClient().getEmail(); // Assuming User entity has email
            this.clientPhoneNumber = appointment.getClient().getPhoneNumber(); // Assuming User entity has phoneNumber
        }
        this.serviceRequestCategory = appointment.getServiceRequest().getCategory();
        this.serviceRequestDescription = appointment.getServiceRequest().getServiceDescription();
        this.createdAt = appointment.createdAt;
        this.status = appointment.getStatus();
    }
}
