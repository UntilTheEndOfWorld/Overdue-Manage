package com.overdue.wechat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import com.overdue.manager.ums.mapper.MemberMapper;
import com.overdue.manager.ums.mapper.MemberWechatMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序订阅消息服务
 * 用于向用户发送订阅消息通知
 */
@Service
@Slf4j
public class WechatSubscribeMessageService {

  @Autowired
  private WechatPayData wechatPayData;

  @Autowired
  private MemberWechatMapper memberWechatMapper;

  @Autowired
  private MemberMapper memberMapper;

  private RestTemplate restTemplate = new RestTemplate();

  // 微信API地址
  private static final String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
  private static final String SEND_SUBSCRIBE_MESSAGE_URL = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s";

  /**
   * 获取微信小程序access_token
   */
  public String getAccessToken() {
    try {
      String url = String.format(GET_ACCESS_TOKEN_URL,
          wechatPayData.getMiniProgramAppId(),
          wechatPayData.getMiniProgramSecret());

      String response = restTemplate.getForObject(url, String.class);
      JSONObject jsonObject = JSON.parseObject(response);

      if (jsonObject.containsKey("access_token")) {
        String accessToken = jsonObject.getString("access_token");
        log.info("获取access_token成功");
        return accessToken;
      } else {
        log.error("获取access_token失败：{}", response);
        return null;
      }
    } catch (Exception e) {
      log.error("获取access_token异常", e);
      return null;
    }
  }

  /**
   * 发送新订单通知给经销商
   * 
   * @param orderId      订单ID
   * @param orderSn      订单号
   * @param memberName   下单用户昵称
   * @param totalAmount  订单金额
   * @param productNames 商品名称
   * @return 发送成功的经销商数量
   */
  public int sendNewOrderNotifyToDealer(Long orderId, String orderSn, String memberName, String totalAmount,
      String productNames) {
    try {
      // 获取所有已审核通过的经销商
      Map<String, Object> params = new HashMap<>();
      params.put("is_dealer", 1);
      params.put("dealer_status", 2); // 2-已通过
      java.util.List<Member> dealers = memberMapper.selectByMap(params);

      if (dealers == null || dealers.isEmpty()) {
        log.warn("没有找到经销商用户");
        return 0;
      }

      log.info("找到{}位经销商，准备发送订单通知", dealers.size());

      // 获取access_token
      String accessToken = getAccessToken();
      if (accessToken == null) {
        log.error("获取access_token失败，无法发送订阅消息");
        return 0;
      }

      int successCount = 0;

      // 向每个经销商发送通知
      for (Member dealer : dealers) {
        MemberWechat memberWechat = memberWechatMapper.selectByMemberId(dealer.getId());
        if (memberWechat == null || memberWechat.getRoutineOpenid() == null) {
          log.warn("经销商{}没有绑定微信小程序openid", dealer.getNickname());
          continue;
        }

        boolean sent = sendSubscribeMessage(
            accessToken,
            memberWechat.getRoutineOpenid(),
            orderId,
            orderSn,
            memberName,
            totalAmount,
            productNames);

        if (sent) {
          successCount++;
        }
      }

      log.info("订单通知发送完成，成功：{}/{}", successCount, dealers.size());
      return successCount;

    } catch (Exception e) {
      log.error("发送订单通知给经销商失败", e);
      return 0;
    }
  }

