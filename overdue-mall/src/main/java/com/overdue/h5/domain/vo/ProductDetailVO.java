package com.overdue.h5.domain.vo;

import com.overdue.manager.pms.domain.entity.Brand;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.Sku;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetailVO {
    private Product product;
    private List<Sku> skus;
    private Brand brand;
}
