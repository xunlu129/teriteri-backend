package com.teriteri.backend.service.user;

import com.teriteri.backend.pojo.dto.UserDTO;

public interface UserService {
    UserDTO getUserById(Integer id);
}
