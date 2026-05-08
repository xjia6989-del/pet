import request from './request';

export const getHealthRecords = (petId, userId) => {
    return request({
        url: `/health/list/${petId}`,
        method: 'get',
        params: { userId }
    }).then(res => res.data);
};


export const addHealthRecord = (data) => {
    return request({
        url: '/health/add',
        method: 'post',
        data
    }).then(res => res.data.result || res.data);
};

export const updateHealthRecord = (data) => {
    return request({
        url: '/health/update',
        method: 'post',
        data
    }).then(res => res.data.result ?? res.data);
};

export const deleteHealthRecord = (recordId) => {
    return request({
        url: `/health/delete/${recordId}`,
        method: 'delete'
    }).then(res => res.data.result ?? res.data);
};

export const getHealthRecordsForAdmin = (petId) => {
    return request({
        url: `/health/admin/list/${petId}`,
        method: 'get'
    }).then(res => res.data.result || res.data);
};

export const getHealthRecordsForVet = (petId) => {
    return request({
        url: `/health/vet/list/${petId}`,
        method: 'get'
    }).then(res => res.data.result || res.data);
};