package com.eshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotBlank(message = "物流公司不能为空")
    private String company;
    @NotBlank(message = "运单号不能为空")
    private String trackingNo;
}
