package com.overdue.manager.pms.mapper;

import java.util.List;
import com.overdue.manager.pms.domain.entity.MerchantImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户图片Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface MerchantImageMapper {
  /**
   * 查询商户图片
   * 
   * @param id 商户图片主键
   * @return 商户图片
   */
  public MerchantImage selectMerchantImageById(Long id);

  /**
   * 查询商户图片列表
   * 
   * @param merchantImage 商户图片
   * @return 商户图片集合
   */
  public List<MerchantImage> selectMerchantImageList(MerchantImage merchantImage);

  /**
   * 根据商户ID查询图片列表
   * 
   * @param merchantId 商户ID
   * @return 商户图片集合
   */
  public List<MerchantImage> selectMerchantImageListByMerchantId(Long merchantId);

  /**
   * 新增商户图片
   * 
   * @param merchantImage 商户图片
   * @return 结果
   */
  public int insertMerchantImage(MerchantImage merchantImage);

  /**
   * 修改商户图片
   * 
   * @param merchantImage 商户图片
   * @return 结果
   */
  public int updateMerchantImage(MerchantImage merchantImage);

  /**
   * 删除商户图片
   * 
   * @param id 商户图片主键
   * @return 结果
   */
  public int deleteMerchantImageById(Long id);

  /**
   * 批量删除商户图片
   * 
   * @param ids 需要删除的数据主键集合
   * @return 结果
   */
  public int deleteMerchantImageByIds(Long[] ids);

  /**
   * 根据商户ID删除图片
   * 
   * @param merchantId 商户ID
   * @return 结果
   */
  public int deleteMerchantImageByMerchantId(Long merchantId);
}
