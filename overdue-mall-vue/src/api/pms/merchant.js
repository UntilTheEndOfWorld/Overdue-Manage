import request from '@/utils/request'

// 查询商户信息列表
export function listMerchant(query, pageReq) {
  return request({
    url: '/pms/merchant/list',
    method: 'get',
    params: {
      ...query,
      ...pageReq
    }
  })
}

// 查询商户信息详细
export function getMerchant(id) {
  return request({
    url: '/pms/merchant/' + id,
    method: 'get'
  })
}

// 新增商户信息
export function addMerchant(data) {
  return request({
    url: '/pms/merchant',
    method: 'post',
    data: data
  })
}

// 修改商户信息
export function updateMerchant(data) {
  return request({
    url: '/pms/merchant',
    method: 'put',
    data: data
  })
}

// 删除商户信息
export function delMerchant(ids) {
  return request({
    url: '/pms/merchant/' + ids,
    method: 'delete'
  })
}

// 导出商户信息
export function exportMerchant(query) {
  return request({
    url: '/pms/merchant/export',
    method: 'post',
    data: query
  })
}

// 更新商户状态
export function changeMerchantStatus(id, status) {
  return request({
    url: '/pms/merchant/changeStatus',
    method: 'put',
    data: {
      id: id,
      status: status
    }
  })
}
