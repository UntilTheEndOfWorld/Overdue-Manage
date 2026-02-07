package com.overdue.manager.pms.service;

import java.util.List;
import com.overdue.manager.pms.domain.entity.Merchant;
import com.overdue.manager.pms.domain.query.MerchantQuery;
import com.overdue.manager.pms.domain.vo.MerchantVo;

/**
 * 商户信息Service接口
 * 
 * @author zcc
 */
public interface MerchantService {
  /**
   * 查询商户信息
   * 
   * @param id 商户信息主键
   * @return 商户信息
   */
  public MerchantVo selectMerchantById(Long id);

  /**
   * 查询商户信息列表
   * 
   * @param query 商户信息查询条件
   * @return 商户信息集合
   */
  public List<MerchantVo> selectMerchantList(MerchantQuery query);

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
   * 批量删除商户信息
   * 
   * @param ids 需要删除的商户信息主键集合
   * @return 结果
   */
  public int deleteMerchantByIds(Long[] ids);

  /**
   * 删除商户信息信息
   * 
   * @param id 商户信息主键
   * @return 结果
   */
  public int deleteMerchantById(Long id);

  /**
   * 更新商户状态
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  public int updateMerchantStatus(Merchant merchant);

  /**
   * 校验商户编码是否唯一
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  public boolean checkMerchantCodeUnique(Merchant merchant);
}
