package com.fitconnect.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.List;
import jakarta.persistence.OneToMany;


@Entity
@Table(name = "clients")
public class Client extends User {

    // Client-specific attributes can be added here if any in the future
    // For now, it inherits all from User and is distinguished by UserRole.CLIENT

    @OneToMany(mappedBy = "client")
    public List<ServiceRequest> serviceRequests;

    public Client() {
        this.role = UserRole.CLIENT;
    }
}
