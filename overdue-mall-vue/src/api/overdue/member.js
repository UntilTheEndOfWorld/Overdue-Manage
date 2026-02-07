import request from '@/utils/request'

// 查询会员信息列表
export function listMember(query) {
  return request({
    url: '/item/member/list',
    method: 'get',
    params: query
  })
}

// 查询会员详细
export function getMember(id) {
  return request({
    url: '/item/member/' + id,
    method: 'get'
  })
}

// 新增会员信息
export function addMember(data) {
  return request({
    url: '/item/member',
    method: 'post',
    data: data
  })
}

// 修改会员信息
export function updateMember(data) {
  return request({
    url: '/item/member',
    method: 'put',
    data: data
  })
}
