package com.overdue.manager.pms.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.overdue.manager.pms.domain.entity.Brand;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌管理Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface BrandMapper extends BaseMapper<Brand> {
    /**
     * 查询品牌管理列表
     *
     * @param brand 品牌管理
     * @return 品牌管理集合
     */
    List<Brand> selectByEntity(Brand brand);
}
