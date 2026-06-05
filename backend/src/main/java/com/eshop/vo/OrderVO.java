package com.eshop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private String merchantName;
    private BigDecimal totalAmount;
    private String status;
    private String receiverName;
    private String phone;
    private String address;
    private String remark;
    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;
    private LogisticsVO logistics;
    private RefundVO refund;

    @Data
    public static class OrderItemVO {
        private Long id;
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalAmount;
    }

    @Data
    public static class LogisticsVO {
        private Long id;
        private String company;
        private String trackingNo;
        private String status;
    }

    @Data
    public static class RefundVO {
        private Long id;
        private String reason;
        private String status;
        private String rejectReason;
        private BigDecimal amount;
    }
}
