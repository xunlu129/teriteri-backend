package com.teriteri.backend.service.user;

import com.teriteri.backend.pojo.dto.UserDTO;

import java.util.List;

public interface UserService {
    /**
     * 根据uid查询用户信息
     * @param id 用户ID
     * @return 用户可见信息实体类 UserDTO
     */
    UserDTO getUserById(Integer id);

    /**
     * 根据有序uid列表查询用户信息
     * @param list 用户id列表
     * @return  用户信息列表
     */
    List<UserDTO> getUserByIdList(List<Integer> list);
}
