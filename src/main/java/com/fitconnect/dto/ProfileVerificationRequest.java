package com.fitconnect.dto;

import com.fitconnect.entity.ProfileStatus;

public class ProfileVerificationRequest {
    public ProfileStatus newStatus;

    public ProfileStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ProfileStatus newStatus) {
        this.newStatus = newStatus;
    }
}
