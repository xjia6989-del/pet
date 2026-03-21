package com.petservices.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pet")
public class Pet implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "petId", type = IdType.AUTO)
    private Integer petId;

    private Integer userId;
    private String petName;
    private String species;
    private String breed;
    private Integer age;
    private String vaccineRecord;
    private String medicalHistory;
    private Date birthDate;
    private BigDecimal weight;
    private Integer gender;
    private String avatar;
    private Date createTime;
}
