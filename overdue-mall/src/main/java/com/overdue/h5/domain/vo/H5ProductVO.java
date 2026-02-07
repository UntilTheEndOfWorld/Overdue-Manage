package com.overdue.h5.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class H5ProductVO {
    private Long id;
    private String pic;
    private String name;
    private BigDecimal price;
    private String bannerTitle;
    private Integer supportExpress;
}
