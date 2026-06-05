package com.eshop.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private Long merchantId;
    private String merchantName;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String mainImage;
    private String images;
    private String specs;
    private String status;
    private Integer sales;
    private LocalDateTime createdAt;
}
