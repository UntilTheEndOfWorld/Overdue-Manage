package com.overdue.manager.pms.convert;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import com.overdue.manager.pms.domain.entity.Merchant;
import com.overdue.manager.pms.domain.entity.MerchantImage;
import com.overdue.manager.pms.domain.entity.MerchantVideo;
import com.overdue.manager.pms.domain.entity.MerchantCategory;
import com.overdue.manager.pms.domain.vo.MerchantVo;
import com.overdue.manager.pms.domain.vo.MerchantImageVo;
import com.overdue.manager.pms.domain.vo.MerchantVideoVo;
import com.overdue.manager.pms.domain.vo.MerchantCategoryVo;

/**
 * 商户信息转换
 * 
 * @author zcc
 */
@Mapper
public interface MerchantConvert {

  MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

  /**
   * 商户实体转VO
   */
  @Mapping(target = "imageList", ignore = true)
  @Mapping(target = "videoList", ignore = true)
  @Mapping(target = "categoryList", ignore = true)
  MerchantVo toVo(Merchant merchant);

  /**
   * 商户图片实体转VO
   */
  @Mapping(target = "imageTypeName", expression = "java(getImageTypeName(image.getImageType()))")
  MerchantImageVo toImageVo(MerchantImage image);

  /**
   * 商户视频实体转VO
   */
  MerchantVideoVo toVideoVo(MerchantVideo video);

  /**
   * 商户分类实体转VO
   */
  MerchantCategoryVo toCategoryVo(MerchantCategory category);

  /**
   * 获取图片类型名称
   */
  default String getImageTypeName(Integer imageType) {
    if (imageType == null) {
      return "";
    }
    switch (imageType) {
      case 1:
        return "环境照片";
      case 2:
        return "产品照片";
      case 3:
        return "其他";
      default:
        return "";
    }
  }
}
