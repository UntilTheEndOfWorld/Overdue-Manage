/**
 * API接口封装
 */
import { tokenManager } from './auth.js'

/**
 * 本地联调开关
 * - true：请求本机（默认端口与 overdue-admin application.yml 中 server.port 一致，当前为 8080）
 * - 微信开发者工具：可用 http://127.0.0.1:8080，并关闭域名校验（manifest 里 mp-weixin.setting.urlCheck 已为 false）
 * - 真机调试：127.0.0.1 指向手机自身，请改为电脑局域网 IP，例如 http://192.168.1.100:8080
 * - 上线前改为 false 或走正式域名
 */
var USE_LOCAL_API = true
var LOCAL_BASE_URL = 'http://127.0.0.1:8080'
var PROD_BASE_URL = 'https://www.lycc.ltd/jx_slr_api'

// 基础配置 - 根据环境动态选择 API 地址
var getBaseUrl = function() {
  if (USE_LOCAL_API) {
    return LOCAL_BASE_URL
  }
  // H5：浏览器访问本机时用本地（可选）
  if (typeof window !== 'undefined') {
    var hostname = window.location.hostname
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return LOCAL_BASE_URL
    }
  }
  return PROD_BASE_URL
}

var BASE_URL = getBaseUrl()

// 请求方法封装
var request = function(url, options) {
  options = options || {}
  return new Promise(function(resolve, reject) {
    var token = tokenManager.getToken()
    var userInfo = uni.getStorageSync('userInfo')
    
    if (typeof userInfo === 'string' && userInfo.trim() !== '') {
      try {
        userInfo = JSON.parse(userInfo)
      } catch (error) {
        console.warn('[API] 解析用户信息失败:', error)
        userInfo = null
      }
    } else if (typeof userInfo === 'string' && userInfo.trim() === '') {
      userInfo = null
    }
    
    if (!token && userInfo && userInfo.token) {
      token = userInfo.token
      console.log('[API] 使用用户信息中的token:', token)
      tokenManager.setToken(token)
    }
    
    var headers = {
      'Content-Type': 'application/json'
    }
    if (options.header) {
      Object.keys(options.header).forEach(function(key) {
        headers[key] = options.header[key]
      })
    }
    
    if (token) {
      headers['Authorization'] = 'Bearer ' + token
    }
    
    if (userInfo && (userInfo.id || userInfo.userId || userInfo.memberId)) {
      var userId = userInfo.id || userInfo.userId || userInfo.memberId
      headers['X-User-Id'] = userId.toString()
      console.log('[API] 添加用户ID到请求头:', userId)
    }
    
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      success: function(res) {
        console.log('[API] 请求响应:', {
          url: url,
          statusCode: res.statusCode,
          data: res.data
        })
        
        if (res.statusCode === 200) {
          if (res.data && res.data.code !== undefined) {
            if (res.data.code === 200) {
              resolve(res.data)
            } else if (res.data.code === 401) {
              console.warn('[API] 业务错误码401:', res.data.msg || res.data)
              tokenManager.clearToken()
              uni.removeStorageSync('userInfo')
              var msg401 = res.data.msg
              if (!msg401 || String(msg401).trim() === '') {
                msg401 = '登录已过期，请重新登录'
              }
              uni.showToast({
                title: msg401,
                icon: 'none'
              })
              // 跳转到登录页
              setTimeout(function() {
                uni.reLaunch({ url: '/pages/auth/login' })
              }, 1500)
              reject(new Error('Token无效'))
            } else {
              console.error('[API] 业务错误:', res.data)
              reject(new Error(res.data.msg || '操作失败'))
            }
          } else {
            resolve(res.data)
          }
        } else if (res.statusCode === 401) {
          console.warn('[API] Token无效，清除本地数据')
          tokenManager.clearToken()
          uni.removeStorageSync('userInfo')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          setTimeout(function() {
            uni.reLaunch({ url: '/pages/auth/login' })
          }, 1500)
          reject(new Error('Token无效'))
        } else {
          reject(new Error('请求失败: ' + res.statusCode))
        }
      },
      fail: function(err) {
        console.error('[API] 请求失败:', err)
        reject(err)
      }
    })
  })
}

