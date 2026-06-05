package com.eshop.service;

import com.eshop.dto.LoginDTO;
import com.eshop.dto.RegisterDTO;
import com.eshop.vo.LoginVO;

public interface AuthService {
    LoginVO login(LoginDTO dto);
    void register(RegisterDTO dto);
    LoginVO getCurrentUser(Long userId);
}
