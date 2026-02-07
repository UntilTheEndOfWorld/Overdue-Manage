import request from '@/utils/request'

// 查询价格历史记录列表
export function listPriceHistory(query) {
  return request({
    url: '/pms/priceHistory/list',
    method: 'post',
    data: query
  })
}

// 查询价格历史记录详细
export function getPriceHistory(id) {
  return request({
    url: '/pms/priceHistory/' + id,
    method: 'get'
  })
}

// 根据商品ID查询价格历史
export function getPriceHistoryByProduct(productId) {
  return request({
    url: '/pms/priceHistory/product/' + productId,
    method: 'get'
  })
}

// 根据SKU ID查询价格历史
export function getPriceHistoryBySku(skuId) {
  return request({
    url: '/pms/priceHistory/sku/' + skuId,
    method: 'get'
  })
}

