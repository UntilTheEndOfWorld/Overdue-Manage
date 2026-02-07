package com.overdue.manager.pms.convert;

import com.overdue.h5.domain.vo.H5ProductVO;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.vo.ProductVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 商品信息 DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface ProductConvert {

    List<ProductVO> dos2vos(List<Product> list);

    @Mapping(source = "isBanner", target = "isBanner")
    @Mapping(source = "bannerTitle", target = "bannerTitle")
    @Mapping(source = "supportExpress", target = "supportExpress")
    Product vo2do(ProductVO productVO);

    @Mapping(source = "isBanner", target = "isBanner")
    @Mapping(source = "bannerTitle", target = "bannerTitle")
    @Mapping(source = "supportExpress", target = "supportExpress")
    ProductVO do2vo(Product product);

    @Mapping(source = "bannerTitle", target = "bannerTitle")
    @Mapping(source = "supportExpress", target = "supportExpress")
    List<H5ProductVO> dos2dtos(List<Product> products);

    @Mapping(source = "bannerTitle", target = "bannerTitle")
    @Mapping(source = "supportExpress", target = "supportExpress")
    H5ProductVO productToH5ProductVO(Product product);
}
