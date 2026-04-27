<template>
  <view class="container" :class="themeClass">
    <view class="form">
      <view class="form-group">
        <text class="label">物品名称 <text class="required">*</text></text>
        <input class="input" v-model="form.name" placeholder="请输入物品名称" />
      </view>

      <view class="form-group">
        <text class="label">分类 <text class="required">*</text></text>
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
        <text class="label">生产日期 <text class="required">*</text></text>
        <picker mode="date" :value="form.productionDate" @change="onProductionDateChange">
          <view class="picker">{{ form.productionDate || '请选择生产日期' }}</view>
        </picker>
      </view>

      <view class="form-group">
        <text class="label">保质期 <text class="required">*</text></text>
        <view class="shelf-life-group">
          <view class="shelf-life-input-wrap">
            <input class="input shelf-life-input" type="number" :value="form.shelfLife" @input="onShelfLifeInput" placeholder="保质期" />
          </view>
          <picker mode="selector" :range="shelfLifeUnits" :value="unitIndex" @change="onUnitChange">
            <view class="picker shelf-life-unit-picker">{{ form.unit || '天' }}</view>
          </picker>
        </view>
      </view>

      <view class="form-group">
        <text class="label">过期日期 <text class="required">*</text></text>
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
import validator from '@/common/utils/validator.js'
import themeMixin from '@/common/mixins/theme.js'
import memberUtil from '@/common/utils/member.js'
import api from '@/common/utils/api.js'

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
    onShelfLifeInput(e) {
      var raw = e && e.detail ? e.detail.value : ''
      var value = String(raw || '').replace(/[^\d]/g, '')
      this.form.shelfLife = value
      this.calculateExpiryDate()
    },
    onExpiryDateChange(e) {
      this.form.expiryDate = e.detail.value
    },
    calculateExpiryDate() {
      var shelfLifeValue = Number(this.form.shelfLife)
      if (this.form.productionDate && shelfLifeValue > 0) {
        const unit = this.form.unit === '天' ? 'day' : (this.form.unit === '月' ? 'month' : 'year')
        this.form.expiryDate = dateUtil.calculateExpiryDate(
          this.form.productionDate,
          shelfLifeValue,
          unit
        )
      } else if (!this.form.productionDate || !this.form.shelfLife) {
        this.form.expiryDate = ''
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

      var newItem = {
        name: this.form.name,
        category: this.form.category,
        purchaseDate: this.form.purchaseDate || null,
        productionDate: this.form.productionDate,
        shelfLife: Number(this.form.shelfLife) || 0,
        unit: this.form.unit,
        expiryDate: this.form.expiryDate
      }

      var self = this
      if (this.spaceId && this.itemType === 'shared') {
        // 共享空间物品 - 调用后端API
        api.addSharedItem(this.spaceId, newItem).then(function(res) {
          uni.showToast({ title: '添加成功', icon: 'success' })
          setTimeout(function() { uni.navigateBack() }, 1500)
        }).catch(function(error) {
          console.error('添加共享物品失败:', error)
          uni.showToast({ title: error.message || '添加失败', icon: 'none' })
        })
      } else {
        // 个人物品 - 调用后端API
        api.addPersonalItem(newItem).then(function(res) {
          // 同步缓存，避免返回列表页时因接口波动回退旧缓存导致“新增后看不到”
          var cachedItems = storage.get('personalItems', [])
          if (!Array.isArray(cachedItems)) {
            cachedItems = []
          }
          var createdItem = (res && res.data) ? Object.assign({}, newItem, res.data) : newItem
          cachedItems.unshift(createdItem)
          storage.set('personalItems', cachedItems)
          uni.showToast({ title: '添加成功', icon: 'success' })
          setTimeout(function() { uni.navigateBack() }, 1500)
        }).catch(function(error) {
          console.error('添加个人物品失败:', error)
          uni.showToast({ title: error.message || '添加失败', icon: 'none' })
        })
      }
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

.required {
  color: #ef4444;
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

/* 小程序里 input 在 flex 内易被压扁，用外层固定高度与 100% 高度拉齐单位选择器 */
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
