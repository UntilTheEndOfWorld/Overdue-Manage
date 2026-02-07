/**
 * 会员工具类
 */
import storage from './storage.js'

class MemberUtil {
  // 免费额度
  FREE_QUOTA = 5
  
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
   * 检查是否是会员
   */
  isMember() {
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
    return items.length
  }

  /**
   * 获取可用额度
   */
  getAvailableQuota() {
    if (this.isMember()) {
      return Infinity // 会员无限制
    }
    return this.FREE_QUOTA
  }

  /**
   * 获取剩余额度
   */
  getRemainingQuota() {
    if (this.isMember()) {
      return Infinity
    }
    const used = this.getUsedQuota()
    return Math.max(0, this.FREE_QUOTA - used)
  }

  /**
   * 检查是否可以添加物品
   */
  canAddItem() {
    if (this.isMember()) {
      return true
    }
    const used = this.getUsedQuota()
    return used < this.FREE_QUOTA
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
   * 检查是否可以添加物品（包括广告额度）
   */
  canAddItemWithAd() {
    if (this.isMember()) {
      return true
    }
    const used = this.getUsedQuota()
    const adQuota = this.getAdQuota()
    return used < (this.FREE_QUOTA + adQuota)
  }

  /**
   * 使用广告额度添加物品
   */
  useAdQuota() {
    const record = this.getAdWatchRecord()
    if (record.count > 0) {
      record.count -= 1
      storage.set('adWatchRecord', record)
      return true
    }
    return false
  }
}

export default new MemberUtil()
