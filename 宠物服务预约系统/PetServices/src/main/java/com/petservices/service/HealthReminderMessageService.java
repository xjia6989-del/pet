package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.dto.HealthReminderDto;
import com.petservices.entity.HealthReminderMessage;
import com.petservices.mapper.HealthReminderMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class HealthReminderMessageService {

    private static final int MAX_RETRY = 3;
    private final Random random = new Random();

    private final HealthReminderMessageMapper messageMapper;

    public void savePendingMessages(Integer userId, List<HealthReminderDto> reminders) {
        if (userId == null || reminders == null || reminders.isEmpty()) {
            return;
        }
        for (HealthReminderDto dto : reminders) {
            QueryWrapper<HealthReminderMessage> q = new QueryWrapper<>();
            q.eq("userId", userId)
                    .eq("petId", dto.getPetId())
                    .eq("reminderType", dto.getReminderType())
                    .eq("dueDate", dto.getDueDate());
            Integer count = messageMapper.selectCount(q);
            if (count != null && count > 0) {
                continue;
            }
            HealthReminderMessage msg = new HealthReminderMessage();
            msg.setUserId(userId);
            msg.setPetId(dto.getPetId());
            msg.setPetName(dto.getPetName());
            msg.setReminderType(dto.getReminderType());
            msg.setReminderText(dto.getReminderText());
            msg.setDueDate(dto.getDueDate());
            msg.setStatus(0);
            msg.setRetryCount(0);
            msg.setFailReason(null);
            msg.setLastAttemptAt(null);
            msg.setCreateTime(new Date());
            messageMapper.insert(msg);
        }
    }

    public int sendPendingForUser(Integer userId) {
        QueryWrapper<HealthReminderMessage> q = new QueryWrapper<>();
        q.eq("userId", userId)
                .in("status", Arrays.asList(0, 2))
                .lt("retryCount", MAX_RETRY);
        List<HealthReminderMessage> pending = messageMapper.selectList(q);
        int sent = 0;
        for (HealthReminderMessage msg : pending) {
            if (attemptSend(msg, false)) {
                sent++;
            }
        }
        return sent;
    }

    public boolean resendMessage(Integer messageId) {
        HealthReminderMessage msg = messageMapper.selectById(messageId);
        if (msg == null) {
            return false;
        }
        return attemptSend(msg, true);
    }

    public List<HealthReminderMessage> getMessages(Integer userId, Integer status) {
        QueryWrapper<HealthReminderMessage> q = new QueryWrapper<>();
        q.eq("userId", userId);
        if (status != null) {
            q.eq("status", status);
        }
        q.orderByDesc("createTime");
        return messageMapper.selectList(q);
    }

    public void sendPendingForAllSubscribedUsers(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Integer userId : userIds) {
            sendPendingForUser(userId);
        }
    }

    public List<HealthReminderDto> toDtoList(List<HealthReminderMessage> msgs) {
        List<HealthReminderDto> list = new ArrayList<>();
        for (HealthReminderMessage msg : msgs) {
            list.add(new HealthReminderDto(
                    msg.getPetId(),
                    msg.getPetName(),
                    msg.getReminderType(),
                    msg.getReminderText(),
                    msg.getDueDate(),
                    msg.getStatus() != null && msg.getStatus() == 1 ? "subscribe" : "display",
                    "upcoming",
                    "healthRecord"
            ));
        }
        return list;
    }

    private boolean attemptSend(HealthReminderMessage msg, boolean forceResend) {
        int nextRetry = (msg.getRetryCount() == null ? 0 : msg.getRetryCount()) + 1;
        Date now = new Date();
        msg.setLastAttemptAt(now);

        // 当前为演示环境：90%成功，10%失败，用于演示重试机制
        boolean sendSuccess = random.nextInt(10) != 0;

        if (sendSuccess) {
            msg.setStatus(1);
            msg.setSentAt(now);
            msg.setFailReason(null);
            if (forceResend) {
                msg.setRetryCount(nextRetry);
            }
            messageMapper.updateById(msg);
            return true;
        }

        msg.setStatus(2);
        msg.setRetryCount(nextRetry);
        msg.setFailReason("模拟发送失败，请稍后重试");
        if (nextRetry >= MAX_RETRY && !forceResend) {
            msg.setFailReason("已达最大重试次数(" + MAX_RETRY + ")");
        }
        messageMapper.updateById(msg);
        return false;
    }
}
