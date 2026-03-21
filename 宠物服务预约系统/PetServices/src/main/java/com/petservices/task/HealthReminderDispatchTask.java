package com.petservices.task;

import com.petservices.service.HealthReminderMessageService;
import com.petservices.service.HealthReminderPipelineService;
import com.petservices.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReminderDispatchTask {

    private final UserService userService;
    private final HealthReminderPipelineService pipelineService;
    private final HealthReminderMessageService messageService;

    /**
     * 每小时跑一次：生成待推送消息 -> 标记发送
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void dispatchHourly() {
        List<Integer> subscribedUserIds = userService.getSubscribedUserIds();
        for (Integer userId : subscribedUserIds) {
            pipelineService.generatePending(userId);
        }
        messageService.sendPendingForAllSubscribedUsers(subscribedUserIds);
        log.info("health reminder dispatch done, users={}", subscribedUserIds.size());
    }
}
