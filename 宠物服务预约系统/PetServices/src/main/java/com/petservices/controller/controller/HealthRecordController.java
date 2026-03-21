package com.petservices.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.entity.HealthRecord;
import com.petservices.entity.Pet;
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
     * 添加健康记录（管理员）
     */
    @PostMapping("/add")
    public HealthRecord add(@RequestBody HealthRecord record, HttpSession session) {
        Integer adminId = (Integer) session.getAttribute("adminId");
        if (adminId == null) {
            return null;
        }
        record.setCreatedBy(adminId);
        record.setCreateTime(new Date());
        boolean saved = healthRecordService.save(record);
        return saved ? record : null;
    }

    /**
     * 修改健康记录（管理员）
     */
    @PostMapping("/update")
    public Boolean update(@RequestBody HealthRecord record, HttpSession session) {
        Integer adminId = (Integer) session.getAttribute("adminId");
        if (adminId == null) {
            return false;
        }
        return healthRecordService.updateById(record);
    }

    /**
     * 删除健康记录（管理员）
     */
    @DeleteMapping("/delete/{recordId}")
    public Boolean delete(@PathVariable Integer recordId, HttpSession session) {
        Integer adminId = (Integer) session.getAttribute("adminId");
        if (adminId == null) {
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

    private boolean isPetOwner(Integer petId, Integer userId) {
        Pet pet = petService.getById(petId);
        return pet != null && pet.getUserId() != null && pet.getUserId().equals(userId);
    }
}
