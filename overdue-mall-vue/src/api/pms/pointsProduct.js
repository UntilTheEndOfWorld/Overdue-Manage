import request from '@/utils/request'

// 查询积分商品信息列表
export function listPointsProduct(query) {
  return request({
    url: '/pms/points-product/list',
    method: 'post',
    data: query
  })
}

// 查询积分商品信息详细
export function getPointsProduct(id) {
  return request({
    url: '/pms/points-product/' + id,
    method: 'get'
  })
}

// 新增积分商品信息
export function addPointsProduct(data) {
  return request({
    url: '/pms/points-product',
    method: 'post',
    data: data
  })
}

// 修改积分商品信息
export function updatePointsProduct(data) {
  return request({
    url: '/pms/points-product',
    method: 'put',
    data: data
  })
}

// 删除积分商品信息
export function delPointsProduct(id) {
  return request({
    url: '/pms/points-product/' + id,
    method: 'delete'
  })
}

// 批量删除积分商品信息
export function delPointsProductBatch(ids) {
  return request({
    url: '/pms/points-product/batch',
    method: 'delete',
    data: ids
  })
}

// 导出积分商品信息
export function exportPointsProduct(query) {
  return request({
    url: '/pms/points-product/export',
    method: 'get',
    params: query
  })
}

// 查询轮播图积分商品列表
export function getBannerPointsProducts() {
  return request({
    url: '/pms/points-product/banner',
    method: 'get'
  })
}
