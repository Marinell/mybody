package com.fitconnect.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Skill extends FirestoreEntity {

    public String name; // e.g., "Yoga Instruction", "Strength Training", "Nutrition Planning"

    // professionals list removed, will be handled by skillNames in Professional entity
}
