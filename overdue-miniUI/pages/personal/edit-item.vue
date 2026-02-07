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
        <button class="btn btn-danger" @click="deleteItem">删除</button>
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

export default {
  mixins: [themeMixin],
  data() {
    return {
      itemId: '',
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
        productionDate: '',
        shelfLife: 30,
        unit: '天',
        expiryDate: ''
      }
    }
  },
  onLoad(options) {
    this.itemId = options.id
    this.spaceId = options.spaceId || ''
    this.itemType = options.type || 'personal'
    this.loadItem()
  },
  methods: {
    loadItem() {
      if (this.itemType === 'shared') {
        const items = storage.get('sharedItems', [])
        const item = items.find(i => i.id === this.itemId && i.spaceId === this.spaceId)
        if (item) {
          this.form = { ...item }
          this.categoryIndex = this.categories.indexOf(item.category)
          this.unitIndex = this.shelfLifeUnits.indexOf(item.unit || '天')
        }
      } else {
        const items = storage.get('personalItems', [])
        const item = items.find(i => i.id === this.itemId)
        if (item) {
          this.form = { ...item }
          this.categoryIndex = this.categories.indexOf(item.category)
          this.unitIndex = this.shelfLifeUnits.indexOf(item.unit || '天')
        }
      }
    },
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

      const userInfo = storage.get('userInfo', {})
      
      if (this.itemType === 'shared') {
        const items = storage.get('sharedItems', [])
        const index = items.findIndex(i => i.id === this.itemId && i.spaceId === this.spaceId)
        if (index !== -1) {
          const oldItem = items[index]
          items[index] = { ...this.form, id: this.itemId, spaceId: this.spaceId }
          storage.set('sharedItems', items)

          // 记录操作日志 - 记录修改的字段
          const changes = {}
          if (oldItem.name !== this.form.name) changes.name = this.form.name
          if (oldItem.category !== this.form.category) changes.category = this.form.category
          if (oldItem.expiryDate !== this.form.expiryDate) changes.expiryDate = this.form.expiryDate
          if (oldItem.productionDate !== this.form.productionDate) changes.productionDate = this.form.productionDate
          
          logger.logOperation(
            this.spaceId,
            userInfo.openid || '',
            userInfo.nickName || '用户',
            'update',
            this.itemId,
            this.form.name,
            changes
          )
        }
      } else {
        const items = storage.get('personalItems', [])
        const index = items.findIndex(i => i.id === this.itemId)
        if (index !== -1) {
          const oldItem = items[index]
          items[index] = { ...this.form, id: this.itemId }
          storage.set('personalItems', items)

          // 记录操作日志 - 记录修改的字段
          const changes = {}
          if (oldItem.name !== this.form.name) changes.name = this.form.name
          if (oldItem.category !== this.form.category) changes.category = this.form.category
          if (oldItem.expiryDate !== this.form.expiryDate) changes.expiryDate = this.form.expiryDate
          if (oldItem.productionDate !== this.form.productionDate) changes.productionDate = this.form.productionDate
          
          logger.logOperation(
            'personal',
            userInfo.openid || '',
            userInfo.nickName || '我',
            'update',
            this.itemId,
            this.form.name,
            changes
          )
        }
      }

      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    },
    deleteItem() {
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个物品吗？',
        success: (res) => {
          if (res.confirm) {
            const userInfo = storage.get('userInfo', {})
            
            if (this.itemType === 'shared') {
              const items = storage.get('sharedItems', [])
              const item = items.find(i => i.id === this.itemId && i.spaceId === this.spaceId)
              const newItems = items.filter(i => !(i.id === this.itemId && i.spaceId === this.spaceId))
              storage.set('sharedItems', newItems)

              // 记录操作日志
              if (item) {
                logger.logOperation(
                  this.spaceId,
                  userInfo.openid || '',
                  userInfo.nickName || '用户',
                  'delete',
                  this.itemId,
                  item.name
                )
              }
            } else {
              const items = storage.get('personalItems', [])
              const item = items.find(i => i.id === this.itemId)
              const newItems = items.filter(i => i.id !== this.itemId)
              storage.set('personalItems', newItems)

              // 记录操作日志
              if (item) {
                logger.logOperation(
                  'personal',
                  userInfo.openid || '',
                  userInfo.nickName || '我',
                  'delete',
                  this.itemId,
                  item.name
                )
              }
            }

            uni.showToast({ title: '删除成功', icon: 'success' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          }
        }
      })
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

.btn-group .btn {
  flex: 1;
}

.btn {
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

.btn-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
  
  &:active {
    box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.4);
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

.light-mode .btn-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  box-shadow: 0 4rpx 16rpx rgba(239, 68, 68, 0.4) !important;
}
</style>
