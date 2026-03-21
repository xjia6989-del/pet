import request from '@/api/request.js'

export const getAdminContactMessageList = (status) => {
  return request.get('/admin/contactMessage/list', { params: { status } }).then(res => res.data.result)
}

export const replyContactMessage = (messageId, reply) => {
  return request.post('/admin/contactMessage/reply/' + messageId, null, { params: { reply } }).then(res => res.data.result)
}

export const updateContactMessageStatus = (messageId, status) => {
  return request.post('/admin/contactMessage/status/' + messageId + '/' + status).then(res => res.data.result)
}
