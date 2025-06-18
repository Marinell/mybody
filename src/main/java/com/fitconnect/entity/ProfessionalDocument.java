package com.fitconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "professional_documents")
@Setter
@NoArgsConstructor
public class ProfessionalDocument extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Changed to private as good practice, with Panache providing accessors

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private Professional professional; // Changed to private

    private String fileName;
    private String fileType; // e.g., application/pdf, image/jpeg
    private String storagePath;

    @JsonIgnore
    @Lob // For large binary data
    @Column(columnDefinition="BLOB")
    private byte[] fileContent;

    // PanacheEntityBase provides id getter/setter.
    // Lombok @Getter @Setter will handle getters and setters for other fields.
    // Explicit getters/setters for clarity or if specific logic is needed later:
}