/** 上传图片到服务端，返回可访问 URL */
var uploadImageFile = function(filePath) {
  return new Promise(function(resolve, reject) {
    var token = tokenManager.getToken()
    var userInfo = uni.getStorageSync('userInfo')
    if (typeof userInfo === 'string' && userInfo.trim() !== '') {
      try {
        userInfo = JSON.parse(userInfo)
      } catch (e) {
        userInfo = null
      }
    }
    var headers = {}
    if (token) {
      headers['Authorization'] = 'Bearer ' + token
    }
    if (userInfo && (userInfo.id || userInfo.userId || userInfo.memberId)) {
      headers['X-User-Id'] = String(userInfo.id || userInfo.userId || userInfo.memberId)
    }
    uni.uploadFile({
      url: BASE_URL + '/h5/file/upload-image',
      filePath: filePath,
      name: 'file',
      header: headers,
      success: function(res) {
        try {
          var body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          if (body.code === 200 && body.data && body.data.url) {
            resolve(body.data.url)
          } else {
            reject(new Error(body.msg || '上传失败'))
          }
        } catch (err) {
          reject(err)
        }
      },
      fail: function(err) {
        reject(err)
      }
    })
  })
}

// 用户相关API
var userAPI = {
  // 用户登录
  login: function(data) {
    return request('/auth/login', {
      method: 'POST',
      data: data
    })
  },
  
  // 微信用户信息登录
  wechatUserInfoLogin: function(data) {
    console.log('[API] 微信用户信息登录，参数:', data)
    return request('/no-auth/wechat/userinfo-login', {
      method: 'POST',
      data: {
        code: data.code,
        nickname: data.nickname || data.nickName,
        avatarUrl: data.avatarUrl || data.avatar,
        gender: data.gender,
        country: data.country,
        province: data.province,
        city: data.city
      },
      header: {
        'Content-Type': 'application/json'
      }
    })
  },
  
  // 微信登录后获取手机号并注册会员
  wechatRegisterWithPhone: function(data) {
    console.log('[API] 微信注册手机号，参数:', data)
    return request('/no-auth/wechat/register-with-phone', {
      method: 'POST',
      data: data,
      header: {
        'Content-Type': 'application/json'
      }
    })
  },
  
  // 获取微信手机号（解密手机号）
  getWechatPhoneNumber: function(data) {
    console.log('[API] 获取微信手机号，参数:', data)
    return request('/no-auth/wechat/get-phone', {
      method: 'POST',
      data: {
        code: data.code,
        encryptedData: data.encryptedData,
        iv: data.iv
      },
      header: {
        'Content-Type': 'application/json'
      }
    })
  },
  
  // 发送短信验证码
  sendSmsCode: function(data) {
    return request('/no-auth/sms/sendAliyun/' + btoa(data.phone), {
      method: 'GET'
    })
  },
  
  // 验证短信验证码
  verifySmsCode: function(data) {
    return request('/h5/sms/login', {
      method: 'POST',
      data: {
        phone: data.phone,
        code: data.code,
        type: data.type
      }
    })
  },
  
  // 短信登录
  smsLogin: function(data) {
    return request('/h5/sms/login', {
      method: 'POST',
      data: {
        phone: data.phone,
        code: data.code
      }
    })
  },
  
  // 获取用户信息
  getUserInfo: function() {
    return request('/auth/user-info')
  }
}

// 统一API类
function Api() {}

/**
 * 通用请求
 */
Api.prototype.request = function(url, method, data) {
  method = method || 'GET'
  data = data || {}
  return request(url, { method: method, data: data })
}