  /**
   * 发送订阅消息
   * 
   * @param accessToken  微信access_token
   * @param openid       用户openid
   * @param orderId      订单ID
   * @param orderSn      订单号
   * @param memberName   下单用户
   * @param totalAmount  订单金额
   * @param productNames 商品名称
   * @return 是否发送成功
   */
  private boolean sendSubscribeMessage(String accessToken, String openid, Long orderId,
      String orderSn, String memberName, String totalAmount, String productNames) {
    try {
      String url = String.format(SEND_SUBSCRIBE_MESSAGE_URL, accessToken);

      // 构建订阅消息数据
      // 注意：需要在微信小程序后台配置订阅消息模板
      // 模板示例：
      // 订单号：{{character_string1.DATA}}
      // 下单时间：{{time2.DATA}}
      // 订单金额：{{amount3.DATA}}
      // 商品名称：{{thing4.DATA}}
      Map<String, Object> data = new HashMap<>();

      // 订单号
      Map<String, String> character_string1 = new HashMap<>();
      character_string1.put("value", orderSn);
      data.put("character_string1", character_string1);

      // 下单时间
      Map<String, String> time2 = new HashMap<>();
      time2.put("value", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
      data.put("time2", time2);

      // 订单金额
      Map<String, String> amount3 = new HashMap<>();
      amount3.put("value", totalAmount + "元");
      data.put("amount3", amount3);

      // 商品名称（限制20个字符）
      Map<String, String> thing4 = new HashMap<>();
      String productName = productNames.length() > 20 ? productNames.substring(0, 17) + "..." : productNames;
      thing4.put("value", productName);
      data.put("thing4", thing4);

      // 下单用户
      Map<String, String> thing5 = new HashMap<>();
      String userName = memberName != null && memberName.length() > 20 ? memberName.substring(0, 17) + "..."
          : memberName;
      thing5.put("value", userName != null ? userName : "未知用户");
      data.put("thing5", thing5);

      // 构建完整请求体
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("touser", openid);
      requestBody.put("template_id", "请在这里填写您的订阅消息模板ID"); // 需要在微信小程序后台获取
      requestBody.put("page", "pages/dealer/order-detail/order-detail?id=" + orderId); // 点击跳转到订单详情页
      requestBody.put("data", data);
      requestBody.put("miniprogram_state", "formal"); // formal-正式版，developer-开发版，trial-体验版

      // 发送请求
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
      JSONObject responseJson = JSON.parseObject(response.getBody());

      if (responseJson.getInteger("errcode") == 0) {
        log.info("订阅消息发送成功 - openid: {}, 订单号: {}", openid, orderSn);
        return true;
      } else {
        log.error("订阅消息发送失败 - openid: {}, 订单号: {}, 错误: {}",
            openid, orderSn, responseJson.getString("errmsg"));
        return false;
      }

    } catch (Exception e) {
      log.error("发送订阅消息异常 - openid: {}, 订单号: {}", openid, orderSn, e);
      return false;
    }
  }

  /**
   * 发送发货通知给用户
   * 
   * @param openid          用户openid
   * @param orderSn         订单号
   * @param deliveryCompany 物流公司
   * @param deliverySn      物流单号
   * @return 是否发送成功
   */
  public boolean sendDeliveryNotify(String openid, String orderSn, String deliveryCompany, String deliverySn) {
    try {
      String accessToken = getAccessToken();
      if (accessToken == null) {
        return false;
      }

      String url = String.format(SEND_SUBSCRIBE_MESSAGE_URL, accessToken);

      // 构建发货通知数据
      Map<String, Object> data = new HashMap<>();

      // 订单号
      Map<String, String> character_string1 = new HashMap<>();
      character_string1.put("value", orderSn);
      data.put("character_string1", character_string1);

      // 物流公司
      Map<String, String> thing2 = new HashMap<>();
      thing2.put("value", deliveryCompany != null ? deliveryCompany : "暂无");
      data.put("thing2", thing2);

      // 物流单号
      Map<String, String> character_string3 = new HashMap<>();
      character_string3.put("value", deliverySn != null ? deliverySn : "暂无");
      data.put("character_string3", character_string3);

      // 发货时间
      Map<String, String> time4 = new HashMap<>();
      time4.put("value", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
      data.put("time4", time4);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("touser", openid);
      requestBody.put("template_id", "请在这里填写您的发货通知模板ID"); // 需要在微信小程序后台获取
      requestBody.put("page", "pages/order-detail/order-detail?orderSn=" + orderSn);
      requestBody.put("data", data);
      requestBody.put("miniprogram_state", "formal");

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(requestBody), headers);

      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
      JSONObject responseJson = JSON.parseObject(response.getBody());

      return responseJson.getInteger("errcode") == 0;

    } catch (Exception e) {
      log.error("发送发货通知异常", e);
      return false;
    }
  }
}
