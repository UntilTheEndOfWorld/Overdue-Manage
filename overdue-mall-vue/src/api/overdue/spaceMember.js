import request from '@/utils/request'

// 查询空间成员列表
export function listSpaceMember(query) {
  return request({
    url: '/item/spaceMember/list',
    method: 'get',
    params: query
  })
}

// 查询空间成员详细
export function getSpaceMember(id) {
  return request({
    url: '/item/spaceMember/' + id,
    method: 'get'
  })
}

// 按空间ID查询成员列表
export function listBySpace(spaceId) {
  return request({
    url: '/item/spaceMember/space/' + spaceId,
    method: 'get'
  })
}

// 新增空间成员
export function addSpaceMember(data) {
  return request({
    url: '/item/spaceMember',
    method: 'post',
    data: data
  })
}

// 修改空间成员
export function updateSpaceMember(data) {
  return request({
    url: '/item/spaceMember',
    method: 'put',
    data: data
  })
}

// 移除空间成员（软删除）
export function leaveSpace(spaceId, userId) {
  return request({
    url: '/item/spaceMember/leave/' + spaceId + '/' + userId,
    method: 'delete'
  })
}

// 删除空间成员（物理删除）
export function delSpaceMember(id) {
  return request({
    url: '/item/spaceMember/' + id,
    method: 'delete'
  })
}
