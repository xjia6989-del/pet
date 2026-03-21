package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.config.AppConsts;
import com.petservices.dto.BookingListDto;
import com.petservices.dto.PageDto;
import com.petservices.entity.Booking;
import com.petservices.entity.Serve;
import com.petservices.mapper.BookingMapper;
import com.petservices.mapper.ServeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final long CANCEL_WINDOW_MILLIS = TimeUnit.HOURS.toMillis(2);

    @Autowired
    BookingMapper bookingMapper;
    @Autowired
    ServeMapper serveMapper;

    public PageDto getBookingList(Integer currentPage, Integer pageSize) {
        PageDto pageDto = new PageDto();
        List<BookingListDto> list = bookingMapper.getAllBooking(currentPage - 1, pageSize);
        pageDto.setRecords(list);
        pageDto.setTotal(list.size());
        return pageDto;
    }

    public PageDto updateBooking(Integer bookingId, Integer flag) {
        Booking booking = bookingMapper.selectById(bookingId);
        booking.setFlag(flag);
        bookingMapper.updateById(booking);
        return getBookingList(1, 8);
    }

    public boolean addBooking(Booking booking) {
        if (booking.getBookingDate() == null || booking.getBookingDate().before(new Date())) {
            return false;
        }

        if (booking.getServe() == null || serveMapper.selectById(booking.getServe()) == null) {
            List<Serve> serveList = serveMapper.selectList(null);
            if (serveList != null && !serveList.isEmpty()) {
                booking.setServe(serveList.get(0).getServeId());
            }
        }
        if (booking.getServe() == null) {
            return false;
        }

        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        wrapper.eq("serve", booking.getServe())
                .eq("bookingDate", booking.getBookingDate())
                .in("flag", Arrays.asList(1, 2));
        Integer count = bookingMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            return false;
        }

        booking.setFlag(1);
        int rows = bookingMapper.insert(booking);
        return rows > 0;
    }

    public List<String> getAvailableSlots(Integer serveId, Date date) {
        List<String> fullSlots = Arrays.asList(
                "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"
        );

        if (serveId == null || date == null) {
            return fullSlots;
        }

        SimpleDateFormat dayFmt = new SimpleDateFormat(AppConsts.dateFormatPattern);
        String targetDay = dayFmt.format(date);
        SimpleDateFormat slotFmt = new SimpleDateFormat("HH:mm");

        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        wrapper.eq("serve", serveId)
                .in("flag", Arrays.asList(1, 2));
        List<Booking> booked = bookingMapper.selectList(wrapper);

        List<String> bookedSlots = booked.stream()
                .filter(b -> b.getBookingDate() != null && targetDay.equals(dayFmt.format(b.getBookingDate())))
                .map(b -> slotFmt.format(b.getBookingDate()))
                .collect(Collectors.toList());

        Date now = new Date();
        String today = dayFmt.format(now);

        return fullSlots.stream()
                .filter(s -> !bookedSlots.contains(s))
                .filter(s -> {
                    if (!today.equals(targetDay)) {
                        return true;
                    }
                    return s.compareTo(slotFmt.format(now)) > 0;
                })
                .collect(Collectors.toList());
    }

    public boolean cancelBooking(Integer bookingId, Integer userId) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null || !booking.getUserId().equals(userId)) {
            return false;
        }
        if (booking.getFlag() == null || booking.getFlag() != 1) {
            return false;
        }
        if (booking.getBookingDate() == null) {
            return false;
        }

        Date now = new Date();
        long diff = booking.getBookingDate().getTime() - now.getTime();
        if (diff < CANCEL_WINDOW_MILLIS) {
            return false;
        }

        booking.setFlag(4);
        return bookingMapper.updateById(booking) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public PageDto deleteBooking(Integer bookingId) {
        bookingMapper.deleteById(bookingId);
        return getBookingList(1, 8);
    }

    public List<BookingListDto> searchBooking(Integer flag) {
        return bookingMapper.getAllBookingByFlag(flag);
    }

    public List<BookingListDto> getMyBooking(Integer userId) {
        return bookingMapper.getMyBooking(userId);
    }

    public List<BookingListDto> updateBookingComment(Booking booking) {
        Booking b = bookingMapper.selectById(booking.getBookingId());
        b.setAppraise(booking.getAppraise());
        b.setStar(booking.getStar());
        b.setFlag(3);
        bookingMapper.updateById(b);
        return getMyBooking(b.getUserId());
    }
}
