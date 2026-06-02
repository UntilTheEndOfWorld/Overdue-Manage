/**
 * 会员工具类
 */
import storage from './storage.js'

const DEFAULT_CONFIG = {
  chargeEnabled: false,
  freePersonalItemLimit: 10,
  freeSharedSpaceLimit: 1,
  freeSharedItemLimit: 10
}

class MemberUtil {
  _config = Object.assign({}, DEFAULT_CONFIG)

  // 兼容旧代码引用
  FREE_QUOTA = DEFAULT_CONFIG.freePersonalItemLimit
  
  // 会员套餐
  PLANS = {
    monthly: {
      id: 'monthly',
      name: '月度会员',
      price: 6,
      period: '月',
      days: 30
    },
    quarterly: {
      id: 'quarterly',
      name: '季度会员',
      price: 15,
      period: '季度',
      days: 90
    },
    yearly: {
      id: 'yearly',
      name: '年度会员',
      price: 50,
      period: '年',
      days: 365
    },
    lifetime: {
      id: 'lifetime',
      name: '终身会员',
      price: 199,
      period: '永久',
      days: 99999
    }
  }

  /**
   * 从后端配置更新额度与收费开关
   */
  updateFromConfig(config) {
    if (!config || typeof config !== 'object') {
      return
    }
    this._config = Object.assign({}, DEFAULT_CONFIG, config)
    this.FREE_QUOTA = this.getFreePersonalItemLimit()
  }

  isChargeEnabled() {
    return !!this._config.chargeEnabled
  }

  getFreePersonalItemLimit() {
    var limit = Number(this._config.freePersonalItemLimit)
    return limit > 0 ? limit : DEFAULT_CONFIG.freePersonalItemLimit
  }

  getFreeSharedSpaceLimit() {
    var limit = Number(this._config.freeSharedSpaceLimit)
    return limit > 0 ? limit : DEFAULT_CONFIG.freeSharedSpaceLimit
  }

  getFreeSharedItemLimit() {
    var limit = Number(this._config.freeSharedItemLimit)
    return limit > 0 ? limit : DEFAULT_CONFIG.freeSharedItemLimit
  }

  /**
   * 获取用户会员信息
   */
  getMemberInfo() {
    const memberInfo = storage.get('memberInfo', {
      isMember: false,
      planType: null,
      expireTime: null,
      purchaseTime: null
    })
    return memberInfo
  }

  /**
   * 设置会员信息
   */
  setMemberInfo(memberInfo) {
    storage.set('memberInfo', memberInfo)
  }

  /**
   * 检查是否是会员（未开启收费时始终视为非会员）
   */
  isMember() {
    if (!this.isChargeEnabled()) {
      return false
    }

    const memberInfo = this.getMemberInfo()
    if (!memberInfo.isMember) {
      return false
    }
    
    // 终身会员
    if (memberInfo.planType === 'lifetime') {
      return true
    }
    
    // 检查会员是否过期
    if (memberInfo.expireTime) {
      const now = new Date()
      const expireTime = new Date(memberInfo.expireTime)
      if (now < expireTime) {
        return true
      } else {
        // 会员已过期
        memberInfo.isMember = false
        this.setMemberInfo(memberInfo)
        return false
      }
    }
    
    return false
  }

  /**
   * 购买会员
   */
  purchaseMember(planId) {
    const plan = this.PLANS[planId]
    if (!plan) {
      return false
    }
    
    const memberInfo = this.getMemberInfo()
    const now = new Date()
    let expireTime = null
    
    if (planId === 'lifetime') {
      expireTime = null // 永久
    } else {
      expireTime = new Date(now.getTime() + plan.days * 24 * 60 * 60 * 1000)
    }
    
    memberInfo.isMember = true
    memberInfo.planType = planId
    memberInfo.expireTime = expireTime ? expireTime.toISOString() : null
    memberInfo.purchaseTime = now.toISOString()
    
    this.setMemberInfo(memberInfo)
    return true
  }

