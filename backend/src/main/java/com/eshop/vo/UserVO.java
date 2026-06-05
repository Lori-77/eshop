package com.eshop.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private String role;
    private String storeName;
    private String storeDesc;
    private Integer status;
    private LocalDateTime createdAt;
}
