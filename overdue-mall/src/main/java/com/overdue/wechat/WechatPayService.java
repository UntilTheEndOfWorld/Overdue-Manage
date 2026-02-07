package com.overdue.wechat;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "wechat", name = "enabled", havingValue = "true")
public class WechatPayService {

    @Autowired
    private WechatPayData wechatPayData;

    /**
     * jsapi下单
     * 
     * @param orderNo     订单号
     * @param desc        订单描述
     * @param totalAmount 总金额，单位：分
     * @param openId      用户openid
     * @return prepay_id
     */
    public String jsapiPay(String orderNo, String desc, Integer totalAmount, String openId, Long memberId,
            String appId) {
        try {
            // 直接创建微信支付配置
            Config config = createWechatPayConfig();

            // 创建JsapiService
            JsapiService jsapiService = new JsapiService.Builder()
                    .config(config)
                    .build();

            // 构建支付请求
            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(appId);
            prepayRequest.setMchid(wechatPayData.getMerchantId());
            prepayRequest.setDescription(desc);
            prepayRequest.setOutTradeNo(orderNo);
            prepayRequest.setAttach(String.valueOf(memberId));
            prepayRequest.setNotifyUrl(wechatPayData.getNotifyUrl());
            Amount amount = new Amount();
            amount.setTotal(totalAmount);
            prepayRequest.setAmount(amount);
            Payer payer = new Payer();
            payer.setOpenid(openId);
            prepayRequest.setPayer(payer);

            // 调用微信支付接口
            PrepayResponse response = jsapiService.prepay(prepayRequest);
            return response.getPrepayId();
        } catch (Exception e) {
            log.error("微信支付调用失败", e);
            throw new RuntimeException("微信支付调用失败: " + e.getMessage());
        }
    }

    public Refund refundPay(String refundId, String payId, String notifyUrl, Long refundAmount, Long totalAmount,
            String reason) {
        try {
            // 直接创建微信支付配置
            Config config = createWechatPayConfig();

            // 创建RefundService
            RefundService refundService = new RefundService.Builder()
                    .config(config)
                    .build();

            // 请求参数
            CreateRequest request = new CreateRequest();
            request.setReason(reason);
            // 设置退款金额 根据自己的实际业务自行填写
            AmountReq amountReq = new AmountReq();
            amountReq.setRefund(refundAmount);
            amountReq.setTotal(totalAmount);
            amountReq.setCurrency("CNY");
            request.setAmount(amountReq);
            // 支付成功后回调回来的transactionId 按照实际情况填写
            request.setOutTradeNo(payId);
            // 支付成功后回调回来的transactionId 按照实际情况填写
            request.setOutRefundNo(refundId);
            // 退款成功的回调地址
            request.setNotifyUrl(notifyUrl);
            // 发起请求,申请退款
            return refundService.create(request);
        } catch (Exception e) {
            log.error("微信退款调用失败", e);
            throw new RuntimeException("微信退款调用失败: " + e.getMessage());
        }
    }

    /**
     * 创建微信支付配置
     */
    private Config createWechatPayConfig() {
        try {
            // 处理证书路径
            String certPath = resolveCertificatePath(wechatPayData.getPrivateKeyPath());
            // 处理证书路径
            String publicKeyPath = resolveCertificatePath(wechatPayData.getPublicKeyPath());
            return new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayData.getMerchantId())
                    .privateKeyFromPath(certPath)
                    .publicKeyFromPath(publicKeyPath)
                    .publicKeyId(wechatPayData.getPublicKeyId())
                    .merchantSerialNumber(wechatPayData.getMerchantSerialNumber())
                    .apiV3Key(wechatPayData.getApiV3key())
                    .build();
        } catch (Exception e) {
            log.error("创建微信支付配置失败", e);
            throw new RuntimeException("创建微信支付配置失败: " + e.getMessage());
        }
    }

    /**
     * 解析证书路径
     */
    private String resolveCertificatePath(String configPath) {
        // 首先尝试从classpath加载
        try {
            java.net.URL resourceUrl = getClass().getClassLoader().getResource(configPath);
            if (resourceUrl != null) {
                String path = resourceUrl.getPath();
                // 处理Windows路径问题：去掉开头的斜杠
                if (path.startsWith("/") && path.length() > 2 && path.charAt(2) == ':') {
                    path = path.substring(1);
                }
                // 处理URL编码
                try {
                    path = java.net.URLDecoder.decode(path, "UTF-8");
                } catch (Exception e) {
                    log.warn("URL解码失败，使用原始路径", e);
                }
                log.info("从classpath找到证书文件: {}", path);
                return path;
            }
        } catch (Exception e) {
            log.debug("无法从classpath加载证书文件", e);
        }

        // 如果classpath中没有找到，尝试直接使用配置的路径
        java.io.File certFile = new java.io.File(configPath);
        if (certFile.exists()) {
            log.info("使用配置文件中的路径: {}", configPath);
            return configPath;
        }

        // 如果仍然找不到，抛出异常
        String errorMessage = "微信支付证书文件不存在: " + configPath + "\n\n";
        errorMessage += "🔍 错误分析: 缺少微信支付证书文件\n\n";
        errorMessage += "📋 解决步骤:\n";
        errorMessage += "1. 登录微信商户平台: https://pay.weixin.qq.com/\n";
        errorMessage += "2. 进入 账户中心 → API安全\n";
        errorMessage += "3. 下载API证书\n";
        errorMessage += "4. 将私钥文件重命名为 apiclient_key.pem\n";
        errorMessage += "5. 放置在 /etc/overdue-store-cert/ 目录下（生产环境）\n";
        errorMessage += "   或放置在 overdue-admin/src/main/resources/cert/ 目录下（开发环境）\n\n";
        errorMessage += "📁 当前工作目录: " + System.getProperty("user.dir") + "\n";
        errorMessage += "📁 期望证书路径: " + configPath + "\n\n";
        errorMessage += "💡 提示: 如果暂时不需要微信支付功能，可以在配置文件中设置 wechat.enabled=false";

        throw new RuntimeException(errorMessage);
    }

    /**
     * 获取微信支付配置（供回调使用）
     */
    public Config getWechatPayConfig() {
        return createWechatPayConfig();
    }
}
