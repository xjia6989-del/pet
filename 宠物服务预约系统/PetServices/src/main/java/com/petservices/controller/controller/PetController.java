package com.petservices.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.dto.HealthReminderDto;
import com.petservices.dto.PetVisionRecognitionDto;
import com.petservices.entity.HealthRecord;
import com.petservices.entity.Pet;
import com.petservices.service.HealthReminderPipelineService;
import com.petservices.service.IHealthRecordService;
import com.petservices.service.IPetService;
import com.petservices.service.UserService;
import com.petservices.service.PetVisionRecognitionService;
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
    @Autowired
    private PetVisionRecognitionService petVisionRecognitionService;

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
                .orderByAsc("petId"));
    }

    /**
     * 管理端：获取全部宠物列表
     */
    @GetMapping("/all")
    public List<Pet> all() {
        List<Pet> pets = petService.list(new QueryWrapper<Pet>().orderByAsc("petId"));
        for (Pet pet : pets) {
            pet.setFocusLevel(resolveFocusLevel(pet.getPetId()));
        }
        pets.sort((a, b) -> {
            int focusDiff = Integer.compare(b.getFocusLevel() == null ? 0 : b.getFocusLevel(), a.getFocusLevel() == null ? 0 : a.getFocusLevel());
            if (focusDiff != 0) return focusDiff;
            return Integer.compare(a.getPetId() == null ? 0 : a.getPetId(), b.getPetId() == null ? 0 : b.getPetId());
        });
        return pets;
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
        pet.setStatus(1);
        boolean saved = petService.save(pet);
        return saved ? pet : null;
    }

    /**
     * 智能识别宠物图片信息（百度智能云宠物/动物识别 + 本地性格侧写）
     */
    @PostMapping("/vision/recognize")
    public PetVisionRecognitionDto recognizePetVision(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return petVisionRecognitionService.recognize(file);
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
     * 删除宠物（用户）
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
     * 停用宠物（管理员）
     */
    @DeleteMapping("/admin/delete/{petId}")
    public Boolean disableByAdmin(@PathVariable Integer petId,
                                  @RequestParam(value = "adminId", required = false) Integer adminId,
                                  HttpSession session) {
        Integer currentAdminId = adminId;
        if (currentAdminId == null) {
            Object sessionAdminId = session.getAttribute("adminId");
            if (sessionAdminId instanceof Integer) {
                currentAdminId = (Integer) sessionAdminId;
            }
        }
        if (currentAdminId == null) {
            return false;
        }

        Pet pet = petService.getById(petId);
        if (pet == null) {
            return false;
        }
        pet.setStatus(0);
        return petService.updateById(pet);
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
                            "healthRecord",
                            r.getRecordTag()
                    ));
                }
            }

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
                            "system",
                            "vaccine"
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
                            "system",
                            "deworm"
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
                            "system",
                            "checkup"
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

    private Integer resolveFocusLevel(Integer petId) {
        if (petId == null) {
            return 0;
        }
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return 0;
        }
        if (pet.getFocusLevel() != null) {
            return pet.getFocusLevel();
        }
        return 0;
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
