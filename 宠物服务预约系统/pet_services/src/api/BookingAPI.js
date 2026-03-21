import request from './request';

export const getAllBooking = (currentPage, pageSize) => {
    return request({
        url: `/booking/getBookingList/${currentPage}/${pageSize}`,
        method: 'get'
    }).then(res => res.data.result);
};

export const updateBookingFlag = (bookingId, flag) => {
    return request({
        url: `/booking/updateBookingFlag/${bookingId}/${flag}`,
        method: 'get'
    }).then(res => res.data.result);
};

export const addBooking = (data) => {
    return request({
        url: '/booking/addBooking',
        method: 'post',
        data
    }).then(res => res.data.result);
};

export const deleteBooking = (bookingId) => {
    return request({
        url: `/booking/deleteBooking/${bookingId}`,
        method: 'get'
    }).then(res => res.data.result);
};

export const searchBooking = (flag) => {
    return request({
        url: `/booking/searchBooking/${flag}`,
        method: 'get'
    }).then(res => res.data.result);
};

export const getMyBooking = (userId) => {
    return request({
        url: `/booking/getMyBooking/${userId}`,
        method: 'get'
    }).then(res => res.data.result);
};

export const updateBookingComment = (data) => {
    return request({
        url: '/booking/updateBookingComment',
        method: 'post',
        data
    }).then(res => res.data.result);
};

export const getAvailableSlots = (serveId, date) => {
    return request({
        url: '/booking/availableSlots',
        method: 'get',
        params: { serveId, date }
    }).then(res => res.data.result);
};

export const cancelBooking = (bookingId, userId) => {
    return request({
        url: `/booking/cancel/${bookingId}`,
        method: 'post',
        params: { userId }
    }).then(res => res.data.result);
};