/**
 * 获取Token
 */
Api.prototype.getToken = function() {
  return tokenManager.getToken()
}

// ==================== 用户相关接口 ====================
Api.prototype.login = function(code) {
  return userAPI.login({ code: code })
}

Api.prototype.getUserInfo = function() {
  return userAPI.getUserInfo()
}

// 获取用户资料
Api.prototype.getUserProfile = function() {
  return this.request('/h5/ucenter/user-profile', 'GET')
}

// 更新用户资料
Api.prototype.updateUserProfile = function(data) {
  return this.request('/h5/ucenter/update-profile', 'PUT', data)
}

// 获取会员信息
Api.prototype.getMemberInfo = function() {
  return this.request('/h5/member/info', 'GET')
}

// ==================== 个人物品相关接口 ====================
Api.prototype.getPersonalItems = function() {
  return this.request('/personal/items', 'GET')
}

Api.prototype.getPersonalItemStats = function() {
  return this.request('/personal/items/stats', 'GET')
}

Api.prototype.addPersonalItem = function(item) {
  return this.request('/personal/items', 'POST', item)
}

Api.prototype.getPersonalItemDetail = function(id) {
  return this.request('/personal/items/' + id, 'GET')
}

Api.prototype.updatePersonalItem = function(id, item) {
  return this.request('/personal/items/' + id, 'PUT', item)
}

Api.prototype.deletePersonalItem = function(id, opType) {
  var url = '/personal/items/' + id
  if (opType) {
    url += '?opType=' + encodeURIComponent(opType)
  }
  return this.request(url, 'DELETE')
}

Api.prototype.getPersonalLogs = function(params) {
  params = params || {}
  return this.request('/personal/logs', 'GET', params)
}

// ==================== 共享空间相关接口 ====================
Api.prototype.getSharedSpaces = function() {
  return this.request('/shared/spaces', 'GET')
}

Api.prototype.createSharedSpace = function(space) {
  return this.request('/shared/spaces', 'POST', space)
}

Api.prototype.getSharedSpaceDetail = function(id) {
  return this.request('/shared/spaces/' + id, 'GET')
}

Api.prototype.updateSharedSpace = function(id, space) {
  return this.request('/shared/spaces/' + id, 'PUT', space)
}

// 生成邀请码
Api.prototype.generateInviteCode = function(spaceId) {
  return this.request('/shared/spaces/' + spaceId + '/invite', 'POST')
}

// 加入共享空间
Api.prototype.joinSpace = function(inviteCode) {
  return this.request('/shared/spaces/join', 'POST', { inviteCode: inviteCode })
}

// 获取邀请信息（无需登录）
Api.prototype.getInviteInfo = function(inviteCode) {
  return this.request('/shared/no-auth/invite-info?inviteCode=' + inviteCode, 'GET')
}

// 检查是否是空间成员
Api.prototype.checkSpaceMember = function(spaceId) {
  return this.request('/shared/spaces/' + spaceId + '/check-member', 'GET')
}

// 离开空间
Api.prototype.leaveSpace = function(spaceId) {
  return this.request('/shared/spaces/' + spaceId + '/leave', 'DELETE')
}

// 获取空间成员列表
Api.prototype.getSpaceMembers = function(spaceId) {
  return this.request('/shared/spaces/' + spaceId + '/members', 'GET')
}

// ==================== 共享物品相关接口 ====================
Api.prototype.getSharedItems = function(spaceId) {
  return this.request('/shared/spaces/' + spaceId + '/items', 'GET')
}

Api.prototype.addSharedItem = function(spaceId, item) {
  return this.request('/shared/spaces/' + spaceId + '/items', 'POST', item)
}

Api.prototype.getSharedItemDetail = function(spaceId, itemId) {
  return this.request('/shared/spaces/' + spaceId + '/items/' + itemId, 'GET')
}

