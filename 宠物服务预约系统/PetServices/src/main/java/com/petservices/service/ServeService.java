package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petservices.entity.Booking;
import com.petservices.entity.Serve;
import com.petservices.mapper.BookingMapper;
import com.petservices.mapper.ServeMapper;
import com.petservices.utils.UserFriendlyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author ${author}
 * @since 2025-02-23
 */
@Service
public class ServeService {

    @Autowired
    ServeMapper serveMapper;
    @Autowired
    BookingMapper bookingMapper;

    public Page<Serve> getServeList(Integer currentPage, Integer pageSize) {
        Page<Serve> userPage = new Page<>(currentPage, pageSize);
        Page<Serve> page = serveMapper.selectPage(userPage, null);
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            page.setRecords(buildDemoServeList());
            page.setTotal(page.getRecords().size());
        } else {
            page.setRecords(fillCompatibleFields(page.getRecords()));
        }
        return page;
    }


    public void updateServe(Serve serve) {
        serveMapper.updateById(serve);
    }

    public Page<Serve> addServe(Serve serve) {
        serveMapper.insert(serve);
        Page<Serve> page = new Page<>(1, 5);
        return serveMapper.selectPage(page, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Page<Serve> deleteServe(Integer serveId) {
        //查找是否有对应的服务预约
        List<Booking> bookings = bookingMapper.selectList(new LambdaQueryWrapper<Booking>().eq(Booking::getServe, serveId));
        UserFriendlyException.throwException(bookings.size() > 0 , "请先删除对应的服务预约信息!");
        serveMapper.deleteById(serveId);
        Page<Serve> page = new Page<>(1, 5);
        return serveMapper.selectPage(page, null);
    }

    public List<Serve> searchServe(String serveName,Integer category) {
        QueryWrapper<Serve> w = new QueryWrapper<>();
        if (StringUtils.isNotBlank(serveName)) {
            w.like("serveName", serveName);
        }
        if (category != null) {
            w.eq("category", category);
        }
        List<Serve> serves = serveMapper.selectList(w);
        if (serves == null || serves.isEmpty()) {
            return buildDemoServeList();
        }
        return fillCompatibleFields(serves);
    }

    public List<Serve> getAllServe() {
        List<Serve> serves = serveMapper.selectList(null);
        if (serves == null || serves.isEmpty()) {
            return buildDemoServeList();
        }
        return fillCompatibleFields(serves);
    }

    private List<Serve> fillCompatibleFields(List<Serve> serves) {
        for (Serve serve : serves) {
            serve.setServedId(serve.getServeId());
            if (StringUtils.isBlank(serve.getServeImage())) {
                serve.setServeImage("https://picsum.photos/seed/pet-service-" + serve.getServeId() + "/600/400");
            }
        }
        return serves;
    }

    private List<Serve> buildDemoServeList() {
        List<Serve> demoList = new ArrayList<>();

        Serve bath = new Serve();
        bath.setServeId(1001);
        bath.setServedId(1001);
        bath.setServeName("宠物基础洗护");
        bath.setCategory(1);
        bath.setPrice(88);
        bath.setServeDesc("包含洗澡、吹干、耳道清洁，适合日常护理展示。");
        bath.setServeImage("https://picsum.photos/seed/pet-bath/600/400");
        demoList.add(bath);

        Serve vaccine = new Serve();
        vaccine.setServeId(1002);
        vaccine.setServedId(1002);
        vaccine.setServeName("疫苗咨询与接种");
        vaccine.setCategory(2);
        vaccine.setPrice(168);
        vaccine.setServeDesc("提供常见疫苗咨询与预约接种，便于课堂演示预约流程。");
        vaccine.setServeImage("https://picsum.photos/seed/pet-vaccine/600/400");
        demoList.add(vaccine);

        Serve beauty = new Serve();
        beauty.setServeId(1003);
        beauty.setServedId(1003);
        beauty.setServeName("宠物美容修剪");
        beauty.setCategory(3);
        beauty.setPrice(128);
        beauty.setServeDesc("基础修剪和造型整理，快速展示服务项目列表效果。");
        beauty.setServeImage("https://picsum.photos/seed/pet-beauty/600/400");
        demoList.add(beauty);

        return demoList;
    }
}
