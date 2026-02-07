import request from '@/utils/request'

// 查询共享物品列表
export function listSharedItem(query) {
  return request({
    url: '/item/sharedItem/list',
    method: 'get',
    params: query
  })
}

// 查询共享物品详细
export function getSharedItem(id) {
  return request({
    url: '/item/sharedItem/' + id,
    method: 'get'
  })
}

// 新增共享物品
export function addSharedItem(data) {
  return request({
    url: '/item/sharedItem',
    method: 'post',
    data: data
  })
}

// 修改共享物品
export function updateSharedItem(data) {
  return request({
    url: '/item/sharedItem',
    method: 'put',
    data: data
  })
}

// 删除共享物品
export function delSharedItem(id) {
  return request({
    url: '/item/sharedItem/' + id,
    method: 'delete'
  })
}

// 按空间ID查询物品列表
export function listBySpace(spaceId) {
  return request({
    url: '/item/sharedItem/space/' + spaceId,
    method: 'get'
  })
}

// 查询即将过期物品
export function listExpiringItems(spaceId) {
  return request({
    url: '/item/sharedItem/expiring/' + spaceId,
    method: 'get'
  })
}

// 查询已过期物品
export function listExpiredItems(spaceId) {
  return request({
    url: '/item/sharedItem/expired/' + spaceId,
    method: 'get'
  })
}
