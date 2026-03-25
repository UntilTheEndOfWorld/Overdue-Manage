package com.overdue.notify.reminder;

import com.overdue.common.utils.StringUtils;
import com.overdue.manager.item.domain.entity.OverdueReminderSent;
import com.overdue.manager.item.domain.entity.OverdueUser;
import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.mapper.OverdueReminderSentMapper;
import com.overdue.manager.item.mapper.OverdueUserMapper;
import com.overdue.manager.item.mapper.PersonalItemMapper;
import com.overdue.notify.facade.OverdueNotifyAsyncFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 扫描个人物品到期日（今日、今日+3 天），写入去重表并入队或直发。
 */
@Service
@Slf4j
public class OverdueReminderScanService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private PersonalItemMapper personalItemMapper;

    @Autowired
    private OverdueUserMapper overdueUserMapper;

    @Autowired
    private OverdueReminderSentMapper overdueReminderSentMapper;

    @Autowired
    private OverdueNotifyAsyncFacade asyncFacade;

    @Autowired
    private OverdueReminderDeliveryService deliveryService;

    /**
     * 每日扫描：到期前第 3 天、到期当天各提醒一次（同一自然日去重）。
     */
    public void scanAndDispatch() {
        LocalDate today = LocalDate.now();
        LocalDate d3 = today.plusDays(3);
        Set<LocalDate> dates = new HashSet<>(Arrays.asList(d3, today));
        List<PersonalItem> items = personalItemMapper.selectByExpiryDatesIn(dates, "0");
        if (items == null || items.isEmpty()) {
            log.debug("[reminder-scan] no items for dates {}", dates);
            return;
        }

        for (PersonalItem item : items) {
            if (item.getExpiryDate() == null || item.getUserId() == null) {
                continue;
            }
            String phase;
            if (item.getExpiryDate().equals(d3)) {
                phase = "T3";
            } else if (item.getExpiryDate().equals(today)) {
                phase = "T0";
            } else {
                continue;
            }

            OverdueUser user = overdueUserMapper.selectById(item.getUserId());
            if (user == null) {
                continue;
            }
            if (!"1".equals(user.getReminderEnabled())) {
                continue;
            }

            OverdueReminderTaskMessage msg = OverdueReminderTaskMessage.builder()
                .userId(user.getId())
                .itemId(item.getId())
                .itemName(item.getName())
                .expiryDate(ISO.format(item.getExpiryDate()))
                .phase(phase)
                .phone(user.getPhone())
                .email(user.getEmail())
                .openid(user.getOpenid())
                .reminderEnabled(true)
                .wantSms("1".equals(user.getReminderSms()) && StringUtils.isNotEmpty(user.getPhone()))
                .wantEmail("1".equals(user.getReminderEmail()) && StringUtils.isNotEmpty(user.getEmail()))
                .wantWx("1".equals(user.getReminderSubscribe()) && StringUtils.isNotEmpty(user.getOpenid()))
                .build();

            OverdueReminderSent row = new OverdueReminderSent();
            row.setUserId(user.getId());
            row.setItemId(item.getId());
            row.setPhase(phase);
            row.setRemindDate(today);

            try {
                overdueReminderSentMapper.insert(row);
            } catch (DataIntegrityViolationException e) {
                continue;
            }

            boolean ok = publish(msg);
            if (!ok) {
                overdueReminderSentMapper.deleteById(row.getId());
                log.warn("[reminder-scan] publish failed, rolled back dedupe row itemId={}", item.getId());
            }
        }
    }

    private boolean publish(OverdueReminderTaskMessage msg) {
        if (asyncFacade.isNoneMode()) {
            return false;
        }
        if (asyncFacade.isDirectMode()) {
            return deliveryService.deliver(msg);
        }
        return asyncFacade.enqueue(msg);
    }
}
