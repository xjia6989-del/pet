package com.petservices.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("booking_slot_config")
public class BookingSlotConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer serveId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date configDate;

    private String timeSlot;

    /** 1开放 0关闭 */
    private Integer enabled;

    private Integer capacity;
}