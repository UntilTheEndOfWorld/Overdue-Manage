/**
 * API接口封装
 */
import { tokenManager } from './auth.js'

// 基础配置 - 根据环境动态选择API地址
const getBaseUrl = () => {
  // 判断环境
  if (typeof window !== 'undefined') {
    // 浏览器环境
    const hostname = window.location.hostname
    const protocol = window.location.protocol
    
    // 本地开发环境判断
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return 'https://www.lycc.ltd/jx_slr_api'  // 生产环境
    } else {
      return 'https://www.lycc.ltd/jx_slr_api'  // 生产环境
    }
  } else {
    // Node.js环境或uni-app环境，默认使用生产环境配置
    return 'https://www.lycc.ltd/jx_slr_api'  // 生产环境
  }
}

const BASE_URL = getBaseUrl() // 后端服务地址，根据实际情况修改

// 请求方法封装
const request = (url, options = {}) => {
  return new Promise((resolve, reject) => {
    // 使用统一的token管理器
    let token = tokenManager.getToken()
    let userInfo = uni.getStorageSync('userInfo')
    
    // 如果userInfo是字符串，尝试解析
    if (typeof userInfo === 'string' && userInfo.trim() !== '') {
      try {
        userInfo = JSON.parse(userInfo)
      } catch (error) {
        console.warn('[API] 解析用户信息失败:', error)
        userInfo = null
      }
    } else if (typeof userInfo === 'string' && userInfo.trim() === '') {
      // 空字符串，设置为null
      userInfo = null
    }
    
    // 如果token不存在但userInfo中有token，使用userInfo中的token
    if (!token && userInfo && userInfo.token) {
      token = userInfo.token
      console.log('[API] 使用用户信息中的token:', token)
      // 使用统一的token管理器设置token
      tokenManager.setToken(token)
    }
    
    // 构建请求头
    const headers = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    // 如果有token，添加到请求头
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    
    // 如果有用户信息，添加用户ID到请求头
    if (userInfo && (userInfo.id || userInfo.userId || userInfo.memberId)) {
      const userId = userInfo.id || userInfo.userId || userInfo.memberId
      headers['X-User-Id'] = userId.toString()
      console.log('[API] 添加用户ID到请求头:', userId)
    }
    
    uni.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      success: (res) => {
        console.log('[API] 请求响应:', {
          url: url,
          statusCode: res.statusCode,
          data: res.data
        })
        
        if (res.statusCode === 200) {
          // 检查返回数据中的code字段
          if (res.data && res.data.code !== undefined) {
            if (res.data.code === 200) {
              resolve(res.data)
            } else if (res.data.code === 401) {
              // 业务错误码401，表示token过期或获取用户ID异常
              console.warn('[API] 业务错误码401，Token无效或获取用户ID异常，清除本地数据')
              tokenManager.clearToken()
              uni.removeStorageSync('userInfo')
              uni.showToast({
                title: '登录已过期，请重新登录',
                icon: 'none'
              })
              reject(new Error('Token无效'))
            } else {
              // 其他业务错误，返回错误信息
              console.error('[API] 业务错误:', res.data)
              reject(new Error(res.data.msg || '操作失败'))
            }
          } else {
            // 没有code字段，直接返回数据
            resolve(res.data)
          }
        } else if (res.statusCode === 401) {
          // token过期或无效，清除本地token并跳转到登录页
          console.warn('[API] Token无效，清除本地数据')
          tokenManager.clearToken()
          uni.removeStorageSync('userInfo')
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          reject(new Error('Token无效'))
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        console.error('[API] 请求失败:', err)
        reject(err)
      }
    })
  })
}

