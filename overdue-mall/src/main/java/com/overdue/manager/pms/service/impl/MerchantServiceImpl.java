package com.overdue.manager.pms.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.overdue.common.utils.DateUtils;
import com.overdue.common.utils.StringUtils;
import com.overdue.manager.pms.mapper.MerchantMapper;
import com.overdue.manager.pms.mapper.MerchantImageMapper;
import com.overdue.manager.pms.mapper.MerchantVideoMapper;
import com.overdue.manager.pms.mapper.MerchantCategoryMapper;
import com.overdue.manager.pms.domain.entity.Merchant;
import com.overdue.manager.pms.domain.entity.MerchantImage;
import com.overdue.manager.pms.domain.entity.MerchantVideo;
import com.overdue.manager.pms.domain.query.MerchantQuery;
import com.overdue.manager.pms.domain.vo.MerchantVo;
import com.overdue.manager.pms.service.MerchantService;
import com.overdue.manager.pms.convert.MerchantConvert;

/**
 * 商户信息Service业务层处理
 * 
 * @author zcc
 */
@Service
public class MerchantServiceImpl implements MerchantService {
  @Autowired
  private MerchantMapper merchantMapper;

  @Autowired
  private MerchantImageMapper merchantImageMapper;

  @Autowired
  private MerchantVideoMapper merchantVideoMapper;

  @Autowired
  private MerchantCategoryMapper merchantCategoryMapper;

  /**
   * 查询商户信息
   * 
   * @param id 商户信息主键
   * @return 商户信息
   */
  @Override
  public MerchantVo selectMerchantById(Long id) {
    Merchant merchant = merchantMapper.selectMerchantById(id);
    if (merchant == null) {
      return null;
    }

    MerchantVo merchantVo = MerchantConvert.INSTANCE.toVo(merchant);

    // 查询商户图片
    List<MerchantImage> imageList = merchantImageMapper.selectMerchantImageListByMerchantId(id);
    if (imageList != null && !imageList.isEmpty()) {
      merchantVo.setImageList(imageList.stream()
          .map(MerchantConvert.INSTANCE::toImageVo)
          .collect(Collectors.toList()));
    }

    // 查询商户视频
    List<MerchantVideo> videoList = merchantVideoMapper.selectMerchantVideoListByMerchantId(id);
    if (videoList != null && !videoList.isEmpty()) {
      merchantVo.setVideoList(videoList.stream()
          .map(MerchantConvert.INSTANCE::toVideoVo)
          .collect(Collectors.toList()));
    }

    return merchantVo;
  }

  /**
   * 查询商户信息列表
   * 
   * @param query 商户信息查询条件
   * @return 商户信息
   */
  @Override
  public List<MerchantVo> selectMerchantList(MerchantQuery query) {
    Merchant merchant = new Merchant();
    merchant.setMerchantName(query.getMerchantName());
    merchant.setMerchantCode(query.getMerchantCode());
    merchant.setStatus(query.getStatus());
    merchant.setDelFlag("0");

    List<Merchant> merchantList = merchantMapper.selectMerchantList(merchant);
    return merchantList.stream()
        .map(MerchantConvert.INSTANCE::toVo)
        .collect(Collectors.toList());
  }

  /**
   * 新增商户信息
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  @Override
  @Transactional
  public int insertMerchant(Merchant merchant) {
    merchant.setCreateTime(LocalDateTime.now());
    merchant.setDelFlag("0");
    if (merchant.getStatus() == null) {
      merchant.setStatus(1);
    }
    if (merchant.getSort() == null) {
      merchant.setSort(0);
    }

    int result = merchantMapper.insertMerchant(merchant);

    // 处理商户图片
    if (merchant.getImageList() != null && !merchant.getImageList().isEmpty()) {
      for (MerchantImage image : merchant.getImageList()) {
        image.setMerchantId(merchant.getId());
        image.setCreateTime(LocalDateTime.now());
        image.setDelFlag("0");
        merchantImageMapper.insertMerchantImage(image);
      }
    }

    // 处理商户视频
    if (merchant.getVideoList() != null && !merchant.getVideoList().isEmpty()) {
      for (MerchantVideo video : merchant.getVideoList()) {
        video.setMerchantId(merchant.getId());
        video.setCreateTime(LocalDateTime.now());
        video.setDelFlag("0");
        merchantVideoMapper.insertMerchantVideo(video);
      }
    }

    return result;
  }

  /**
   * 修改商户信息
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  @Override
  @Transactional
  public int updateMerchant(Merchant merchant) {
    merchant.setUpdateTime(LocalDateTime.now());

    int result = merchantMapper.updateMerchant(merchant);

    // 更新商户图片
    if (merchant.getImageList() != null) {
      // 先删除原有图片
      merchantImageMapper.deleteMerchantImageByMerchantId(merchant.getId());
      // 再插入新图片
      for (MerchantImage image : merchant.getImageList()) {
        image.setMerchantId(merchant.getId());
        image.setCreateTime(LocalDateTime.now());
        image.setDelFlag("0");
        merchantImageMapper.insertMerchantImage(image);
      }
    }

    // 更新商户视频
    if (merchant.getVideoList() != null) {
      // 先删除原有视频
      merchantVideoMapper.deleteMerchantVideoByMerchantId(merchant.getId());
      // 再插入新视频
      for (MerchantVideo video : merchant.getVideoList()) {
        video.setMerchantId(merchant.getId());
        video.setCreateTime(LocalDateTime.now());
        video.setDelFlag("0");
        merchantVideoMapper.insertMerchantVideo(video);
      }
    }

    return result;
  }

  /**
   * 批量删除商户信息
   * 
   * @param ids 需要删除的商户信息主键
   * @return 结果
   */
  @Override
  @Transactional
  public int deleteMerchantByIds(Long[] ids) {
    for (Long id : ids) {
      // 删除商户图片
      merchantImageMapper.deleteMerchantImageByMerchantId(id);
      // 删除商户视频
      merchantVideoMapper.deleteMerchantVideoByMerchantId(id);
    }
    return merchantMapper.deleteMerchantByIds(ids);
  }

  /**
   * 删除商户信息信息
   * 
   * @param id 商户信息主键
   * @return 结果
   */
  @Override
  @Transactional
  public int deleteMerchantById(Long id) {
    // 删除商户图片
    merchantImageMapper.deleteMerchantImageByMerchantId(id);
    // 删除商户视频
    merchantVideoMapper.deleteMerchantVideoByMerchantId(id);
    return merchantMapper.deleteMerchantById(id);
  }

  /**
   * 更新商户状态
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  @Override
  public int updateMerchantStatus(Merchant merchant) {
    return merchantMapper.updateMerchantStatus(merchant);
  }

  /**
   * 校验商户编码是否唯一
   * 
   * @param merchant 商户信息
   * @return 结果
   */
  @Override
  public boolean checkMerchantCodeUnique(Merchant merchant) {
    Long merchantId = StringUtils.isNull(merchant.getId()) ? -1L : merchant.getId();
    Merchant info = merchantMapper.selectMerchantByCode(merchant.getMerchantCode());
    if (StringUtils.isNotNull(info) && info.getId().longValue() != merchantId.longValue()) {
      return false;
    }
    return true;
  }
}
