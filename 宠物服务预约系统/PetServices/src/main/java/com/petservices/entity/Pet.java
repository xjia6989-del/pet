package com.petservices.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthDate;
    private BigDecimal weight;
    private Integer gender;
    private String avatar;
    /** 1正常 0停用 */
    private Integer status;
    /** 0正常 1需关注 2重点关注 */
    private Integer focusLevel;
    /** AI智能识别结果 */
    private String aiBreed;
    private String aiColor;
    private String aiSize;
    private String aiAgeEstimate;
    private String aiPersonality;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date createTime;
}
