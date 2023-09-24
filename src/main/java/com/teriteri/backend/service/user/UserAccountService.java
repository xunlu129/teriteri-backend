package com.teriteri.backend.service.user;

import com.teriteri.backend.pojo.CustomResponse;

import java.util.Map;

public interface UserAccountService {
    CustomResponse register(String username, String password, String confirmedPassword);

    CustomResponse login(String username, String password);

    CustomResponse personalInfo();

    void logout();
}
