package com.eshop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundHandleDTO {
    @NotNull(message = "退款ID不能为空")
    private Long refundId;
    @NotNull(message = "处理结果不能为空")
    private Boolean approved;
    private String rejectReason;
}
