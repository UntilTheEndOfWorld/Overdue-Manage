package com.overdue.manager.pms.mapper;

import java.util.List;
import com.overdue.manager.pms.domain.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户信息Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface MerchantMapper {
  /**
   * 查询商户信息
   * 
   * @param id 商户信息主键
   * @return 商户信息
   */
  public Merchant selectMerchantById(Long id);

  /**
   * 查询商户信息列表
   * 
   * @param merchant 商户信息
   * @return 商户信息集合
   */
  public List<Merchant> selectMerchantList(Merchant merchant);

  /**
   * 新增商户信息
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  public int insertMerchant(Merchant merchant);

  /**
   * 修改商户信息
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  public int updateMerchant(Merchant merchant);

  /**
   * 删除商户信息
   * 
   * @param id 商户信息主键
   * @return 结果
   */
  public int deleteMerchantById(Long id);

  /**
   * 批量删除商户信息
   * 
   * @param ids 需要删除的数据主键集合
   * @return 结果
   */
  public int deleteMerchantByIds(Long[] ids);

  /**
   * 根据商户编码查询商户信息
   * 
   * @param merchantCode 商户编码
   * @return 商户信息
   */
  public Merchant selectMerchantByCode(String merchantCode);

  /**
   * 更新商户状态
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  public int updateMerchantStatus(Merchant merchant);
}
