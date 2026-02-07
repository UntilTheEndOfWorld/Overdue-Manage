package com.overdue.manager.pms.mapper;

import java.util.List;
import com.overdue.manager.pms.domain.entity.MerchantCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户分类Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface MerchantCategoryMapper {
  /**
   * 查询商户分类
   * 
   * @param id 商户分类主键
   * @return 商户分类
   */
  public MerchantCategory selectMerchantCategoryById(Long id);

  /**
   * 查询商户分类列表
   * 
   * @param merchantCategory 商户分类
   * @return 商户分类集合
   */
  public List<MerchantCategory> selectMerchantCategoryList(MerchantCategory merchantCategory);

  /**
   * 查询所有启用的商户分类
   * 
   * @return 商户分类集合
   */
  public List<MerchantCategory> selectEnabledMerchantCategoryList();

  /**
   * 新增商户分类
   * 
   * @param merchantCategory 商户分类
   * @return 结果
   */
  public int insertMerchantCategory(MerchantCategory merchantCategory);

  /**
   * 修改商户分类
   * 
   * @param merchantCategory 商户分类
   * @return 结果
   */
  public int updateMerchantCategory(MerchantCategory merchantCategory);

  /**
   * 删除商户分类
   * 
   * @param id 商户分类主键
   * @return 结果
   */
  public int deleteMerchantCategoryById(Long id);

  /**
   * 批量删除商户分类
   * 
   * @param ids 需要删除的数据主键集合
   * @return 结果
   */
  public int deleteMerchantCategoryByIds(Long[] ids);

  /**
   * 根据分类编码查询分类
   * 
   * @param categoryCode 分类编码
   * @return 商户分类
   */
  public MerchantCategory selectMerchantCategoryByCode(String categoryCode);
}
