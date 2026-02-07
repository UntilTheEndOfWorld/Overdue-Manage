import request from '@/utils/request'

// 查询共享空间列表
export function listSharedSpace(query) {
  return request({
    url: '/item/space/list',
    method: 'get',
    params: query
  })
}

// 查询共享空间详细
export function getSharedSpace(id) {
  return request({
    url: '/item/space/' + id,
    method: 'get'
  })
}

// 新增共享空间
export function addSharedSpace(data) {
  return request({
    url: '/item/space',
    method: 'post',
    data: data
  })
}

// 修改共享空间
export function updateSharedSpace(data) {
  return request({
    url: '/item/space',
    method: 'put',
    data: data
  })
}

// 删除共享空间
export function delSharedSpace(id) {
  return request({
    url: '/item/space/' + id,
    method: 'delete'
  })
}
