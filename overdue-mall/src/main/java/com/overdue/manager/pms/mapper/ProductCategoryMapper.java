package com.overdue.manager.pms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.pms.domain.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
    /**
     * 查询商品分类列表
     *
     * @param productCategory 商品分类
     * @return 商品分类集合
     */
    List<ProductCategory> selectByEntity(ProductCategory productCategory);
}
