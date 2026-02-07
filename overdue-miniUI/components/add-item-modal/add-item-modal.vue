<template>
  <view class="modal" v-if="show" @click.stop="handleMaskClick">
    <view class="modal-content" @click.stop>
      <view class="modal-header">
        <text class="modal-title">添加新物品</text>
        <view class="close-btn" @click="handleClose">×</view>
      </view>
      
      <view class="modal-body">
        <view class="form-group">
          <text class="form-label">物品名称 *</text>
          <input 
            class="form-input" 
            v-model="form.name" 
            placeholder="例如：牛奶、维生素C片"
            maxlength="50"
          />
        </view>

        <view class="form-group">
          <text class="form-label">分类 *</text>
          <picker 
            mode="selector" 
            :range="categories" 
            :value="categoryIndex" 
            @change="onCategoryChange"
          >
            <view class="picker">{{ form.category || '请选择分类' }}</view>
          </picker>
        </view>

        <view class="form-group">
          <text class="form-label">购买日期</text>
          <picker 
            mode="date" 
            :value="form.purchaseDate" 
            @change="onPurchaseDateChange"
          >
            <view class="picker">{{ form.purchaseDate || '请选择购买日期（可选）' }}</view>
          </picker>
        </view>

        <view class="form-group">
          <text class="form-label">生产日期 *</text>
          <picker 
            mode="date" 
            :value="form.productionDate" 
            @change="onProductionDateChange"
          >
            <view class="picker">{{ form.productionDate || '请选择生产日期' }}</view>
          </picker>
        </view>

        <view class="form-group">
          <text class="form-label">保质期 *</text>
          <view class="shelf-life-group">
            <input 
              class="form-input" 
              type="number" 
              v-model="form.shelfLife" 
              placeholder="保质期"
              @input="calculateExpiryDate"
            />
            <picker 
              mode="selector" 
              :range="shelfLifeUnits" 
              :value="unitIndex" 
              @change="onUnitChange"
            >
              <view class="picker">{{ form.unit || '天' }}</view>
            </picker>
          </view>
        </view>

        <view class="form-group">
          <text class="form-label">过期日期 *</text>
          <picker 
            mode="date" 
            :value="form.expiryDate" 
            @change="onExpiryDateChange"
          >
            <view class="picker">{{ form.expiryDate || '自动计算' }}</view>
          </picker>
        </view>
      </view>
      
      <view class="modal-footer">
        <button class="btn btn-secondary" @click="handleClose">取消</button>
        <button class="btn btn-primary" @click="handleSave">保存</button>
      </view>
    </view>
  </view>
</template>

<script>
import storage from '@/common/utils/storage.js'
import dateUtil from '@/common/utils/date.js'
import validator from '@/common/utils/validator.js'
import logger from '@/common/utils/logger.js'

export default {
  name: 'AddItemModal',
  options: {
    virtualHost: true
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
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
  watch: {
    show(newVal) {
      if (newVal) {
        // 弹窗显示时重置表单
        this.resetForm()
      }
    }
  },
  methods: {
    resetForm() {
      this.form = {
        name: '',
        category: '食品',
        purchaseDate: '',
        productionDate: dateUtil.getToday(),
        shelfLife: 30,
        unit: '天',
        expiryDate: ''
      }
      this.categoryIndex = 0
      this.unitIndex = 0
      this.calculateExpiryDate()
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
    handleSave() {
      // 表单验证
      const validation = validator.validateItemForm(this.form)
      if (!validator.showErrors(validation.errors)) {
        return
      }

      // 检查额度（仅个人物品需要检查）
      const memberUtil = require('@/common/utils/member.js').default
      if (!memberUtil.canAddItemWithAd()) {
        // 额度已用完，跳转到升级页面
        this.$emit('close')
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

      const items = storage.get('personalItems', [])
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
      const userInfo = storage.get('userInfo', {})
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

      uni.showToast({ title: '添加成功', icon: 'success' })
      
      // 触发添加成功事件，通知父组件刷新数据
      this.$emit('success', newItem)
      
      // 关闭弹窗
      this.handleClose()
    },
    handleClose() {
      this.$emit('close')
    },
    handleMaskClick() {
      // 点击遮罩层关闭弹窗
      this.handleClose()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/style/common.scss';

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--modal-overlay);
  backdrop-filter: blur(10rpx);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: var(--card-bg-solid);
  border-radius: 48rpx;
  border: 2rpx solid var(--card-border);
  backdrop-filter: blur(40rpx);
  width: 100%;
  max-width: 600rpx;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 50rpx 100rpx -24rpx var(--shadow-color);
  animation: modalSlideIn 0.4s ease-out;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(60rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  padding: 50rpx 60rpx 30rpx;
  border-bottom: 2rpx solid var(--card-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 48rpx;
  font-weight: 600;
  color: var(--text-primary);
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60rpx;
  color: var(--text-secondary);
  line-height: 1;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.close-btn:active {
  color: var(--text-primary);
  transform: scale(0.9);
}

.modal-body {
  padding: 40rpx 60rpx;
  overflow-y: auto;
  flex: 1;
}

.form-group {
  margin-bottom: 40rpx;
}

.form-label {
  display: block;
  margin-bottom: 16rpx;
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 28rpx;
}

.form-input {
  width: 100%;
  padding: 28rpx 36rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 24rpx;
  color: var(--text-primary);
  font-size: 32rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
  margin: 0;
}

.form-input:focus {
  outline: none;
  border-color: var(--accent-blue);
  background: var(--active-bg);
}

.picker {
  padding: 28rpx 36rpx;
  background: var(--hover-bg);
  border: 2rpx solid var(--card-border);
  border-radius: 24rpx;
  font-size: 32rpx;
  color: var(--text-primary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.picker:active {
  border-color: var(--accent-blue);
  background: var(--active-bg);
}

.shelf-life-group {
  display: flex;
  gap: 20rpx;
}

.shelf-life-group .form-input {
  flex: 1;
}

.shelf-life-group .picker {
  width: 150rpx;
  flex-shrink: 0;
}

.modal-footer {
  padding: 30rpx 60rpx 40rpx;
  border-top: 2rpx solid var(--card-border);
  display: flex;
  justify-content: flex-end;
  gap: 30rpx;
}

.btn {
  padding: 24rpx 48rpx;
  border-radius: 24rpx;
  font-weight: 500;
  font-size: 32rpx;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-secondary {
  background: var(--hover-bg);
  color: var(--text-secondary);
  border: 2rpx solid var(--card-border);
}

.btn-secondary:active {
  background: var(--active-bg);
}

.btn-primary {
  background: linear-gradient(135deg, var(--accent-blue), var(--accent-teal));
  color: white;
  font-weight: 600;
}

.btn-primary:active {
  transform: translateY(2rpx);
  box-shadow: 0 10rpx 20rpx rgba(59, 130, 246, 0.3);
}
</style>
