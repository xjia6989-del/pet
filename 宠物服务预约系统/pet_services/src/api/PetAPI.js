import request from './request';

export const getPetList = (userId) => {
    return request({
        url: '/pet/list',
        method: 'get',
        params: { userId }
    }).then(res => res.data.result);   // 注意 res.data 是后端 JSON，result 是数组
};

export const addPet = (data, userId) => {
    return request({
        url: '/pet/add',
        method: 'post',
        params: { userId },
        data
    }).then(res => res.data.result);   // 添加成功返回宠物对象
};

export const updatePet = (data, userId) => {
    return request({
        url: '/pet/update',
        method: 'post',
        params: { userId },
        data
    }).then(res => res.data.result);   // 修改成功返回 true/null
};

export const deletePet = (petId, userId) => {
    return request({
        url: `/pet/delete/${petId}`,
        method: 'delete',
        params: { userId }
    }).then(res => res.data.result);   // 删除成功返回 true/null
};

export const getHealthReminders = (userId, mode = 'display') => {
    return request({
        url: '/pet/healthReminders',
        method: 'get',
        params: { userId, mode }
    }).then(res => res.data.result);
};

export const getAllPetsForAdmin = () => {
    return request({
        url: '/pet/all',
        method: 'get'
    }).then(res => res.data.result);
};

export const recognizePetVision = (formData) => {
    return request({
        url: '/pet/vision/recognize',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    }).then(res => res.data.result);
};

export const deletePetByAdmin = (petId, adminId) => {
    return request({
        url: `/pet/admin/delete/${petId}`,
        method: 'delete',
        params: { adminId }
    }).then(res => res.data.result);
};