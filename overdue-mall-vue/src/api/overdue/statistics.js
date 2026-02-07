import request from '@/utils/request'

// 获取总览统计数据
export function getOverview() {
  return request({
    url: '/item/statistics/overview',
    method: 'get'
  })
}

// 获取用户增长趋势
export function getUserTrend(startDate, endDate) {
  return request({
    url: '/item/statistics/user-trend',
    method: 'get',
    params: { startDate, endDate }
  })
}

// 获取物品过期统计
export function getExpiryStats() {
  return request({
    url: '/item/statistics/expiry-stats',
    method: 'get'
  })
}

// 获取订单统计
export function getOrderStats() {
  return request({
    url: '/item/statistics/order-stats',
    method: 'get'
  })
}

// 获取会员统计
export function getMemberStats() {
  return request({
    url: '/item/statistics/member-stats',
    method: 'get'
  })
}
