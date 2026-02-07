import request from '@/utils/request'

// 查询操作日志列表
export function listOperationLog(query) {
  return request({
    url: '/item/log/list',
    method: 'get',
    params: query
  })
}

// 按空间ID查询操作日志
export function listBySpace(spaceId) {
  return request({
    url: '/item/log/space/' + spaceId,
    method: 'get'
  })
}

// 按物品ID查询操作日志
export function listByItem(itemId, itemType) {
  return request({
    url: '/item/log/item/' + itemId,
    method: 'get',
    params: { itemType }
  })
}
