/**
 * 日期工具类
 */
class DateUtil {
  /**
   * 格式化日期为 YYYY-MM-DD
   */
  formatDate(date) {
    if (!date) return ''
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  /**
   * 获取今天日期字符串
   */
  getToday() {
    return this.formatDate(new Date())
  }

  /**
   * 计算过期日期
   */
  calculateExpiryDate(productionDate, shelfLife, unit = 'day') {
    if (!productionDate || !shelfLife) return null
    
    const date = new Date(productionDate)
    let days = shelfLife
    
    if (unit === 'month') {
      days = shelfLife * 30
    } else if (unit === 'year') {
      days = shelfLife * 365
    }
    
    date.setDate(date.getDate() + days)
    return this.formatDate(date)
  }

  /**
   * 获取物品状态
   * @returns {status: 'normal'|'near'|'expired', days: number}
   */
  getItemStatus(expiryDate) {
    if (!expiryDate) return { status: 'normal', days: 0 }
    
    const now = new Date()
    const expiry = new Date(expiryDate)
    const diffTime = expiry - now
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    
    if (diffDays < 0) {
      return { status: 'expired', days: Math.abs(diffDays) }
    } else if (diffDays <= 7) {
      return { status: 'near', days: diffDays }
    } else {
      return { status: 'normal', days: diffDays }
    }
  }

  /**
   * 格式化显示日期
   */
  formatDisplayDate(date) {
    if (!date) return ''
    const d = new Date(date)
    const month = d.getMonth() + 1
    const day = d.getDate()
    return `${month}月${day}日`
  }

  /**
   * 获取相对时间描述
   */
  getRelativeTime(date) {
    if (!date) return ''
    
    const now = new Date()
    const target = new Date(date)
    const diffTime = target - now
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    
    if (diffDays < 0) {
      return `已过期 ${Math.abs(diffDays)} 天`
    } else if (diffDays === 0) {
      return '今天过期'
    } else if (diffDays === 1) {
      return '明天过期'
    } else if (diffDays <= 7) {
      return `${diffDays} 天后过期`
    } else {
      return this.formatDisplayDate(date)
    }
  }
}

export default new DateUtil()
