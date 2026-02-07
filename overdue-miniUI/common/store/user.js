/**
 * 用户状态管理
 */
import storage from '../utils/storage.js'

const state = {
  userInfo: null,
  isLoggedIn: false
}

const mutations = {
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    state.isLoggedIn = !!userInfo
    if (userInfo) {
      storage.set('userInfo', userInfo)
    } else {
      storage.remove('userInfo')
    }
  }
}

const actions = {
  // 微信登录
  async wxLogin({ commit }) {
    return new Promise((resolve, reject) => {
      uni.login({
        provider: 'weixin',
        success: async (res) => {
          try {
            // 获取用户信息
            const userInfoRes = await uni.getUserProfile({
              desc: '用于完善用户资料'
            })
            
            // 这里应该调用后端API进行登录
            // const loginRes = await api.login(res.code)
            
            // 模拟登录成功
            const userInfo = {
              openid: res.code, // 实际应该是后端返回的openid
              nickName: userInfoRes.userInfo.nickName,
              avatarUrl: userInfoRes.userInfo.avatarUrl,
              token: 'mock_token_' + Date.now()
            }
            
            commit('SET_USER_INFO', userInfo)
            resolve(userInfo)
          } catch (err) {
            console.error('登录失败:', err)
            reject(err)
          }
        },
        fail: (err) => {
          console.error('获取登录凭证失败:', err)
          reject(err)
        }
      })
    })
  },

  // 退出登录
  logout({ commit }) {
    commit('SET_USER_INFO', null)
    uni.reLaunch({
      url: '/pages/index/index'
    })
  },

  // 初始化用户信息
  initUserInfo({ commit }) {
    const userInfo = storage.get('userInfo')
    if (userInfo) {
      commit('SET_USER_INFO', userInfo)
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
