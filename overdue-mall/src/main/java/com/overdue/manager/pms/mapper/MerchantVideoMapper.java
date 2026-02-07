package com.overdue.manager.pms.mapper;

import java.util.List;
import com.overdue.manager.pms.domain.entity.MerchantVideo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商户视频Mapper接口
 * 
 * @author zcc
 */
@Mapper
public interface MerchantVideoMapper {
  /**
   * 查询商户视频
   * 
   * @param id 商户视频主键
   * @return 商户视频
   */
  public MerchantVideo selectMerchantVideoById(Long id);

  /**
   * 查询商户视频列表
   * 
   * @param merchantVideo 商户视频
   * @return 商户视频集合
   */
  public List<MerchantVideo> selectMerchantVideoList(MerchantVideo merchantVideo);

  /**
   * 根据商户ID查询视频列表
   * 
   * @param merchantId 商户ID
   * @return 商户视频集合
   */
  public List<MerchantVideo> selectMerchantVideoListByMerchantId(Long merchantId);

  /**
   * 新增商户视频
   * 
   * @param merchantVideo 商户视频
   * @return 结果
   */
  public int insertMerchantVideo(MerchantVideo merchantVideo);

  /**
   * 修改商户视频
   * 
   * @param merchantVideo 商户视频
   * @return 结果
   */
  public int updateMerchantVideo(MerchantVideo merchantVideo);

  /**
   * 删除商户视频
   * 
   * @param id 商户视频主键
   * @return 结果
   */
  public int deleteMerchantVideoById(Long id);

  /**
   * 批量删除商户视频
   * 
   * @param ids 需要删除的数据主键集合
   * @return 结果
   */
  public int deleteMerchantVideoByIds(Long[] ids);

  /**
   * 根据商户ID删除视频
   * 
   * @param merchantId 商户ID
   * @return 结果
   */
  public int deleteMerchantVideoByMerchantId(Long merchantId);
}
