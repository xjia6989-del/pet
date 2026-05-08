package com.petservices.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petservices.config.AppConsts;
import com.petservices.dto.BookingListDto;
import com.petservices.dto.BookingSlotConfigDto;
import com.petservices.dto.PageDto;
import com.petservices.entity.Booking;
import com.petservices.entity.BookingSlotConfig;
import com.petservices.entity.Serve;
import com.petservices.mapper.BookingMapper;
import com.petservices.mapper.BookingSlotConfigMapper;
import com.petservices.mapper.ServeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final long CANCEL_WINDOW_MILLIS = TimeUnit.HOURS.toMillis(2);
    private static final List<String> FULL_SLOTS = Arrays.asList(
            "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"
    );

    @Autowired
    BookingMapper bookingMapper;
    @Autowired
    ServeMapper serveMapper;
    @Autowired
    BookingSlotConfigMapper bookingSlotConfigMapper;

    public PageDto getBookingList(Integer currentPage, Integer pageSize) {
        PageDto pageDto = new PageDto();
        List<BookingListDto> list = bookingMapper.getAllBooking((currentPage - 1) * pageSize, pageSize);
        pageDto.setRecords(list);
        pageDto.setTotal(Math.toIntExact(bookingMapper.selectCount(null)));
        return pageDto;
    }

    public PageDto updateBooking(Integer bookingId, Integer flag) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            return getBookingList(1, 8);
        }
        booking.setFlag(flag);
        bookingMapper.updateById(booking);
        return getBookingList(1, 8);
    }

    public Boolean assignVet(Integer bookingId, Integer vetId) {
        if (bookingId == null || vetId == null) {
            return false;
        }
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            return false;
        }
        booking.setVetId(vetId);
        return bookingMapper.updateById(booking) > 0;
    }

    public boolean addBooking(Booking booking) {
        if (booking == null || booking.getBookingDate() == null) {
            return false;
        }
        if (booking.getBookingDate().before(new Date())) {
            return false;
        }
        if (booking.getServe() == null || serveMapper.selectById(booking.getServe()) == null) {
            return false;
        }
        if (booking.getPetId() == null) {
            return false;
        }

        SimpleDateFormat dayFmt = new SimpleDateFormat(AppConsts.dateFormatPattern);
        SimpleDateFormat slotFmt = new SimpleDateFormat("HH:mm");
        String day = dayFmt.format(booking.getBookingDate());
        String slot = slotFmt.format(booking.getBookingDate());

        BookingSlotConfig config = getSlotConfigOrNull(booking.getServe(), day, slot);
        if (config != null && config.getEnabled() != null && config.getEnabled() == 0) {
            return false;
        }

        int capacity = 1;
        if (config != null && config.getCapacity() != null && config.getCapacity() > 0) {
            capacity = config.getCapacity();
        }

        synchronized (buildSlotKey(booking.getServe(), day, slot).intern()) {
            int latestBookedCount = countBookedByBooking(booking.getServe(), day, slot);
            if (latestBookedCount >= capacity) {
                return false;
            }
            booking.setFlag(1);
            boolean inserted = bookingMapper.insert(booking) > 0;
            if (inserted) {
                log.info("booking inserted with capacity check, serveId={}, day={}, slot={}, booked={}, capacity={}", booking.getServe(), day, slot, latestBookedCount + 1, capacity);
            }
            return inserted;
        }
    }

    public List<String> getAllDefaultSlots() {
        return FULL_SLOTS;
    }

    public List<String> getAvailableSlots(Integer serveId, Date date) {
        if (date == null) {
            return FULL_SLOTS;
        }
        SimpleDateFormat dayFmt = new SimpleDateFormat(AppConsts.dateFormatPattern);
        SimpleDateFormat slotFmt = new SimpleDateFormat("HH:mm");
        String targetDay = dayFmt.format(date);
        String today = dayFmt.format(new Date());

        return FULL_SLOTS.stream().filter(slot -> {
            BookingSlotConfig config = getSlotConfigOrNull(serveId, targetDay, slot);
            if (config != null && config.getEnabled() != null && config.getEnabled() == 0) {
                return false;
            }
            int capacity = 1;
            if (config != null && config.getCapacity() != null && config.getCapacity() > 0) {
                capacity = config.getCapacity();
            }
            if (countBookedByBooking(serveId, targetDay, slot) >= capacity) {
                return false;
            }
            if (!today.equals(targetDay)) {
                return true;
            }
            return slot.compareTo(slotFmt.format(new Date())) > 0;
        }).collect(Collectors.toList());
    }

    public List<BookingSlotConfigDto> getSlotConfig(Integer serveId, String date) {
        List<BookingSlotConfigDto> result = new ArrayList<>();
        if (serveId == null || date == null || date.trim().isEmpty()) {
            return result;
        }
        for (String slot : FULL_SLOTS) {
            BookingSlotConfig cfg = getSlotConfigOrNull(serveId, date, slot);
            Integer enabled = (cfg == null || cfg.getEnabled() == null) ? 1 : cfg.getEnabled();
            Integer capacity = (cfg == null || cfg.getCapacity() == null || cfg.getCapacity() <= 0) ? 1 : cfg.getCapacity();
            Integer bookedCount = countBookedByBooking(serveId, date, slot);
            Integer remainingCount = Math.max(capacity - bookedCount, 0);

            BookingSlotConfigDto dto = new BookingSlotConfigDto();
            dto.setId(cfg == null ? null : cfg.getId());
            dto.setServeId(serveId);
            dto.setDate(date);
            dto.setTimeSlot(slot);
            dto.setEnabled(enabled);
            dto.setCapacity(capacity);
            dto.setBookedCount(bookedCount);
            dto.setRemainingCount(remainingCount);
            result.add(dto);
        }
        return result;
    }

    public boolean saveSlotConfig(BookingSlotConfigDto dto) {
        if (dto == null || dto.getServeId() == null || dto.getDate() == null || dto.getTimeSlot() == null) {
            return false;
        }
        BookingSlotConfig config = getSlotConfigOrNull(dto.getServeId(), dto.getDate(), dto.getTimeSlot());
        if (config == null) {
            config = new BookingSlotConfig();
            config.setServeId(dto.getServeId());
            config.setConfigDate(parseSqlDate(dto.getDate()));
            config.setTimeSlot(dto.getTimeSlot());
        }
        config.setEnabled((dto.getEnabled() != null && dto.getEnabled() == 0) ? 0 : 1);
        config.setCapacity(dto.getCapacity() == null || dto.getCapacity() <= 0 ? 1 : dto.getCapacity());
        if (config.getId() == null) {
            return bookingSlotConfigMapper.insertSlotConfig(
                    config.getServeId(),
                    config.getConfigDate(),
                    config.getTimeSlot(),
                    config.getEnabled(),
                    config.getCapacity()) > 0;
        }
        return bookingSlotConfigMapper.updateById(config) > 0;
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

    public List<BookingListDto> getVetBooking(Integer vetId) {
        if (vetId == null) {
            return java.util.Collections.emptyList();
        }
        List<BookingListDto> list = bookingMapper.getVetBooking(vetId);
        return list == null ? java.util.Collections.emptyList() : list;
    }

    public List<BookingListDto> searchBookingByPetName(String petName) {
        if (petName == null || petName.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return bookingMapper.searchBookingByPetName(petName.trim());
    }

    public List<BookingListDto> updateBookingComment(Booking booking) {
        Booking b = bookingMapper.selectById(booking.getBookingId());
        b.setAppraise(booking.getAppraise());
        b.setStar(booking.getStar());
        b.setFlag(3);
        bookingMapper.updateById(b);
        return getMyBooking(b.getUserId());
    }

    private BookingSlotConfig getSlotConfigOrNull(Integer serveId, String day, String slot) {
        if (serveId == null || day == null || day.trim().isEmpty() || slot == null || slot.trim().isEmpty()) {
            return null;
        }
        return bookingSlotConfigMapper.selectSlotConfig(serveId, day, slot);
    }

    private java.sql.Date parseSqlDate(String day) {
        return java.sql.Date.valueOf(day);
    }

    private int countBookedByBooking(Integer serveId, String day, String slot) {
        try {
            Date start = new SimpleDateFormat(AppConsts.dateTimeMinuteFormatPattern).parse(day + " " + slot);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.MINUTE, 1);
            Date end = calendar.getTime();
            QueryWrapper<Booking> wrapper = new QueryWrapper<>();
            wrapper.eq("serve", serveId)
                    .ge("bookingDate", start)
                    .lt("bookingDate", end)
                    .in("flag", Arrays.asList(1, 2));
            Integer count = Math.toIntExact(bookingMapper.selectCount(wrapper));
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.warn("countBookedByBooking failed, fallback to 0, serveId={}, day={}, slot={}", serveId, day, slot, e);
            return 0;
        }
    }

    private String buildSlotKey(Integer serveId, String day, String slot) {
        return String.valueOf(serveId) + "|" + String.valueOf(day) + "|" + String.valueOf(slot);
    }
}