  /**
   * 获取已使用的物品数量
   */
  getUsedQuota() {
    const items = storage.get('personalItems', [])
    return Array.isArray(items) ? items.length : 0
  }

  /**
   * 获取可用额度
   */
  getAvailableQuota() {
    if (this.isMember()) {
      return Infinity // 会员无限制
    }
    return this.getFreePersonalItemLimit()
  }

  /**
   * 获取剩余额度
   */
  getRemainingQuota() {
    if (this.isMember()) {
      return Infinity
    }
    const used = this.getUsedQuota()
    return Math.max(0, this.getFreePersonalItemLimit() - used)
  }

  /**
   * 检查是否可以添加物品
   */
  canAddItem() {
    if (this.isMember()) {
      return true
    }
    const used = this.getUsedQuota()
    return used < this.getFreePersonalItemLimit()
  }

  /**
   * 检查是否可以创建共享空间
   */
  canCreateSharedSpace(currentCount) {
    if (this.isMember()) {
      return true
    }
    var count = Number(currentCount)
    if (!Number.isFinite(count) || count < 0) {
      count = 0
    }
    return count < this.getFreeSharedSpaceLimit()
  }

  /**
   * 检查共享空间是否还能添加物品
   */
  canAddSharedItem(currentCount) {
    if (this.isMember()) {
      return true
    }
    var count = Number(currentCount)
    if (!Number.isFinite(count) || count < 0) {
      count = 0
    }
    return count < this.getFreeSharedItemLimit()
  }

  /**
   * 获取广告观看记录
   */
  getAdWatchRecord() {
    return storage.get('adWatchRecord', {
      count: 0, // 通过看广告获得的额外额度
      lastWatchTime: null
    })
  }

  /**
   * 记录观看广告
   */
  recordAdWatch() {
    const record = this.getAdWatchRecord()
    record.count += 1
    record.lastWatchTime = new Date().toISOString()
    storage.set('adWatchRecord', record)
  }

  /**
   * 通过看广告获得额度后的可用数量
   */
  getAdQuota() {
    const record = this.getAdWatchRecord()
    return record.count
  }

  /**
   * 检查是否可以添加物品（包括广告额度，仅收费模式生效）
   */
  canAddItemWithAd() {
    if (this.isMember()) {
      return true
    }
    const used = this.getUsedQuota()
    const limit = this.getFreePersonalItemLimit()
    if (!this.isChargeEnabled()) {
      return used < limit
    }
    const adQuota = this.getAdQuota()
    return used < (limit + adQuota)
  }

  /**
   * 使用广告额度添加物品
   */
  useAdQuota() {
    if (!this.isChargeEnabled()) {
      return false
    }
    const record = this.getAdWatchRecord()
    if (record.count > 0) {
      record.count -= 1
      storage.set('adWatchRecord', record)
      return true
    }
    return false
  }

  /**
   * 额度不足时的统一处理
   */
  handlePersonalQuotaExceeded() {
    if (this.isChargeEnabled()) {
      uni.navigateTo({
        url: '/pages/member/upgrade'
      })
      return
    }
    uni.showToast({
      title: '个人物品已达上限（' + this.getFreePersonalItemLimit() + '个）',
      icon: 'none'
    })
  }

  handleSharedSpaceQuotaExceeded() {
    if (this.isChargeEnabled()) {
      uni.navigateTo({
        url: '/pages/member/member'
      })
      return
    }
    uni.showToast({
      title: '共享空间已达上限（' + this.getFreeSharedSpaceLimit() + '个）',
      icon: 'none'
    })
  }

  handleSharedItemQuotaExceeded() {
    if (this.isChargeEnabled()) {
      uni.navigateTo({
        url: '/pages/member/member'
      })
      return
    }
    uni.showToast({
      title: '空间物品已达上限（' + this.getFreeSharedItemLimit() + '个）',
      icon: 'none'
    })
  }
}

export default new MemberUtil()
