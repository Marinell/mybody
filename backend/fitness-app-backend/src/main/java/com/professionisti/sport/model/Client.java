package com.professionisti.sport.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
public class Client extends User {

    // Client-specific fields can be added here if any in the future.
    // For now, it primarily serves to distinguish role and relationships.

    @OneToMany(mappedBy = "client", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRequest> serviceRequests = new ArrayList<>();

    // Getters and Setters
    public List<ServiceRequest> getServiceRequests() { return serviceRequests; }
    public void setServiceRequests(List<ServiceRequest> serviceRequests) { this.serviceRequests = serviceRequests; }
}
