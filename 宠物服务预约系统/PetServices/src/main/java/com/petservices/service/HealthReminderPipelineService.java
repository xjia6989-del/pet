package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.dto.HealthReminderDto;
import com.petservices.entity.Pet;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthReminderPipelineService {

    private final IPetService petService;
    private final HealthReminderMessageService messageService;

    public List<HealthReminderDto> buildReminders(Integer userId, String mode) {
        List<Pet> pets = petService.list(new QueryWrapper<Pet>().eq("userId", userId));
        List<HealthReminderDto> reminders = new ArrayList<>();
        Date now = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        for (Pet pet : pets) {
            Date baseDate = pet.getBirthDate() != null ? pet.getBirthDate() : pet.getCreateTime();
            if (baseDate == null) {
                baseDate = now;
            }

            int age = pet.getAge() == null ? -1 : pet.getAge();
            double weight = pet.getWeight() == null ? -1 : pet.getWeight().doubleValue();

            Date vaccineDue = nextOccurrence(baseDate, 365, now);
            if (isWithinNextDays(vaccineDue, now, 365)) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "疫苗", buildAdviceText("疫苗", age, weight), fmt.format(vaccineDue), mode, resolveLevel(vaccineDue, now), "system", "vaccine"));
            }

            Date dewormDue = nextOccurrence(baseDate, 90, now);
            if (isWithinNextDays(dewormDue, now, 365)) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "驱虫", buildAdviceText("驱虫", age, weight), fmt.format(dewormDue), mode, resolveLevel(dewormDue, now), "system", "deworm"));
            }

            Date checkupDue = nextOccurrence(baseDate, 180, now);
            if (isWithinNextDays(checkupDue, now, 365)) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "体检", buildAdviceText("体检", age, weight), fmt.format(checkupDue), mode, resolveLevel(checkupDue, now), "system", "checkup"));
            }

            if (age >= 8) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "高龄关注", "建议加强体检频率，关注老年宠物常见健康问题", fmt.format(checkupDue), mode, resolveLevel(checkupDue, now), "system", "senior"));
            }

            if (age > 0 && age < 1) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "幼宠关注", "建议优先完成疫苗接种，并按月关注健康变化", fmt.format(vaccineDue), mode, resolveLevel(vaccineDue, now), "system", "junior"));
            }

            if (weight >= 10) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "体重关注", "建议注意饮食管理与运动安排，避免体重继续上升", fmt.format(checkupDue), mode, resolveLevel(checkupDue, now), "system", "weight"));
            }
        }
        return reminders;
    }

    public int generateAndDispatch(Integer userId) {
        List<HealthReminderDto> reminders = buildReminders(userId, "subscribe");
        messageService.savePendingMessages(userId, reminders);
        return messageService.sendPendingForUser(userId);
    }

    public void generatePending(Integer userId) {
        List<HealthReminderDto> reminders = buildReminders(userId, "subscribe");
        messageService.savePendingMessages(userId, reminders);
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledGenerateDailyReminders() {
        messageService.purgeExpiredPendingMessages();
        List<Integer> userIds = messageService.getSubscribedUserIds();
        for (Integer userId : userIds) {
            generatePending(userId);
        }
    }

    @Scheduled(cron = "0 30 8 * * ?")
    public void scheduledDispatchDailyReminders() {
        List<Integer> userIds = messageService.getSubscribedUserIds();
        for (Integer userId : userIds) {
            messageService.sendPendingForUser(userId);
        }
    }

    public String resolveFocusReason(List<HealthReminderDto> reminders) {
        if (reminders == null || reminders.isEmpty()) {
            return "暂无";
        }
        long overdue = reminders.stream().filter(r -> "overdue".equals(r.getLevel())).count();
        if (overdue > 0) {
            return "存在已过期提醒";
        }
        long upcoming = reminders.stream().filter(r -> "upcoming".equals(r.getLevel())).count();
        if (upcoming > 0) {
            return "存在即将到期提醒";
        }
        return "常规关注";
    }

    public String resolvePetFocusReason(Integer petId) {
        if (petId == null) {
            return "暂无";
        }
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return "暂无";
        }
        List<HealthReminderDto> reminders = buildReminders(pet.getUserId(), "display");
        List<HealthReminderDto> petReminders = new ArrayList<>();
        for (HealthReminderDto dto : reminders) {
            if (petId.equals(dto.getPetId())) {
                petReminders.add(dto);
            }
        }
        return resolveFocusReason(petReminders);
    }

    public String buildAdviceSummary(Integer petId) {
        if (petId == null) {
            return "暂无建议";
        }
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return "暂无建议";
        }
        int age = pet.getAge() == null ? -1 : pet.getAge();
        double weight = pet.getWeight() == null ? -1 : pet.getWeight().doubleValue();
        if (age > 0 && age < 1) {
            return "建议优先关注疫苗接种和基础体检";
        }
        if (age >= 8) {
            return "建议适当提高体检频率，关注老年宠物健康变化";
        }
        if (weight >= 10) {
            return "建议注意饮食管理与运动安排，保持健康体重";
        }
        return "建议按常规节奏进行体检、驱虫和疫苗管理";
    }

    private String buildAdviceText(String type, int age, double weight) {
        if ("疫苗".equals(type)) {
            if (age > 0 && age < 1) {
                return "幼宠建议优先完成疫苗接种";
            }
            return "建议按计划完成疫苗加强";
        }
        if ("驱虫".equals(type)) {
            if (age > 0 && age < 1) {
                return "幼宠需更关注驱虫频率";
            }
            return "建议按周期完成驱虫";
        }
        if ("体检".equals(type)) {
            if (age >= 8) {
                return "老年宠物建议提高体检频率";
            }
            if (weight >= 10) {
                return "建议结合体重管理进行体检";
            }
            return "建议按计划完成常规体检";
        }
        return "建议按健康档案计划执行";
    }

    private String resolveLevel(Date dueDate, Date now) {
        if (dueDate == null || now == null) {
            return "normal";
        }
        long diff = dueDate.getTime() - now.getTime();
        long day = 24L * 60L * 60L * 1000L;
        if (diff < 0) {
            return "overdue";
        }
        if (diff <= 7 * day) {
            return "upcoming";
        }
        return "normal";
    }

    private Date nextOccurrence(Date baseDate, int cycleDays, Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        while (cal.getTime().before(now)) {
            cal.add(Calendar.DAY_OF_YEAR, cycleDays);
        }
        return cal.getTime();
    }

    private boolean isWithinNextDays(Date target, Date now, int days) {
        if (target == null) {
            return false;
        }
        long diff = target.getTime() - now.getTime();
        long max = days * 24L * 60L * 60L * 1000L;
        return diff >= 0 && diff <= max;
    }
}
