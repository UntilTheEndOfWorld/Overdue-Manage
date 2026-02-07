<template>
  <view class="container" :class="themeClass">
    <view class="form">
      <view class="form-group">
        <text class="label">物品名称 *</text>
        <input class="input" v-model="form.name" placeholder="请输入物品名称" />
      </view>

      <view class="form-group">
        <text class="label">分类 *</text>
        <picker mode="selector" :range="categories" :value="categoryIndex" @change="onCategoryChange">
          <view class="picker">{{ form.category || '请选择分类' }}</view>
        </picker>
      </view>

      <view class="form-group">
        <text class="label">购买日期</text>
        <picker mode="date" :value="form.purchaseDate" @change="onPurchaseDateChange">
          <view class="picker">{{ form.purchaseDate || '请选择购买日期（可选）' }}</view>
        </picker>
      </view>

      <view class="form-group">
        <text class="label">生产日期 *</text>
        <picker mode="date" :value="form.productionDate" @change="onProductionDateChange">
          <view class="picker">{{ form.productionDate || '请选择生产日期' }}</view>
        </picker>
      </view>

      <view class="form-group">
        <text class="label">保质期 *</text>
        <view class="shelf-life-group">
          <input class="input" type="number" v-model="form.shelfLife" placeholder="保质期" />
          <picker mode="selector" :range="shelfLifeUnits" :value="unitIndex" @change="onUnitChange">
            <view class="picker">{{ form.unit || '天' }}</view>
          </picker>
        </view>
      </view>

      <view class="form-group">
        <text class="label">过期日期 *</text>
        <picker mode="date" :value="form.expiryDate" @change="onExpiryDateChange">
          <view class="picker">{{ form.expiryDate || '自动计算' }}</view>
        </picker>
      </view>

      <view class="btn-group">
        <button class="btn btn-secondary" @click="cancel">取消</button>
        <button class="btn btn-primary" @click="save">保存</button>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import dateUtil from '@/common/utils/date.js'
import logger from '@/common/utils/logger.js'
import validator from '@/common/utils/validator.js'
import themeMixin from '@/common/mixins/theme.js'
import memberUtil from '@/common/utils/member.js'