Api.prototype.updateSharedItem = function(spaceId, itemId, item) {
  return this.request('/shared/spaces/' + spaceId + '/items/' + itemId, 'PUT', item)
}

Api.prototype.deleteSharedItem = function(spaceId, itemId, opType) {
  var url = '/shared/spaces/' + spaceId + '/items/' + itemId
  if (opType) {
    url += '?opType=' + encodeURIComponent(opType)
  }
  return this.request(url, 'DELETE')
}

// 获取空间操作日志
Api.prototype.getSpaceLogs = function(spaceId, params) {
  params = params || {}
  return this.request('/shared/spaces/' + spaceId + '/logs', 'GET', params)
}

// ==================== 个人资料 ====================
Api.prototype.getProfile = function() {
  var self = this
  return this.request('/personal/profile', 'GET').catch(function(err) {
    // 兼容旧后端：个人资料接口可能仍为 /h5/ucenter/user-profile
    var message = (err && err.message) ? String(err.message) : ''
    if (message.indexOf('404') !== -1) {
      return self.request('/h5/ucenter/user-profile', 'GET')
    }
    throw err
  })
}

Api.prototype.updateProfile = function(data) {
  var self = this
  return this.request('/personal/profile', 'PUT', data).catch(function(err) {
    // 兼容旧后端：更新资料接口回退到历史路径
    var message = (err && err.message) ? String(err.message) : ''
    if (message.indexOf('404') !== -1) {
      return self.request('/h5/ucenter/update-profile', 'PUT', data)
    }
    throw err
  })
}

Api.prototype.uploadImage = function(filePath) {
  return uploadImageFile(filePath)
}

// ==================== 到期提醒设置 ====================
Api.prototype.getReminderSettings = function() {
  return this.request('/personal/reminder-settings', 'GET')
}

Api.prototype.updateReminderSettings = function(data) {
  return this.request('/personal/reminder-settings', 'PUT', data)
}

// ==================== 日历相关接口 ====================
/**
 * 获取按日期分组的物品列表
 * @param {Object} params - 查询参数
 * @param {String} params.type - 类型：all/personal/shared
 * @param {String} params.startDate - 开始日期 YYYY-MM-DD
 * @param {String} params.endDate - 结束日期 YYYY-MM-DD
 */
Api.prototype.getItemsByDate = function(params) {
  return this.request('/item/calendar/items', 'GET', params)
}

/**
 * 获取指定日期的物品列表
 * @param {String} date - 日期 YYYY-MM-DD
 * @param {String} type - 类型：all/personal/shared
 */
Api.prototype.getItemsBySpecificDate = function(date, type) {
  type = type || 'all'
  return this.request('/item/calendar/items', 'GET', { date: date, type: type })
}

// ==================== 应用配置 ====================
Api.prototype.getAppConfig = function() {
  return this.request('/no-auth/overdue/config', 'GET')
}

// ==================== 会员订单相关接口 ====================
// 获取会员套餐列表
Api.prototype.getMemberPlans = function() {
  return this.request('/member/plans', 'GET')
}

// 创建会员订单
Api.prototype.createMemberOrder = function(data) {
  return this.request('/member/orders', 'POST', data)
}

// 获取用户订单列表
Api.prototype.getMemberOrders = function() {
  return this.request('/member/orders', 'GET')
}

// 获取订单详情
Api.prototype.getMemberOrderDetail = function(id) {
  return this.request('/member/orders/' + id, 'GET')
}

// 取消订单
Api.prototype.cancelMemberOrder = function(id) {
  return this.request('/member/orders/' + id + '/cancel', 'PUT')
}

// ==================== 操作日志相关接口 ====================
Api.prototype.getOperationLogs = function(spaceId, params) {
  params = params || {}
  return this.request('/shared/spaces/' + spaceId + '/logs', 'GET', params)
}

// 导出用户API
export { userAPI }

export default new Api()
