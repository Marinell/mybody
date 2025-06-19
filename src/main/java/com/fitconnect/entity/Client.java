package com.fitconnect.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class Client extends User {

    // Client-specific attributes can be added here if any in the future
    // For now, it inherits all from User and is distinguished by UserRole.CLIENT

    // serviceRequests list removed, will be queried

    public Client() {
        this.role = UserRole.CLIENT;
    }
}
