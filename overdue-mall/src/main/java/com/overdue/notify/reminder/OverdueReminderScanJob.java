package com.overdue.notify.reminder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时扫描到期提醒并入队（或直发）。cron 可在 application.yml 覆盖。
 */
@Component
@Slf4j
public class OverdueReminderScanJob {

    @Autowired
    private OverdueReminderScanService scanService;

    @Scheduled(cron = "${overdue.reminder.scan.cron:0 0 8 * * ?}")
    public void run() {
        log.info("[reminder-scan] start");
        try {
            scanService.scanAndDispatch();
        } catch (Exception e) {
            log.error("[reminder-scan] error", e);
        }
        log.info("[reminder-scan] end");
    }
}