// 用户相关API
const userAPI = {
  // 用户登录
  login(data) {
    return request('/auth/login', {
      method: 'POST',
      data
    })
  },
  
  // 微信用户信息登录
  wechatUserInfoLogin(data) {
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
  wechatRegisterWithPhone(data) {
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
  getWechatPhoneNumber(data) {
    console.log('[API] 获取微信手机号，参数:', data)
    
    const requestData = {
      code: data.code,
      encryptedData: data.encryptedData,
      iv: data.iv
    }
    
    return request('/no-auth/wechat/get-phone', {
      method: 'POST',
      data: requestData,
      header: {
        'Content-Type': 'application/json'
      }
    })
  },
  
  // 发送短信验证码
  sendSmsCode(data) {
    return request('/no-auth/sms/sendAliyun/' + btoa(data.phone), {
      method: 'GET'
    })
  },
  
  // 验证短信验证码
  verifySmsCode(data) {
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
  smsLogin(data) {
    return request('/h5/sms/login', {
      method: 'POST',
      data: {
        phone: data.phone,
        code: data.code
      }
    })
  },
  
  // 获取用户信息
  getUserInfo() {
    return request('/auth/user-info')
  }
}

class Api {
  /**
   * 请求封装
   */
  request(url, method = 'GET', data = {}) {
    return request(url, { method, data })
  }

  /**
   * 获取Token
   */
  getToken() {
    return tokenManager.getToken()
  }

  // 用户相关接口
  login(code) {
    return userAPI.login({ code })
  }

  getUserInfo() {
    return userAPI.getUserInfo()
  }

  // 个人物品相关接口
  getPersonalItems() {
    return this.request('/personal/items', 'GET')
  }

  addPersonalItem(item) {
    return this.request('/personal/items', 'POST', item)
  }

  updatePersonalItem(id, item) {
    return this.request(`/personal/items/${id}`, 'PUT', item)
  }

  deletePersonalItem(id) {
    return this.request(`/personal/items/${id}`, 'DELETE')
  }

  // 共享空间相关接口
  getSharedSpaces() {
    return this.request('/shared/spaces', 'GET')
  }

  createSharedSpace(space) {
    return this.request('/shared/spaces', 'POST', space)
  }

  getSharedSpaceDetail(id) {
    return this.request(`/shared/spaces/${id}`, 'GET')
  }

  getSharedItems(spaceId) {
    return this.request(`/shared/spaces/${spaceId}/items`, 'GET')
  }

  addSharedItem(spaceId, item) {
    return this.request(`/shared/spaces/${spaceId}/items`, 'POST', item)
  }

  updateSharedItem(spaceId, itemId, item) {
    return this.request(`/shared/spaces/${spaceId}/items/${itemId}`, 'PUT', item)
  }

  deleteSharedItem(spaceId, itemId) {
    return this.request(`/shared/spaces/${spaceId}/items/${itemId}`, 'DELETE')
  }

  // 日历相关接口
  /**
   * 获取按日期分组的物品列表
   * @param {Object} params - 查询参数
   * @param {String} params.type - 类型：all/personal/shared
   * @param {String} params.startDate - 开始日期 YYYY-MM-DD
   * @param {String} params.endDate - 结束日期 YYYY-MM-DD
   */
  getItemsByDate(params) {
    return this.request('/item/calendar/items', 'GET', params)
  }

  /**
   * 获取指定日期的物品列表
   * @param {String} date - 日期 YYYY-MM-DD
   * @param {String} type - 类型：all/personal/shared
   */
  getItemsBySpecificDate(date, type = 'all') {
    return this.request('/item/calendar/items', 'GET', { date, type })
  }

  // 操作日志相关接口
  getOperationLogs(spaceId, params = {}) {
    return this.request(`/shared/spaces/${spaceId}/logs`, 'GET', params)
  }

  // 邀请相关接口
  generateInviteCode(spaceId) {
    return this.request(`/shared/spaces/${spaceId}/invite`, 'POST')
  }

  joinSpace(inviteCode) {
    return this.request('/shared/spaces/join', 'POST', { inviteCode })
  }
}

// 导出用户API
export { userAPI }

export default new Api()
