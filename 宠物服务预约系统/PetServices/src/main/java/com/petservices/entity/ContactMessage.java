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
@TableName("contact_message")
public class ContactMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "messageId", type = IdType.AUTO)
    private Integer messageId;

    private Integer userId;
    private String username;
    private String name;
    private String phone;
    private String content;

    /** 0未处理 1已处理 */
    private Integer status;
    private String reply;

    private Date createTime;
    private Date replyTime;
}
