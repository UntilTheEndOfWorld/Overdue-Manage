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
          <view class="shelf-life-input-wrap">
            <input class="input shelf-life-input" type="number" v-model="form.shelfLife" placeholder="保质期" />
          </view>
          <picker mode="selector" :range="shelfLifeUnits" :value="unitIndex" @change="onUnitChange">
            <view class="picker shelf-life-unit-picker">{{ form.unit || '天' }}</view>
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
        <button class="btn btn-danger" @click="deleteItem">删除</button>
        <button class="btn btn-primary" @click="save">保存</button>
      </view>
    </view>
  </view>
</template>

<script>
import dateUtil from '@/common/utils/date.js'
import validator from '@/common/utils/validator.js'
import themeMixin from '@/common/mixins/theme.js'
import api from '@/common/utils/api.js'

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
    async loadItem() {
      try {
        var res
        var item
        if (this.itemType === 'shared') {
          res = await api.getSharedItemDetail(this.spaceId, this.itemId)
          if (res && res.code === 200 && res.data) {
            item = res.data
          }
        } else {
          res = await api.getPersonalItemDetail(this.itemId)
          if (res && res.code === 200 && res.data) {
            item = res.data
          }
        }
        if (item) {
          this.form = Object.assign({}, item)
          this.categoryIndex = this.categories.indexOf(item.category)
          if (this.categoryIndex < 0) this.categoryIndex = 0
          this.unitIndex = this.shelfLifeUnits.indexOf(item.unit || '天')
          if (this.unitIndex < 0) this.unitIndex = 0
        }
      } catch (error) {
        console.error('加载物品详情失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
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
    async save() {
      // 表单验证
      var validation = validator.validateItemForm(this.form)
      if (!validator.showErrors(validation.errors)) {
        return
      }

      var updateData = {
        name: this.form.name,
        category: this.form.category,
        purchaseDate: this.form.purchaseDate || null,
        productionDate: this.form.productionDate,
        shelfLife: this.form.shelfLife,
        unit: this.form.unit,
        expiryDate: this.form.expiryDate
      }

      try {
        if (this.itemType === 'shared') {
          await api.updateSharedItem(this.spaceId, this.itemId, updateData)
        } else {
          await api.updatePersonalItem(this.itemId, updateData)
        }
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(function() { uni.navigateBack() }, 1500)
      } catch (error) {
        console.error('保存物品失败:', error)
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
      }
    },
    deleteItem() {
      var self = this
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个物品吗？',
        success: function(res) {
          if (res.confirm) {
            var deletePromise
            if (self.itemType === 'shared') {
              deletePromise = api.deleteSharedItem(self.spaceId, self.itemId)
            } else {
              deletePromise = api.deletePersonalItem(self.itemId)
            }
            deletePromise.then(function() {
              uni.showToast({ title: '删除成功', icon: 'success' })
              setTimeout(function() { uni.navigateBack() }, 1500)
            }).catch(function(error) {
              console.error('删除物品失败:', error)
              uni.showToast({ title: error.message || '删除失败', icon: 'none' })
            })
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

/* 与 .picker 同高，避免原生 input 与选择器展示区域高度不一致 */
.input {
  width: 100%;
  height: 88rpx;
  min-height: 88rpx;
  padding: 0 30rpx;
  line-height: 88rpx;
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
  height: 88rpx;
  min-height: 88rpx;
  padding: 0 30rpx;
  display: flex;
  align-items: center;
  box-sizing: border-box;
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
  align-items: stretch;
  gap: 20rpx;
}

.shelf-life-input-wrap {
  flex: 1;
  min-width: 0;
  height: 88rpx;
  box-sizing: border-box;
}

.shelf-life-group .shelf-life-input {
  display: block;
  width: 100%;
  height: 88rpx !important;
  min-height: 88rpx !important;
  line-height: 88rpx !important;
}

.shelf-life-group .shelf-life-unit-picker {
  width: 150rpx;
  flex-shrink: 0;
  height: 88rpx !important;
  min-height: 88rpx !important;
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
