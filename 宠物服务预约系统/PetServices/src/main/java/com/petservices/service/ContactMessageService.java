package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.entity.ContactMessage;
import com.petservices.mapper.ContactMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageMapper contactMessageMapper;

    public boolean submit(ContactMessage message) {
        if (message == null || message.getUserId() == null) {
            return false;
        }
        String content = message.getContent() == null ? "" : message.getContent().trim();
        if (content.isEmpty()) {
            return false;
        }
        message.setContent(content);
        message.setStatus(0);
        message.setCreateTime(new Date());
        return contactMessageMapper.insert(message) > 0;
    }

    public List<ContactMessage> listByUser(Integer userId) {
        QueryWrapper<ContactMessage> q = new QueryWrapper<>();
        q.eq("userId", userId).orderByDesc("createTime");
        return contactMessageMapper.selectList(q);
    }
}
