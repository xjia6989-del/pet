package com.petservices.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.dto.HealthReminderDto;
import com.petservices.entity.HealthRecord;
import com.petservices.entity.Pet;
import com.petservices.service.HealthReminderPipelineService;
import com.petservices.service.IHealthRecordService;
import com.petservices.service.IPetService;
import com.petservices.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private IPetService petService;
    @Autowired
    private UserService userService;
    @Autowired
    private HealthReminderPipelineService healthReminderPipelineService;
    @Autowired
    private IHealthRecordService healthRecordService;

    /**
     * 获取当前登录用户的宠物列表
     */
    @GetMapping("/list")
    public List<Pet> list(@RequestParam(value = "userId", required = false) Integer userId,
                          HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        if (currentUserId == null) {
            return java.util.Collections.emptyList();
        }
        return petService.list(new QueryWrapper<Pet>()
                .eq("userId", currentUserId)
                .orderByDesc("createTime"));
    }

    /**
     * 管理端：获取全部宠物列表
     */
    @GetMapping("/all")
    public List<Pet> all() {
        return petService.list(new QueryWrapper<Pet>().orderByDesc("createTime"));
    }


    /**
     * 添加宠物
     */
    @PostMapping("/add")
    public Pet add(@RequestBody Pet pet,
                   @RequestParam(value = "userId", required = false) Integer userId,
                   HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        if (currentUserId == null) {
            return null;
        }
        pet.setUserId(currentUserId);
        pet.setCreateTime(new Date());
        boolean saved = petService.save(pet);
        return saved ? pet : null;
    }

    /**
     * 修改宠物信息
     */
    @PostMapping("/update")
    public Boolean update(@RequestBody Pet pet,
                          @RequestParam(value = "userId", required = false) Integer userId,
                          HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        if (currentUserId == null || pet.getPetId() == null) {
            return false;
        }

        Pet old = petService.getById(pet.getPetId());
        if (old == null || !old.getUserId().equals(currentUserId)) {
            return false;
        }

        pet.setUserId(currentUserId);
        return petService.updateById(pet);
    }

    /**
     * 删除宠物
     */
    @DeleteMapping("/delete/{petId}")
    public Boolean delete(@PathVariable Integer petId,
                          @RequestParam(value = "userId", required = false) Integer userId,
                          HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        if (currentUserId == null) {
            return false;
        }
        Pet pet = petService.getById(petId);
        if (pet == null || !pet.getUserId().equals(currentUserId)) {
            return false;
        }
        return petService.removeById(petId);
    }

    /**
     * 健康管理提醒：优先读取 health_record.nextDate（真实业务数据），无记录时回退系统周期估算
     */
    @GetMapping("/healthReminders")
    public List<HealthReminderDto> healthReminders(@RequestParam(value = "userId", required = false) Integer userId,
                                                    @RequestParam(value = "mode", required = false, defaultValue = "display") String mode,
                                                    HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        if (currentUserId == null) {
            return java.util.Collections.emptyList();
        }
        List<Pet> pets = petService.list(new QueryWrapper<Pet>().eq("userId", currentUserId));

        List<HealthReminderDto> reminders = new ArrayList<>();
        Date now = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        for (Pet pet : pets) {
            // 1) 真实记录来源：管理员录入健康档案的 nextDate
            List<HealthRecord> healthRecords = healthRecordService.list(new QueryWrapper<HealthRecord>()
                    .eq("petId", pet.getPetId())
                    .isNotNull("nextDate")
                    .orderByAsc("nextDate"));

            for (HealthRecord r : healthRecords) {
                if (r.getNextDate() == null) {
                    continue;
                }
                String level = resolveLevel(r.getNextDate(), now);
                if (!"normal".equals(level)) {
                    reminders.add(new HealthReminderDto(
                            pet.getPetId(),
                            pet.getPetName(),
                            r.getRecordType() == null ? "健康记录" : r.getRecordType(),
                            "请按健康档案计划复查/处理（来源：健康档案）",
                            fmt.format(r.getNextDate()),
                            mode,
                            level,
                            "healthRecord"
                    ));
                }
            }

            // 2) 若无健康档案提醒，回退系统周期估算
            if (healthRecords == null || healthRecords.isEmpty()) {
                Date baseDate = pet.getBirthDate() != null ? pet.getBirthDate() : pet.getCreateTime();
                if (baseDate == null) {
                    baseDate = now;
                }

                int windowDays = 30;

                Date vaccineDue = nextOccurrence(baseDate, 365, now);
                if (isWithinNextDays(vaccineDue, now, windowDays)) {
                    reminders.add(new HealthReminderDto(
                            pet.getPetId(),
                            pet.getPetName(),
                            "疫苗",
                            "建议进行年度疫苗加强（来源：系统估算）",
                            fmt.format(vaccineDue),
                            mode,
                            "upcoming",
                            "system"
                    ));
                }

                Date dewormDue = nextOccurrence(baseDate, 90, now);
                if (isWithinNextDays(dewormDue, now, windowDays)) {
                    reminders.add(new HealthReminderDto(
                            pet.getPetId(),
                            pet.getPetName(),
                            "驱虫",
                            "建议进行周期驱虫（来源：系统估算）",
                            fmt.format(dewormDue),
                            mode,
                            "upcoming",
                            "system"
                    ));
                }

                Date checkupDue = nextOccurrence(baseDate, 180, now);
                if (isWithinNextDays(checkupDue, now, windowDays)) {
                    reminders.add(new HealthReminderDto(
                            pet.getPetId(),
                            pet.getPetName(),
                            "体检",
                            "建议进行常规体检（来源：系统估算）",
                            fmt.format(checkupDue),
                            mode,
                            "upcoming",
                            "system"
                    ));
                }
            }
        }

        if ("subscribe".equalsIgnoreCase(mode) && userService.getHealthReminderSubscribed(currentUserId) == 1) {
            healthReminderPipelineService.generatePending(currentUserId);
        }
        return reminders;
    }

    private String resolveLevel(Date dueDate, Date now) {
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

    private Integer resolveUserId(Integer userId, HttpSession session) {
        if (userId != null) {
            return userId;
        }
        Object sessionUserId = session.getAttribute("userId");
        if (sessionUserId instanceof Integer) {
            return (Integer) sessionUserId;
        }
        return null;
    }

    private boolean isWithinNextDays(Date target, Date now, int days) {
        if (target == null) {
            return false;
        }
        long diff = target.getTime() - now.getTime();
        long max = days * 24L * 60L * 60L * 1000L;
        return diff >= 0 && diff <= max;
    }

    private Date nextOccurrence(Date baseDate, int cycleDays, Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        while (cal.getTime().before(now)) {
            cal.add(Calendar.DAY_OF_YEAR, cycleDays);
        }
        return cal.getTime();
    }
}
