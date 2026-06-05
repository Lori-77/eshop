package com.eshop.service;

import com.eshop.common.PageResult;
import com.eshop.vo.StatisticsVO;
import com.eshop.vo.UserVO;

public interface AdminService {
    // 用户管理
    PageResult<UserVO> getUsers(String keyword, String role, Integer status, Integer page, Integer size);
    void updateUserStatus(Long userId, Integer status);

    // 商家管理
    PageResult<UserVO> getMerchants(String keyword, Integer status, Integer page, Integer size);
    void updateMerchantStatus(Long merchantId, Integer status);

    // 数据统计
    StatisticsVO getStats();
    Object getChartData(String type);
}
