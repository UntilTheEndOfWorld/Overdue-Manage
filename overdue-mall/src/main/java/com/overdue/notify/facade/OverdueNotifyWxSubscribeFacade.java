package com.overdue.notify.facade;

import com.overdue.notify.config.OverdueNotifyConfigReader;
import com.overdue.wechat.WechatSubscribeMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 按 sys_config {@code overdue.notify.wx.subscribe.provider} 选择微信订阅消息实现。
 */
@Service
@Slf4j
public class OverdueNotifyWxSubscribeFacade {

    @Autowired
    private OverdueNotifyConfigReader configReader;

    @Autowired(required = false)
    private WechatSubscribeMessageService wechatSubscribeMessageService;

    /**
     * 发送订阅消息（data 为微信要求的 thing1 / date2 等 → value 结构）。
     *
     * @param page 小程序跳转路径，可 null
     */
    public boolean send(String openid, String templateId, Map<String, Map<String, String>> data, String page) {
        String provider = configReader.getWxSubscribeProvider();
        if ("noop".equals(provider)) {
            log.debug("[notify-wx][noop] skip openid={}", maskOpenid(openid));
            return true;
        }
        if ("log".equals(provider)) {
            log.info("[notify-wx][log] openid={} templateId={} data={} page={}", maskOpenid(openid), templateId, data, page);
            return true;
        }
        if ("wechat".equals(provider)) {
            if (wechatSubscribeMessageService == null) {
                log.error("[notify-wx] WechatSubscribeMessageService not available");
                return false;
            }
            return wechatSubscribeMessageService.sendGenericSubscribeMessage(openid, templateId, data, page);
        }
        log.warn("[notify-wx] unknown provider {}, fallback log", provider);
        log.info("[notify-wx][fallback-log] openid={} templateId={}", maskOpenid(openid), templateId);
        return true;
    }

    private static String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) {
            return "****";
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }
}
