package com.eshop.service;

import com.eshop.dto.PayDTO;

public interface PaymentService {
    void pay(Long userId, PayDTO dto);
}
