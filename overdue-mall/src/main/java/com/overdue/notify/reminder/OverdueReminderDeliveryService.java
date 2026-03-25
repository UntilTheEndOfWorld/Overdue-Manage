package com.overdue.notify.reminder;

import com.overdue.common.core.domain.model.SmsResult;
import com.overdue.common.utils.StringUtils;
import com.overdue.notify.config.OverdueNotifyConfigReader;
import com.overdue.notify.facade.OverdueNotifyEmailFacade;
import com.overdue.notify.facade.OverdueNotifySmsFacade;
import com.overdue.notify.facade.OverdueNotifyWxSubscribeFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 消费提醒任务：按用户开关与 sys_config 渠道发送短信、邮件、订阅消息。
 */
@Service
@Slf4j
public class OverdueReminderDeliveryService {

    @Autowired
    private OverdueNotifyConfigReader configReader;

    @Autowired
    private OverdueNotifySmsFacade smsFacade;

    @Autowired
    private OverdueNotifyEmailFacade emailFacade;

    @Autowired
    private OverdueNotifyWxSubscribeFacade wxFacade;

    /**
     * @return 是否全部尝试成功（任一渠道失败仍可能返回 true，仅记录日志）
     */
    public boolean deliver(OverdueReminderTaskMessage msg) {
        if (msg == null) {
            return false;
        }
        if (!msg.isReminderEnabled()) {
            log.debug("[reminder] user {} disabled reminder", msg.getUserId());
            return true;
        }
        String phaseText = "T3".equals(msg.getPhase()) ? "还有3天到期" : "今日到期";
        boolean any = false;

        String smsTpl = configReader.getSmsReminderTemplateId();
        if (msg.isWantSms() && StringUtils.isNotEmpty(smsTpl) && StringUtils.isNotEmpty(msg.getPhone())) {
            Map<String, String> param = new HashMap<>();
            param.put("name", msg.getItemName() != null ? msg.getItemName() : "");
            param.put("date", msg.getExpiryDate() != null ? msg.getExpiryDate() : "");
            param.put("tip", phaseText);
            SmsResult sr = smsFacade.sendTemplate(msg.getPhone(), smsTpl, param);
            if (sr != null && sr.isSuccess()) {
                any = true;
            } else {
                log.warn("[reminder] sms not success userId={} itemId={} msg={}", msg.getUserId(), msg.getItemId(),
                    sr != null ? sr.getMessage() : "");
            }
        }

        if (msg.isWantEmail() && StringUtils.isNotEmpty(msg.getEmail())) {
            String subject = "【过期提醒】" + phaseText + "：" + (msg.getItemName() != null ? msg.getItemName() : "物品");
            String body = "物品：" + msg.getItemName() + "\n到期日：" + msg.getExpiryDate() + "\n说明：" + phaseText;
            try {
                if (emailFacade.send(msg.getEmail(), subject, body)) {
                    any = true;
                }
            } catch (Exception e) {
                log.error("[reminder] email failed userId={} itemId={}", msg.getUserId(), msg.getItemId(), e);
            }
        }

        String wxTpl = configReader.getWxReminderTemplateId();
        if (msg.isWantWx() && StringUtils.isNotEmpty(wxTpl) && StringUtils.isNotEmpty(msg.getOpenid())) {
            Map<String, Map<String, String>> data = new HashMap<>();
            Map<String, String> thing1 = new HashMap<>();
            thing1.put("value", (msg.getItemName() != null ? msg.getItemName() : "物品") + " " + phaseText + " " + msg.getExpiryDate());
            data.put("thing1", thing1);
            try {
                if (wxFacade.send(msg.getOpenid(), wxTpl, data, "pages/personal/personal")) {
                    any = true;
                }
            } catch (Exception e) {
                log.error("[reminder] wx failed userId={} itemId={}", msg.getUserId(), msg.getItemId(), e);
            }
        }

        if (!any && (msg.isWantSms() || msg.isWantEmail() || msg.isWantWx())) {
            log.warn("[reminder] nothing sent (template empty or contact missing) userId={} itemId={}", msg.getUserId(),
                msg.getItemId());
        }
        return true;
    }
}
