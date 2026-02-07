package com.overdue.wechat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 微信支付服务测试
 * 验证微信支付服务可以正常注入和使用
 */
@SpringBootTest
@ActiveProfiles("dev")
public class WechatPayServiceLazyLoadTest {

    @Autowired
    private WechatPayService wechatPayService;

    @Test
    public void testWechatPayServiceInjection() {
        // 测试微信支付服务可以正常注入

        System.out.println("✅ 应用启动成功，微信支付服务已注入");
        System.out.println("📝 微信支付配置在调用时动态创建");
        System.out.println("🔧 如果需要测试支付功能，请先配置证书文件");

        // 验证服务实例存在
        assertNotNull(wechatPayService, "WechatPayService应该被成功注入");

        System.out.println("✅ 服务注入测试通过");
    }

    @Test
    public void testServiceAvailability() {
        // 验证服务可用性
        // 这里我们只是验证服务可以正常注入，而不实际调用支付方法

        System.out.println("🔍 验证微信支付服务可用性...");

        // 服务应该存在
        assertNotNull(wechatPayService);

        System.out.println("✅ 服务可用性验证通过");
        System.out.println("💡 配置在调用 jsapiPay() 或 refundPay() 方法时动态创建");
    }
}
