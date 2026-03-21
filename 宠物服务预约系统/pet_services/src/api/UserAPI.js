import request from '@/api/request.js'

export const userLogin = function (username, password) {
  return request.get('/user/login/' + username + '/' + password, {
  })
}

//获取所有用户信息
export const getAllUser = function (currentPage, pageSize) {
  return request.get('/user/getUserList/' + currentPage + '/' + pageSize, {
  })
}

//添加用户信息
export const addUser = function (userInfo) {
  return request({
    url: '/user/addUser',
    method: 'POST',
    data: userInfo,
    header: { 'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVCFSAonTuDbVCoAN' }
  })
}

//修改用户信息
export const updateUser = function (userInfo) {
  return request({
    url: '/user/updateUser',
    method: 'POST',
    data: userInfo,
    header: { 'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVCFSAonTuDbVCoAN' }
  })
}

//删除用户
export const deleteUser = function (userId) {
  return request.get('/user/deleteUser/' + userId, {
  })
}

export const searchUser = function (name, phone) {
  return request.post('/user/searchUser', {
    name, phone
  })
}

export const resetPassword = function (userId) {
  return request.get('/user/resetPassword/' + userId, {
  })
}

export const updateMyInfo = function (myInfo) {
  return request({
    url: '/user/updateMyInfo',
    method: 'POST',
    data: myInfo,
    header: { 'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundaryVCFSAonTuDbVCoAN' }
  })
}

export const register = function (myInfo) {
  return request({
    url: '/user/register',
    method: 'POST',
    data: myInfo,
    headers: { 'Content-Type': 'application/json' }
  })
}

export const getHealthReminderSubscribed = function (userId) {
  return request.get('/user/healthReminderSubscribed/' + userId).then(res => res.data.result)
}

export const updateHealthReminderSubscribed = function (userId, subscribed) {
  return request.post('/user/healthReminderSubscribed/' + userId + '/' + subscribed).then(res => res.data.result)
}

export const getHealthReminderMessages = function (userId, status) {
  return request.get('/user/healthReminderMessages/' + userId, { params: { status } }).then(res => res.data.result)
}

export const triggerHealthReminderDispatch = function (userId) {
  return request.post('/user/healthReminderDispatch/' + userId).then(res => res.data.result)
}

export const resendHealthReminderMessage = function (messageId) {
  return request.post('/user/healthReminderMessages/resend/' + messageId).then(res => res.data.result)
}

export const submitContactMessage = function (data) {
  return request.post('/user/contactMessage/submit', data).then(res => res.data.result)
}

export const listMyContactMessages = function (userId) {
  return request.get('/user/contactMessage/list/' + userId).then(res => res.data.result)
}