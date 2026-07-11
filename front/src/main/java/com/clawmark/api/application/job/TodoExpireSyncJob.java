package com.clawmark.api.application.job;

import com.clawmark.api.application.service.TodoAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class TodoExpireSyncJob {

    @Resource
    private TodoAppService todoAppService;

    @Scheduled(cron = "${todo.expire-sync-cron:0 */5 * * * ?}")
    public void syncExpireStatus() {
        int updated = todoAppService.markExpiredTodos();
        if (updated > 0) {
            log.info("todo expire sync updated rows: {}", updated);
        }
    }
}
