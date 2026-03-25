package com.overdue.notify.facade;

import com.overdue.common.config.properties.SmsProperties;
import com.overdue.common.core.domain.model.SmsResult;
import com.overdue.common.core.sms.AliyunSmsTemplate;
import com.overdue.common.core.sms.SmsTemplate;
import com.overdue.common.utils.StringUtils;
import com.overdue.notify.config.OverdueNotifyConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 按 sys_config {@code overdue.notify.sms.provider} 选择短信实现。
 */
@Service
@Slf4j
public class OverdueNotifySmsFacade {

    @Autowired
    private OverdueNotifyConfigReader configReader;

    @Autowired
    private SmsProperties smsProperties;

    @Autowired(required = false)
    private ObjectProvider<SmsTemplate> smsTemplateProvider;

    /**
     * 发送模板短信（业务侧传入已在控制台申请的模板 ID）。
     */
    public SmsResult sendTemplate(String phones, String templateId, Map<String, String> param) {
        String provider = configReader.getSmsProvider();
        if ("noop".equals(provider)) {
            log.debug("[notify-sms][noop] skip phones={}", maskPhone(phones));
            return SmsResult.builder().isSuccess(true).message("noop").build();
        }
        if ("log".equals(provider)) {
            log.info("[notify-sms][log] phones={} templateId={} param={}", maskPhone(phones), templateId, param);
            return SmsResult.builder().isSuccess(true).message("log_only").build();
        }
        if ("aliyun".equals(provider)) {
            return sendAliyun(phones, templateId, param);
        }
        log.warn("[notify-sms] unknown provider {}, fallback noop", provider);
        return SmsResult.builder().isSuccess(false).message("unknown_provider:" + provider).build();
    }

    private SmsResult sendAliyun(String phones, String templateId, Map<String, String> param) {
        if (smsProperties.getEnabled() == null || !smsProperties.getEnabled()) {
            log.warn("[notify-sms][aliyun] sms.enabled=false");
            return SmsResult.builder().isSuccess(false).message("sms_disabled").build();
        }
        SmsTemplate template = smsTemplateProvider != null ? smsTemplateProvider.getIfAvailable() : null;
        if (template == null) {
            template = new AliyunSmsTemplate(smsProperties);
        }
        try {
            return template.send(phones, templateId, param);
        } catch (Exception e) {
            log.error("[notify-sms][aliyun] send failed", e);
            return SmsResult.builder().isSuccess(false).message(e.getMessage()).build();
        }
    }

    private static String maskPhone(String phones) {
        if (StringUtils.isEmpty(phones)) {
            return "";
        }
        if (phones.length() <= 7) {
            return "****";
        }
        return phones.substring(0, 3) + "****" + phones.substring(phones.length() - 4);
    }
}
