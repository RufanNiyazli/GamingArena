package com.project.redis.service;

import com.project.redis.dto.UserProfileResponse;

public interface IUserProfileService {
    public UserProfileResponse readUserProfile(Long userId);
}
