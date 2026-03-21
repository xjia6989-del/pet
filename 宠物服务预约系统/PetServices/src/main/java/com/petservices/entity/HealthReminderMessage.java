package com.petservices.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("health_reminder_message")
public class HealthReminderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "messageId", type = IdType.AUTO)
    private Integer messageId;

    private Integer userId;
    private Integer petId;
    private String petName;
    private String reminderType;
    private String reminderText;
    private String dueDate;

    /** 0待发送 1已发送 2发送失败 */
    private Integer status;

    /** 已重试次数 */
    private Integer retryCount;

    /** 最近一次失败原因 */
    private String failReason;

    /** 最近一次发送尝试时间 */
    private Date lastAttemptAt;

    private Date sentAt;
    private Date createTime;
}
