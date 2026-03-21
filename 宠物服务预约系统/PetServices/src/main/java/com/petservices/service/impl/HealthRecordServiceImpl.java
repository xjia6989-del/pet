package com.petservices.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petservices.entity.HealthRecord;
import com.petservices.mapper.HealthRecordMapper;
import com.petservices.service.IHealthRecordService;
import org.springframework.stereotype.Service;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements IHealthRecordService {
}