export default {
  mixins: [themeMixin],
  data() {
    return {
      spaceId: '',
      itemType: 'personal',
      categories: ['食品', '药品', '其他'],
      categoryIndex: 0,
      shelfLifeUnits: ['天', '月', '年'],
      unitIndex: 0,
      form: {
        name: '',
        category: '食品',
        purchaseDate: '',
        productionDate: dateUtil.getToday(),
        shelfLife: 30,
        unit: '天',
        expiryDate: ''
      }
    }
  },
  onLoad(options) {
    this.spaceId = options.spaceId || ''
    this.itemType = options.type || 'personal'
    this.calculateExpiryDate()
  },
  methods: {
    onCategoryChange(e) {
      this.categoryIndex = e.detail.value
      this.form.category = this.categories[e.detail.value]
    },
    onPurchaseDateChange(e) {
      this.form.purchaseDate = e.detail.value
    },
    onUnitChange(e) {
      this.unitIndex = e.detail.value
      this.form.unit = this.shelfLifeUnits[e.detail.value]
      this.calculateExpiryDate()
    },
    onProductionDateChange(e) {
      this.form.productionDate = e.detail.value
      this.calculateExpiryDate()
    },
    onExpiryDateChange(e) {
      this.form.expiryDate = e.detail.value
    },
    calculateExpiryDate() {
      if (this.form.productionDate && this.form.shelfLife) {
        const unit = this.form.unit === '天' ? 'day' : (this.form.unit === '月' ? 'month' : 'year')
        this.form.expiryDate = dateUtil.calculateExpiryDate(
          this.form.productionDate,
          this.form.shelfLife,
          unit
        )
      }
    },
    save() {
      // 表单验证
      const validation = validator.validateItemForm(this.form)
      if (!validator.showErrors(validation.errors)) {
        return
      }

      // 检查额度（仅个人物品需要检查）
      if (this.itemType === 'personal' || !this.itemType) {
        if (!memberUtil.canAddItemWithAd()) {
          // 额度已用完，跳转到升级页面
          uni.showModal({
            title: '免费额度已用完',
            content: '您已用完免费额度，请观看广告或升级会员继续添加物品',
            confirmText: '去升级',
            cancelText: '取消',
            success: (res) => {
              if (res.confirm) {
                uni.navigateTo({
                  url: '/pages/member/upgrade'
                })
              }
            }
          })
          return
        }
        
        // 如果使用了广告额度，消耗一个广告额度
        const used = memberUtil.getUsedQuota()
        const freeQuota = memberUtil.FREE_QUOTA
        if (used >= freeQuota) {
          // 使用了广告额度
          memberUtil.useAdQuota()
        }
      }

      const items = storage.get('personalItems', [])
      const userInfo = storage.get('userInfo', {})
      const newItem = {
        id: Date.now().toString(),
        name: this.form.name,
        category: this.form.category,
        purchaseDate: this.form.purchaseDate || null,
        productionDate: this.form.productionDate,
        shelfLife: this.form.shelfLife,
        unit: this.form.unit,
        expiryDate: this.form.expiryDate,
        createdAt: new Date().toISOString()
      }
      items.push(newItem)
      storage.set('personalItems', items)

      // 记录操作日志
      if (this.spaceId && this.itemType === 'shared') {
        // 共享空间物品日志
        // 保存到共享物品列表
        const sharedItems = storage.get('sharedItems', [])
        sharedItems.push({
          ...newItem,
          spaceId: this.spaceId,
          creatorId: userInfo.openid || '',
          creatorName: userInfo.nickName || '用户'
        })
        storage.set('sharedItems', sharedItems)

        // 记录操作日志
        logger.logOperation(
          this.spaceId,
          userInfo.openid || '',
          userInfo.nickName || '用户',
          'add',
          newItem.id,
          newItem.name,
          { category: newItem.category, expiryDate: newItem.expiryDate }
        )
      } else {
        // 个人物品日志
        logger.logOperation(
          'personal',
          userInfo.openid || '',
          userInfo.nickName || '我',
          'add',
          newItem.id,
          newItem.name,
          { 
            category: newItem.category, 
            expiryDate: newItem.expiryDate,
            productionDate: newItem.productionDate
          }
        )
      }

      uni.showToast({ title: '添加成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    },
    cancel() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.container {
  padding: 40rpx;
  min-height: 100vh;
  background: var(--primary-bg);
  transition: background-color 0.3s ease;
}

.form {
  background: var(--card-bg-solid);
  border-radius: 30rpx;
  padding: 50rpx;
  border: 2rpx solid var(--card-border);
  box-shadow: 0 10rpx 40rpx var(--shadow-color);
  transition: all 0.3s ease;
}

.form-group {
  margin-bottom: 40rpx;
}

.label {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20rpx;
}

.input {
  width: 100%;
  padding: 24rpx 30rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 16rpx;
  font-size: 32rpx;
  color: var(--text-primary);
  transition: all 0.3s ease;
  box-sizing: border-box;
  margin: 0;
}

.input:focus {
  border-color: var(--accent-blue);
  background: var(--active-bg);
}

.picker {
  padding: 24rpx 30rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 16rpx;
  font-size: 32rpx;
  color: var(--text-primary);
  transition: all 0.3s ease;
}

.picker:active {
  border-color: var(--accent-blue);
  background: var(--active-bg);
}

.shelf-life-group {
  display: flex;
  gap: 20rpx;
}

.shelf-life-group .input {
  flex: 1;
}

.shelf-life-group .picker {
  width: 150rpx;
}

.btn-group {
  display: flex;
  gap: 30rpx;
  margin-top: 60rpx;
}

.btn {
  flex: 1;
  padding: 28rpx;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

.btn-primary {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3);
  
  &:active {
    box-shadow: 0 2rpx 8rpx rgba(59, 130, 246, 0.4);
  }
}

.btn-secondary {
  background: var(--hover-bg);
  color: var(--text-secondary);
  border: 2rpx solid var(--card-border);
  
  &:active {
    background: var(--active-bg);
  }
}

/* 浅色模式下的按钮增强对比度 */
.light-mode .btn-secondary {
  background: #f1f5f9 !important;
  border: 2rpx solid rgba(59, 130, 246, 0.3) !important;
  color: #475569 !important;
  
  &:active {
    background: #e2e8f0 !important;
  }
}

.light-mode .btn-primary {
  background: linear-gradient(135deg, #3b82f6, #14b8a6) !important;
  box-shadow: 0 4rpx 16rpx rgba(59, 130, 246, 0.4) !important;
}
</style>
