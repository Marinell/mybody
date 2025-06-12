package com.fitconnect.entity;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = "professionals") // callSuper = false as PanacheEntityBase has no fields for equals/hashCode by default
@ToString(exclude = "professionals")
public class Skill extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String name; // e.g., "Yoga Instruction", "Strength Training", "Nutrition Planning"

    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY)
    public List<Professional> professionals;
}
