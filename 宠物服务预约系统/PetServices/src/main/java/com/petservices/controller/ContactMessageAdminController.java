package com.petservices.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.entity.ContactMessage;
import com.petservices.mapper.ContactMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin/contactMessage")
public class ContactMessageAdminController {

    @Autowired
    private ContactMessageMapper contactMessageMapper;

    /**
     * 管理员查看留言列表
     */
    @GetMapping("/list")
    public List<ContactMessage> list(@RequestParam(value = "status", required = false) Integer status) {
        QueryWrapper<ContactMessage> q = new QueryWrapper<>();
        if (status != null) {
            q.eq("status", status);
        }
        q.orderByDesc("createTime");
        return contactMessageMapper.selectList(q);
    }

    /**
     * 管理员回复留言并标记已处理
     */
    @PostMapping("/reply/{messageId}")
    public Boolean reply(@PathVariable Integer messageId,
                         @RequestParam("reply") String reply) {
        ContactMessage msg = contactMessageMapper.selectById(messageId);
        if (msg == null) {
            return false;
        }
        msg.setReply(reply);
        msg.setStatus(1);
        msg.setReplyTime(new Date());
        return contactMessageMapper.updateById(msg) > 0;
    }

    /**
     * 仅更新处理状态
     */
    @PostMapping("/status/{messageId}/{status}")
    public Boolean updateStatus(@PathVariable Integer messageId,
                                @PathVariable Integer status) {
        ContactMessage msg = contactMessageMapper.selectById(messageId);
        if (msg == null) {
            return false;
        }
        msg.setStatus(status == 1 ? 1 : 0);
        if (msg.getStatus() == 1 && msg.getReplyTime() == null) {
            msg.setReplyTime(new Date());
        }
        return contactMessageMapper.updateById(msg) > 0;
    }
}
