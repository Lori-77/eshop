package com.eshop.service;

import com.eshop.dto.RefundDTO;
import com.eshop.entity.Refund;

import java.util.List;

public interface RefundService {
    void applyRefund(Long userId, RefundDTO dto);
    List<Refund> getUserRefunds(Long userId);
    List<Refund> getMerchantRefunds(Long merchantId);
    void handleRefund(Long merchantId, Long refundId, Boolean approved, String rejectReason);
}
