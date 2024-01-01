package com.teriteri.backend.service.user;

import com.teriteri.backend.pojo.dto.UserDTO;

public interface UserService {
    /**
     * 根据uid查询用户信息
     * @param id 用户ID
     * @return 用户可见信息实体类 UserDTO
     */
    UserDTO getUserById(Integer id);
}
