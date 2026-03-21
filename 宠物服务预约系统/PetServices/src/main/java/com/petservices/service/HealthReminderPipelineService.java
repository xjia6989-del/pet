package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.dto.HealthReminderDto;
import com.petservices.entity.Pet;
import lombok.RequiredArgsConstructor;
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

            Date vaccineDue = nextOccurrence(baseDate, 365, now);
            if (isWithinNextDays(vaccineDue, now, 365)) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "疫苗", "建议进行年度疫苗加强", fmt.format(vaccineDue), mode));
            }

            Date dewormDue = nextOccurrence(baseDate, 90, now);
            if (isWithinNextDays(dewormDue, now, 365)) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "驱虫", "建议进行周期驱虫", fmt.format(dewormDue), mode));
            }

            Date checkupDue = nextOccurrence(baseDate, 180, now);
            if (isWithinNextDays(checkupDue, now, 365)) {
                reminders.add(new HealthReminderDto(pet.getPetId(), pet.getPetName(), "体检", "建议进行常规体检", fmt.format(checkupDue), mode));
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
