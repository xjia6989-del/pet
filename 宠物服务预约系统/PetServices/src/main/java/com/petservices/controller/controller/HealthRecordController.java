package com.petservices.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.entity.HealthRecord;
import com.petservices.entity.Pet;
import com.petservices.service.HealthReminderPipelineService;
import com.petservices.service.IHealthRecordService;
import com.petservices.service.IPetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/health")
public class HealthRecordController {

    @Autowired
    private IHealthRecordService healthRecordService;
    @Autowired
    private IPetService petService;
    @Autowired
    private HealthReminderPipelineService healthReminderPipelineService;

    /**
     * 根据宠物ID获取健康记录（仅宠物主人可查看）
     */
    @GetMapping("/list/{petId}")
    public List<HealthRecord> listByPet(@PathVariable Integer petId,
                                        @RequestParam(value = "userId", required = false) Integer userId,
                                        HttpSession session) {
        Integer currentUserId = resolveUserId(userId, session);
        if (currentUserId == null || !isPetOwner(petId, currentUserId)) {
            return java.util.Collections.emptyList();
        }
        return healthRecordService.list(new QueryWrapper<HealthRecord>()
                .eq("petId", petId)
                .orderByDesc("recordDate"));
    }

    /**
     * 管理员查看指定宠物的健康记录
     */
    @GetMapping("/admin/list/{petId}")
    public List<HealthRecord> listByPetForAdmin(@PathVariable Integer petId, HttpSession session) {
        return healthRecordService.list(new QueryWrapper<HealthRecord>()
                .eq("petId", petId)
                .orderByDesc("recordDate"));
    }

    /**
     * 兽医查看指定宠物的健康记录
     */
    @GetMapping("/vet/list/{petId}")
    public List<HealthRecord> listByPetForVet(@PathVariable Integer petId) {
        return healthRecordService.list(new QueryWrapper<HealthRecord>()
                .eq("petId", petId)
                .orderByDesc("recordDate"));
    }

    /**
     * 添加健康记录（管理员 / 兽医）
     */
    @PostMapping("/add")
    public HealthRecord add(@RequestBody HealthRecord record, HttpSession session) {
        Integer operatorId = (Integer) session.getAttribute("adminId");
        if (operatorId == null) {
            operatorId = (Integer) session.getAttribute("vetId");
        }
        if (operatorId == null) {
            operatorId = record.getCreatedBy();
        }
        if (operatorId == null) {
            return null;
        }
        if (record.getPetId() == null) {
            return null;
        }
        record.setCreatedBy(operatorId);
        if (record.getRecordDate() == null) {
            record.setRecordDate(new Date());
        }
        if (record.getCreateTime() == null) {
            record.setCreateTime(new Date());
        }
        applySmartFields(record);
        boolean saved = healthRecordService.save(record);
        if (!saved) {
            return null;
        }
        try {
            updatePetFocusLevel(record.getPetId());
        } catch (Exception ignore) {
            // 健康记录保存成功优先，宠物关注等级更新失败不影响主流程
        }
        return record;
    }

    /**
     * 修改健康记录（管理员 / 兽医）
     */
    @PostMapping("/update")
    public Boolean update(@RequestBody HealthRecord record, HttpSession session) {
        Integer operatorId = (Integer) session.getAttribute("adminId");
        if (operatorId == null) {
            operatorId = (Integer) session.getAttribute("vetId");
        }
        if (operatorId == null) {
            return false;
        }
        return healthRecordService.updateById(record);
    }

    /**
     * 删除健康记录（管理员 / 兽医）
     */
    @DeleteMapping("/delete/{recordId}")
    public Boolean delete(@PathVariable Integer recordId, HttpSession session) {
        Integer operatorId = (Integer) session.getAttribute("adminId");
        if (operatorId == null) {
            operatorId = (Integer) session.getAttribute("vetId");
        }
        if (operatorId == null) {
            return false;
        }
        return healthRecordService.removeById(recordId);
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

    private void applySmartFields(HealthRecord record) {
        if (record.getRecordTag() == null || record.getRecordTag().trim().isEmpty()) {
            String type = record.getRecordType() == null ? "" : record.getRecordType();
            if (type.contains("疫苗")) {
                record.setRecordTag("疫苗");
            } else if (type.contains("体检")) {
                record.setRecordTag("体检");
            } else if (type.contains("驱虫")) {
                record.setRecordTag("驱虫");
            } else if (type.contains("复查")) {
                record.setRecordTag("复查");
            } else {
                record.setRecordTag("治疗");
            }
        }

        if (record.getReminderStatus() == null) {
            Date now = new Date();
            if (record.getNextDate() == null) {
                record.setReminderStatus(1);
            } else if (record.getNextDate().before(now)) {
                record.setReminderStatus(3);
            } else {
                long diff = record.getNextDate().getTime() - now.getTime();
                long day = 24L * 60L * 60L * 1000L;
                record.setReminderStatus(diff <= 7 * day ? 2 : 1);
            }
        }
    }

    private void updatePetFocusLevel(Integer petId) {
        Pet pet = petService.getById(petId);
        if (pet == null) {
            return;
        }
        List<HealthRecord> records = healthRecordService.list(new QueryWrapper<HealthRecord>()
                .eq("petId", petId)
                .isNotNull("nextDate"));
        int level = 0;
        Date now = new Date();
        for (HealthRecord r : records) {
            if (r.getNextDate() == null) {
                continue;
            }
            long diff = r.getNextDate().getTime() - now.getTime();
            long day = 24L * 60L * 60L * 1000L;
            if (diff <= 7 * day) {
                level = 2;
                break;
            }
            if (diff <= 30 * day) {
                level = Math.max(level, 1);
            }
        }
        pet.setFocusLevel(level);
        petService.updateById(pet);
    }

    private String buildFocusReason(Integer petId) {
        return healthReminderPipelineService.resolvePetFocusReason(petId);
    }

    private boolean isPetOwner(Integer petId, Integer userId) {
        Pet pet = petService.getById(petId);
        return pet != null && pet.getUserId() != null && pet.getUserId().equals(userId);
    }
}
