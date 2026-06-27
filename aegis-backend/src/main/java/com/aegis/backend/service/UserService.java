package com.aegis.backend.service;

import com.aegis.backend.dto.RegisterRequest;
import com.aegis.backend.entity.User;

public interface UserService {
    User registerUser(RegisterRequest registerRequest);
}
