import request from '@/utils/request'

// 查询广告观看记录列表
export function listAdWatchRecord(query) {
  return request({
    url: '/item/adWatch/list',
    method: 'get',
    params: query
  })
}

// 查询广告观看记录详细
export function getAdWatchRecord(id) {
  return request({
    url: '/item/adWatch/' + id,
    method: 'get'
  })
}

// 按用户ID查询广告观看记录
export function getByUserId(userId) {
  return request({
    url: '/item/adWatch/user/' + userId,
    method: 'get'
  })
}

// 新增广告观看记录
export function addAdWatchRecord(data) {
  return request({
    url: '/item/adWatch',
    method: 'post',
    data: data
  })
}

// 修改广告观看记录
export function updateAdWatchRecord(data) {
  return request({
    url: '/item/adWatch',
    method: 'put',
    data: data
  })
}

// 删除广告观看记录
export function delAdWatchRecord(id) {
  return request({
    url: '/item/adWatch/' + id,
    method: 'delete'
  })
}

// 获取统计信息
export function getStatistics() {
  return request({
    url: '/item/adWatch/statistics',
    method: 'get'
  })
}

// 按日期统计
export function getStatsByDate(startDate, endDate) {
  return request({
    url: '/item/adWatch/stats/date',
    method: 'get',
    params: { startDate, endDate }
  })
}

// 查询用户剩余额度
export function getRemainingQuota(userId) {
  return request({
    url: '/item/adWatch/quota/' + userId,
    method: 'get'
  })
}
