package com.overdue.notify.facade;

import com.overdue.common.utils.StringUtils;
import com.overdue.notify.config.OverdueNotifyConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 按 sys_config {@code overdue.notify.email.provider} 选择邮件实现。
 * smtp 需后续接入 JavaMail 后再扩展。
 */
@Service
@Slf4j
public class OverdueNotifyEmailFacade {

    @Autowired
    private OverdueNotifyConfigReader configReader;

    /**
     * @return 是否视为成功（noop/log 均返回 true）
     */
    public boolean send(String to, String subject, String textBody) {
        String provider = configReader.getEmailProvider();
        if ("noop".equals(provider)) {
            log.debug("[notify-email][noop] skip to={}", maskEmail(to));
            return true;
        }
        if ("log".equals(provider)) {
            log.info("[notify-email][log] to={} subject={} body={}", maskEmail(to), subject, textBody);
            return true;
        }
        if ("smtp".equals(provider)) {
            log.warn("[notify-email][smtp] not implemented yet, to={}", maskEmail(to));
            return false;
        }
        log.warn("[notify-email] unknown provider {}, fallback log", provider);
        log.info("[notify-email][fallback-log] to={} subject={}", maskEmail(to), subject);
        return true;
    }

    private static String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return "****";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String tail = local.length() > 2 ? local.substring(0, 2) + "***" : "***";
        return tail + email.substring(at);
    }
}
