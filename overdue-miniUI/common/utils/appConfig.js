/**
 * 应用配置工具：从后端 sys_config 拉取是否收费、免费额度等
 */
import storage from './storage.js'
import api from './api.js'
import memberUtil from './member.js'

const CACHE_KEY = 'appConfig'

const DEFAULT_CONFIG = {
  chargeEnabled: false,
  freePersonalItemLimit: 10,
  freeSharedSpaceLimit: 1,
  freeSharedItemLimit: 10
}

class AppConfigUtil {
  /**
   * 从后端加载配置并同步到 memberUtil
   * @param {boolean} force 保留参数兼容；始终请求服务端以获取 sys_config 最新值
   */
  async loadConfig(force) {
    try {
      var res = await api.getAppConfig()
      if (res && res.code === 200 && res.data) {
        var config = Object.assign({}, DEFAULT_CONFIG, res.data)
        storage.set(CACHE_KEY, config)
        memberUtil.updateFromConfig(config)
        return config
      }
    } catch (error) {
      console.warn('[AppConfig] 加载配置失败，尝试使用本地缓存', error)
      var cached = storage.get(CACHE_KEY)
      if (cached && typeof cached === 'object') {
        memberUtil.updateFromConfig(cached)
        return cached
      }
    }

    memberUtil.updateFromConfig(DEFAULT_CONFIG)
    storage.set(CACHE_KEY, DEFAULT_CONFIG)
    return DEFAULT_CONFIG
  }

  getConfig() {
    var cached = storage.get(CACHE_KEY)
    if (cached && typeof cached === 'object') {
      return Object.assign({}, DEFAULT_CONFIG, cached)
    }
    return Object.assign({}, DEFAULT_CONFIG)
  }

  isChargeEnabled() {
    return !!this.getConfig().chargeEnabled
  }
}

export default new AppConfigUtil()
