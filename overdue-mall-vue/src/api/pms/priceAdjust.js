import request from '@/utils/request'

// 查询调价任务列表
export function listPriceAdjustTask(query) {
  return request({
    url: '/pms/priceAdjust/list',
    method: 'post',
    data: query
  })
}

// 查询调价任务详细
export function getPriceAdjustTask(id) {
  return request({
    url: '/pms/priceAdjust/' + id,
    method: 'get'
  })
}

// 批量调价
export function batchAdjustPrice(data) {
  return request({
    url: '/pms/priceAdjust/batch',
    method: 'post',
    data: data
  })
}

// 快速调价
export function quickAdjustPrice(data) {
  return request({
    url: '/pms/priceAdjust/quick',
    method: 'post',
    params: data
  })
}

