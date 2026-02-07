<template>
  <div class="app-container">
    <!-- 总览统计 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">总用户数</div>
          <div class="stat-value primary">{{ overview.totalUsers || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">个人物品</div>
          <div class="stat-value success">{{ overview.totalPersonalItems || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">共享空间</div>
          <div class="stat-value warning">{{ overview.totalSharedSpaces || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">共享物品</div>
          <div class="stat-value info">{{ overview.totalSharedItems || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">活跃会员</div>
          <div class="stat-value danger">{{ overview.activeMembers || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">总收入</div>
          <div class="stat-value primary">￥{{ (overview.totalRevenue || 0).toFixed(2) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 物品过期统计 -->
      <el-col :span="12">
        <el-card shadow="hover" class="mb20">
          <div slot="header"><span>物品过期统计</span></div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="sub-title">个人物品</div>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="总数">{{ personalItems.total }}</el-descriptions-item>
                <el-descriptions-item label="已过期"><span class="text-danger">{{ personalItems.expired }}</span></el-descriptions-item>
                <el-descriptions-item label="即将过期"><span class="text-warning">{{ personalItems.expiringSoon }}</span></el-descriptions-item>
                <el-descriptions-item label="正常">{{ personalItems.normal }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="12">
              <div class="sub-title">共享物品</div>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="总数">{{ sharedItems.total }}</el-descriptions-item>
                <el-descriptions-item label="已过期"><span class="text-danger">{{ sharedItems.expired }}</span></el-descriptions-item>
                <el-descriptions-item label="即将过期"><span class="text-warning">{{ sharedItems.expiringSoon }}</span></el-descriptions-item>
                <el-descriptions-item label="正常">{{ sharedItems.normal }}</el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 订单统计 -->
      <el-col :span="12">
        <el-card shadow="hover" class="mb20">
          <div slot="header"><span>订单统计</span></div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="总订单数">{{ orderStats.totalOrders || 0 }}</el-descriptions-item>
            <el-descriptions-item label="总收入">￥{{ (orderStats.totalRevenue || 0).toFixed(2) }}</el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">按状态</el-divider>
          <div class="tag-group">
            <el-tag v-for="(count, status) in orderStats.byStatus" :key="status" class="mr10 mb10">
              {{ status }}: {{ count }}
            </el-tag>
          </div>
          <el-divider content-position="left">按套餐</el-divider>
          <div class="tag-group">
            <el-tag v-for="(count, plan) in orderStats.byPlanType" :key="plan" type="success" class="mr10 mb10">
              {{ getPlanName(plan) }}: {{ count }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 会员统计 -->
      <el-col :span="12">
        <el-card shadow="hover" class="mb20">
          <div slot="header"><span>会员统计</span></div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="总会员数">{{ memberStats.total || 0 }}</el-descriptions-item>
            <el-descriptions-item label="活跃会员"><span class="text-success">{{ memberStats.active || 0 }}</span></el-descriptions-item>
            <el-descriptions-item label="过期会员"><span class="text-danger">{{ memberStats.expired || 0 }}</span></el-descriptions-item>
          </el-descriptions>
          <el-divider content-position="left">按套餐类型</el-divider>
          <div class="tag-group">
            <el-tag v-for="(count, plan) in memberStats.byPlanType" :key="plan" type="warning" class="mr10 mb10">
              {{ getPlanName(plan) }}: {{ count }}
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 广告观看统计 -->
      <el-col :span="12">
        <el-card shadow="hover" class="mb20">
          <div slot="header"><span>广告观看统计</span></div>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="总观看次数">{{ adWatchStats.totalWatchCount }}</el-descriptions-item>
            <el-descriptions-item label="总使用次数">{{ adWatchStats.totalUsedCount }}</el-descriptions-item>
            <el-descriptions-item label="剩余额度">{{ adWatchStats.totalRemaining }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getOverview, getExpiryStats, getOrderStats, getMemberStats } from "@/api/overdue/statistics";

export default {
  name: "OverdueStatistics",
  data() {
    return {
      overview: {},
      expiryStats: {},
      orderStats: {},
      memberStats: {}
    };
  },
  computed: {
    personalItems() {
      return this.expiryStats.personalItems || { total: 0, expired: 0, expiringSoon: 0, normal: 0 };
    },
    sharedItems() {
      return this.expiryStats.sharedItems || { total: 0, expired: 0, expiringSoon: 0, normal: 0 };
    },
    adWatchStats() {
      return this.overview.adWatchStats || { totalWatchCount: 0, totalUsedCount: 0, totalRemaining: 0 };
    }
  },
  created() {
    this.loadData();
  },
  methods: {
    loadData() {
      getOverview().then(res => { this.overview = res.data || {}; });
      getExpiryStats().then(res => { this.expiryStats = res.data || {}; });
      getOrderStats().then(res => { this.orderStats = res.data || {}; });
      getMemberStats().then(res => { this.memberStats = res.data || {}; });
    },
    getPlanName(plan) {
      const names = { monthly: '月卡', quarterly: '季卡', yearly: '年卡', none: '无' };
      return names[plan] || plan;
    }
  }
};
</script>

<style scoped>
.mb20 { margin-bottom: 20px; }
.mr10 { margin-right: 10px; }
.mb10 { margin-bottom: 10px; }
.stat-card { text-align: center; }
.stat-title { font-size: 14px; color: #909399; margin-bottom: 10px; }
.stat-value { font-size: 24px; font-weight: bold; }
.stat-value.primary { color: #409EFF; }
.stat-value.success { color: #67C23A; }
.stat-value.warning { color: #E6A23C; }
.stat-value.info { color: #909399; }
.stat-value.danger { color: #F56C6C; }
.sub-title { font-weight: bold; margin-bottom: 10px; color: #606266; }
.tag-group { display: flex; flex-wrap: wrap; }
.text-danger { color: #F56C6C; font-weight: bold; }
.text-warning { color: #E6A23C; font-weight: bold; }
.text-success { color: #67C23A; font-weight: bold; }
</style>
