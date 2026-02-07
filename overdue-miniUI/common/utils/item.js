/**
 * 物品相关工具函数
 */
import dateUtil from './date.js'

class ItemUtil {
  /**
   * 筛选物品
   */
  filterItems(items, filterType) {
    const now = new Date()
    const sevenDaysLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
    
    return items.filter(item => {
      const expiry = new Date(item.expiryDate)
      if (filterType === 'near') {
        return expiry > now && expiry <= sevenDaysLater
      } else if (filterType === 'expired') {
        return expiry <= now
      } else if (filterType === 'normal') {
        return expiry > sevenDaysLater
      }
      return true
    }).sort((a, b) => new Date(a.expiryDate) - new Date(b.expiryDate))
  }

  /**
   * 搜索物品
   */
  searchItems(items, keyword) {
    if (!keyword) return items
    const lowerKeyword = keyword.toLowerCase()
    return items.filter(item => {
      return item.name.toLowerCase().includes(lowerKeyword) ||
             (item.category && item.category.toLowerCase().includes(lowerKeyword))
    })
  }

  /**
   * 获取物品统计
   */
  getItemStats(items) {
    const now = new Date()
    const sevenDaysLater = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
    
    const total = items.length
    const expired = items.filter(item => new Date(item.expiryDate) <= now).length
    const near = items.filter(item => {
      const expiry = new Date(item.expiryDate)
      return expiry > now && expiry <= sevenDaysLater
    }).length
    const normal = total - expired - near

    return {
      total,
      expired,
      near,
      normal
    }
  }

  /**
   * 获取物品状态类名
   */
  getItemClass(item) {
    const status = dateUtil.getItemStatus(item.expiryDate)
    if (status.status === 'expired') return 'expired'
    if (status.status === 'near') return 'near-expiry'
    return ''
  }

  /**
   * 获取状态类名
   */
  getStatusClass(item) {
    const status = dateUtil.getItemStatus(item.expiryDate)
    return `status-${status.status}`
  }

  /**
   * 获取状态文本
   */
  getStatusText(item) {
    const status = dateUtil.getItemStatus(item.expiryDate)
    if (status.status === 'expired') return '已过期'
    if (status.status === 'near') return '即将过期'
    return '正常'
  }
}

export default new ItemUtil()
