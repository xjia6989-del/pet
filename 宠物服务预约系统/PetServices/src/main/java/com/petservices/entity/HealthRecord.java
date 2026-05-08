package com.petservices.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("health_record")
public class HealthRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "recordId", type = IdType.AUTO)
    private Integer recordId;

    private Integer petId;
    private Integer bookingId;
    private String recordType;
    private String recordTag;
    private Date recordDate;
    private String description;
    private String diagnosis;
    private String prescription;
    private Date nextDate;
    private Integer reminderStatus;
    private Integer createdBy;
    private Date createTime;
}