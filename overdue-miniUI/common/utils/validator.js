/**
 * 表单验证工具
 */
class Validator {
  /**
   * 验证物品表单
   */
  validateItemForm(form) {
    const errors = []

    if (!form.name || form.name.trim() === '') {
      errors.push('请输入物品名称')
    }

    if (!form.category) {
      errors.push('请选择分类')
    }

    if (!form.productionDate) {
      errors.push('请选择生产日期')
    }

    if (!form.shelfLife || form.shelfLife <= 0) {
      errors.push('请输入有效的保质期')
    }

    if (!form.expiryDate) {
      errors.push('请确认过期日期')
    }

    // 验证过期日期必须晚于生产日期
    if (form.productionDate && form.expiryDate) {
      const prodDate = new Date(form.productionDate)
      const expDate = new Date(form.expiryDate)
      if (expDate <= prodDate) {
        errors.push('过期日期必须晚于生产日期')
      }
    }

    return {
      valid: errors.length === 0,
      errors
    }
  }

  /**
   * 显示验证错误
   */
  showErrors(errors) {
    if (errors.length > 0) {
      uni.showToast({
        title: errors[0],
        icon: 'none',
        duration: 2000
      })
      return false
    }
    return true
  }
}

export default new Validator()
