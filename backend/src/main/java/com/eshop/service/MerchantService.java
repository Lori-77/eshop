package com.eshop.service;

import com.eshop.dto.ShipDTO;
import com.eshop.vo.StatisticsVO;

public interface MerchantService {
    void shipOrder(Long merchantId, ShipDTO dto);
    StatisticsVO getMerchantStats(Long merchantId);
}
