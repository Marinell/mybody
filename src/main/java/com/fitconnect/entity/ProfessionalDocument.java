package com.fitconnect.entity;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "professional_documents")
@Getter
@Setter
@NoArgsConstructor
public class ProfessionalDocument extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    public Professional professional;

    public String fileName;
    public String fileType; // e.g., application/pdf, image/jpeg
    public String storagePath; // Path where the file is stored
}
