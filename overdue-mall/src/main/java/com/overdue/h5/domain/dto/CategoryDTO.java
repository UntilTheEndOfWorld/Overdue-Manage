package com.overdue.h5.domain.dto;

import com.overdue.h5.domain.vo.H5ProductVO;
import lombok.Data;

import java.util.List;

/**
 * 分类数据传输对象
 */
@Data
public class CategoryDTO {
    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类排序
     */
    private Integer sort;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 分类级别
     */
    private Integer level;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 子分类列表
     */
    private List<CategoryDTO> children;

    /**
     * 该分类下的商品列表
     */
    private List<H5ProductVO> productList;
}
