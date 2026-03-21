package com.petservices.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthReminderDto {
    private Integer petId;
    private String petName;
    private String reminderType; // 疫苗/驱虫/体检/复诊/健康记录
    private String reminderText;
    private String dueDate; // yyyy-MM-dd
    private String mode; // display/subscribe
    private String level; // overdue/upcoming/normal
    private String source; // healthRecord/system

    // 兼容旧代码调用（6参数）
    public HealthReminderDto(Integer petId,
                             String petName,
                             String reminderType,
                             String reminderText,
                             String dueDate,
                             String mode) {
        this.petId = petId;
        this.petName = petName;
        this.reminderType = reminderType;
        this.reminderText = reminderText;
        this.dueDate = dueDate;
        this.mode = mode;
        this.level = "upcoming";
        this.source = "system";
    }
}
