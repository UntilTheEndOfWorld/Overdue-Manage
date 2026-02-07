package com.overdue.wechat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "wechat")
@Component("WechatPayData")
public class WechatPayData {

    /** 商户号 */
    private String appId;
    private String secret;
    private String merchantId;
    private String publicKeyId;
    /** 商户API私钥路径 */
    private String privateKeyPath;
    /** 商户API私钥路径 */
    private String publicKeyPath;
    /** 商户证书序列号 */
    private String merchantSerialNumber;
    /** 商户APIV3密钥 */
    private String apiV3key;
    private String notifyUrl;
    private String miniProgramAppId;
    private String miniProgramSecret;

}
