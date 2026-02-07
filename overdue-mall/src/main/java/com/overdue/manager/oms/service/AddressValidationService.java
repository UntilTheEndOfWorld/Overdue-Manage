package com.overdue.manager.oms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 地址验证服务
 * 
 * @author zcc
 */
@Service
@Slf4j
public class AddressValidationService {

  /**
   * 验证地址是否支持同城配送
   * 
   * @param province      省份
   * @param city          城市
   * @param district      区县
   * @param detailAddress 详细地址
   * @return 是否支持同城配送
   */
  public boolean isSupportLocalDelivery(String province, String city, String district, String detailAddress) {
    // 将所有地址信息拼接在一起
    String fullAddress = buildFullAddress(province, city, district, detailAddress);

    // 检查是否包含靖西市或靖西
    boolean isSupported = containsJingxiCity(fullAddress);

    log.info("地址验证 - 完整地址: {}, 是否支持同城配送: {}", fullAddress, isSupported);

    return isSupported;
  }

  /**
   * 构建完整地址字符串
   * 
   * @param province      省份
   * @param city          城市
   * @param district      区县
   * @param detailAddress 详细地址
   * @return 完整地址
   */
  private String buildFullAddress(String province, String city, String district, String detailAddress) {
    StringBuilder address = new StringBuilder();

    if (StringUtils.hasText(province)) {
      address.append(province.trim());
    }

    if (StringUtils.hasText(city)) {
      address.append(city.trim());
    }

    if (StringUtils.hasText(district)) {
      address.append(district.trim());
    }

    if (StringUtils.hasText(detailAddress)) {
      address.append(detailAddress.trim());
    }

    // 移除所有空格，便于匹配
    return address.toString().replaceAll("\\s", "");
  }

  /**
   * 检查地址是否包含靖西市相关信息
   * 
   * @param fullAddress 完整地址
   * @return 是否包含靖西市
   */
  private boolean containsJingxiCity(String fullAddress) {
    if (!StringUtils.hasText(fullAddress)) {
      return false;
    }

    // 支持的地址模式
    String[] jingxiPatterns = {
        "靖西市",
        "靖西县",
        "靖西",
        "广西壮族自治区靖西",
        "广西靖西",
        "百色市靖西"
    };

    // 检查是否包含任一模式
    for (String pattern : jingxiPatterns) {
      if (fullAddress.contains(pattern)) {
        log.debug("地址匹配成功 - 地址: {}, 匹配模式: {}", fullAddress, pattern);
        return true;
      }
    }

    log.debug("地址匹配失败 - 地址: {}, 不在靖西市范围内", fullAddress);
    return false;
  }

  /**
   * 获取同城配送服务范围描述
   * 
   * @return 服务范围描述
   */
  public String getLocalDeliveryServiceArea() {
    return "靖西市城区";
  }

  /**
   * 获取同城配送限制提示信息
   * 
   * @return 提示信息
   */
  public String getLocalDeliveryLimitMessage() {
    return "只有靖西市城区才能进行同城配送，超过市区，暂时无法提供同城配送服务！";
  }
}