import request from '@/utils/request'

// 查询个人物品列表
export function listPersonalItem(query) {
  return request({
    url: '/item/personal/list',
    method: 'get',
    params: query
  })
}

// 查询个人物品详细
export function getPersonalItem(id) {
  return request({
    url: '/item/personal/' + id,
    method: 'get'
  })
}

// 新增个人物品
export function addPersonalItem(data) {
  return request({
    url: '/item/personal',
    method: 'post',
    data: data
  })
}

// 修改个人物品
export function updatePersonalItem(data) {
  return request({
    url: '/item/personal',
    method: 'put',
    data: data
  })
}

// 删除个人物品
export function delPersonalItem(id) {
  return request({
    url: '/item/personal/' + id,
    method: 'delete'
  })
}

// 查询即将过期物品
export function listExpiringItems(userId) {
  return request({
    url: '/item/personal/expiring/' + userId,
    method: 'get'
  })
}

// 查询已过期物品
export function listExpiredItems(userId) {
  return request({
    url: '/item/personal/expired/' + userId,
    method: 'get'
  })
}
