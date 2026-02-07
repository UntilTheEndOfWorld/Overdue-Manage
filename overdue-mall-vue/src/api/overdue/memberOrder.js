import request from '@/utils/request'

// 查询会员订单列表
export function listMemberOrder(query) {
  return request({
    url: '/item/order/list',
    method: 'get',
    params: query
  })
}

// 查询会员订单详细
export function getMemberOrder(id) {
  return request({
    url: '/item/order/' + id,
    method: 'get'
  })
}

// 取消订单
export function cancelOrder(id) {
  return request({
    url: '/item/order/cancel/' + id,
    method: 'put'
  })
}
