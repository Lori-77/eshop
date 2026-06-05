package com.eshop.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private Long categoryId;
    private String keyword;
    private String status;
    private Long merchantId;
    private Integer page = 1;
    private Integer size = 12;
    private String sort; // price_asc, price_desc, sales_desc, new_desc
}
