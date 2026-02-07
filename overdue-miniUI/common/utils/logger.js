/**
 * 操作日志工具
 */
import storage from './storage.js'

class Logger {
  /**
   * 记录操作日志
   */
  logOperation(spaceId, userId, userName, operationType, itemId, itemName, details = {}) {
    const logs = storage.get('operationLogs', [])
    const log = {
      id: Date.now().toString(),
      spaceId,
      userId,
      operatorName: userName,
      operationType, // 'add', 'update', 'delete'
      itemId,
      itemName,
      details: JSON.stringify(details),
      operationTime: new Date().toISOString()
    }
    logs.push(log)
    storage.set('operationLogs', logs)
    return log
  }

  /**
   * 获取空间的操作日志
   * @param {string} spaceId - 空间ID，'personal'表示个人空间
   */
  getSpaceLogs(spaceId, filterType = 'all') {
    const allLogs = storage.get('operationLogs', [])
    let logs = allLogs.filter(log => log.spaceId === spaceId)
    
    if (filterType !== 'all') {
      logs = logs.filter(log => log.operationType === filterType)
    }
    
    return logs.sort((a, b) => new Date(b.operationTime) - new Date(a.operationTime))
  }

  /**
   * 获取个人操作日志
   */
  getPersonalLogs(filterType = 'all') {
    return this.getSpaceLogs('personal', filterType)
  }

  /**
   * 获取操作文本
   */
  getOperationText(log) {
    let text = ''
    const typeMap = {
      'add': '添加了物品',
      'update': '更新了物品',
      'delete': '删除了物品'
    }
    text = typeMap[log.operationType] || '未知操作'
    
    // 添加物品名称
    if (log.itemName) {
      text += `"${log.itemName}"`
    }
    
    // 如果有详细信息，显示修改内容
    if (log.details && log.operationType === 'update') {
      try {
        const details = JSON.parse(log.details)
        const changes = []
        if (details.name) changes.push(`名称: ${details.name}`)
        if (details.category) changes.push(`分类: ${details.category}`)
        if (details.expiryDate) changes.push(`过期日期: ${details.expiryDate}`)
        if (details.productionDate) changes.push(`生产日期: ${details.productionDate}`)
        if (changes.length > 0) {
          text += ` (${changes.join(', ')})`
        }
      } catch (e) {
        // 解析失败，忽略
      }
    }
    
    return text
  }
}

export default new Logger()
