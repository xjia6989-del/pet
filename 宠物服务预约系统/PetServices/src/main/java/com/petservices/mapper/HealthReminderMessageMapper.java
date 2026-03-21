package com.petservices.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petservices.entity.HealthReminderMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthReminderMessageMapper extends BaseMapper<HealthReminderMessage> {
}
