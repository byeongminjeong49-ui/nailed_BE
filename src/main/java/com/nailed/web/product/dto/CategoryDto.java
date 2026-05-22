package com.nailed.web.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryDto {
    private Long groupId;
    private String code;
    private String name;
    private String parentCode;
}
