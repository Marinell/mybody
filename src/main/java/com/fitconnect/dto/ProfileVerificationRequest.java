package com.fitconnect.dto;

import com.fitconnect.entity.ProfileStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileVerificationRequest {
    public ProfileStatus newStatus;
}
