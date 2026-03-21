package com.petservices.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petservices.entity.Pet;
import com.petservices.mapper.PetMapper;
import com.petservices.service.IPetService;
import org.springframework.stereotype.Service;

@Service
public class PetServiceImpl extends ServiceImpl<PetMapper, Pet> implements IPetService {
}