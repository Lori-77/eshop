package com.eshop.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
    private String storeName;
}
