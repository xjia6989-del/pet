package com.petservices.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservices.entity.ContactMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContactMessageMapper extends BaseMapper<ContactMessage> {
}
