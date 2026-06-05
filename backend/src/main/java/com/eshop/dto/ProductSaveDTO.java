package com.eshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSaveDTO {
    private Long id;
    @NotBlank(message = "商品名称不能为空")
    private String name;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    @NotNull(message = "库存不能为空")
    private Integer stock;
    private String description;
    private String mainImage;
    private String images;
    private String specs;
}
