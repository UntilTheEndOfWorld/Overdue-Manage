<template>
  <div class="app-container">
    <!-- 页头 -->
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #F59E0B;">
        <i class="el-icon-video-play"></i>
      </div>
      <div class="overdue-page-header__text">
        <h2>广告观看记录</h2>
        <p>统计用户广告观看次数与使用情况</p>
      </div>
    </div>

    <!-- 顶部统计卡片 -->
    <div class="overdue-stat-cards">
      <div class="overdue-stat-card stat-card--watch">
        <i class="stat-card-bg-icon el-icon-view"></i>
        <div class="overdue-stat-card__value" style="color:#2563eb">{{ statsData.totalWatch || 0 }}</div>
        <div class="overdue-stat-card__label">总观看次数</div>
      </div>
      <div class="overdue-stat-card stat-card--used">
        <i class="stat-card-bg-icon el-icon-circle-check"></i>
        <div class="overdue-stat-card__value" style="color:#d97706">{{ statsData.totalUsed || 0 }}</div>
        <div class="overdue-stat-card__label">总使用次数</div>
      </div>
      <div class="overdue-stat-card stat-card--remain">
        <i class="stat-card-bg-icon el-icon-present"></i>
        <div class="overdue-stat-card__value" style="color:#059669">{{ statsData.totalRemaining || 0 }}</div>
        <div class="overdue-stat-card__label">总剩余额度</div>
      </div>
    </div>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="用户昵称" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户昵称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:adWatch:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList">
      <el-table-column label="ID" prop="id" width="70" align="center"/>
      <el-table-column label="用户昵称" prop="userName" show-overflow-tooltip/>
      <el-table-column label="观看次数" width="120" align="center">
        <template slot-scope="scope">
          <span class="num-badge num-badge--blue">{{ scope.row.watchCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="已使用" width="120" align="center">
        <template slot-scope="scope">
          <span class="num-badge num-badge--orange">{{ scope.row.usedCount || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="剩余额度" width="120" align="center">
        <template slot-scope="scope">
          <span class="num-badge num-badge--green">{{ (scope.row.watchCount || 0) - (scope.row.usedCount || 0) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="使用率" width="140">
        <template slot-scope="scope">
          <div class="usage-bar-wrap" v-if="scope.row.watchCount > 0">
            <div class="usage-bar">
              <div class="usage-bar__fill" :style="{ width: getUsagePercent(scope.row) + '%' }"></div>
            </div>
            <span class="usage-bar__text">{{ getUsagePercent(scope.row) }}%</span>
          </div>
          <span v-else style="color:#9ca3af;font-size:12px">-</span>
        </template>
      </el-table-column>
      <el-table-column label="最后观看" prop="lastWatchTime" width="160"/>
      <el-table-column label="操作" width="80" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:adWatch:query']">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="广告观看记录详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ currentRow.userName || currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="观看次数">
          <span class="num-badge num-badge--blue">{{ currentRow.watchCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已使用次数">
          <span class="num-badge num-badge--orange">{{ currentRow.usedCount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="剩余额度">
          <span class="num-badge num-badge--green">{{ (currentRow.watchCount || 0) - (currentRow.usedCount || 0) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="最后观看时间">{{ currentRow.lastWatchTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listAdWatchRecord, getAdWatchRecord, getStatistics } from "@/api/overdue/adWatchRecord";

export default {
  name: "AdWatchRecord",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      recordList: [],
      statsData: {
        totalWatch: 0,
        totalUsed: 0,
        totalRemaining: 0
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userName: null
      },
      detailOpen: false,
      currentRow: null
    };
  },
  created() {
    this.getList();
    this.loadStats();
  },
  methods: {
    getList() {
      this.loading = true;
      listAdWatchRecord(this.queryParams).then(response => {
        this.recordList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      });
    },
    loadStats() {
      getStatistics().then(response => {
        if (response && response.data) {
          this.statsData = response.data;
        }
      }).catch(function() {});
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleView(row) {
      this.currentRow = row;
      this.detailOpen = true;
    },
    handleExport() {
      this.$download.excel('/item/adWatch/export', this.queryParams, '广告观看记录_' + new Date().getTime() + '.xlsx');
    },
    getUsagePercent(row) {
      if (!row.watchCount) return 0;
      return Math.round((row.usedCount || 0) / row.watchCount * 100);
    }
  }
};
</script>

<style scoped>
/* 统计卡片背景图标 */
.overdue-stat-card { position: relative; overflow: hidden; }
.stat-card-bg-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 56px;
  opacity: .06;
  transform: rotate(-15deg);
}
.stat-card--watch::before { background: linear-gradient(90deg, #2563eb, #60a5fa); }
.stat-card--used::before  { background: linear-gradient(90deg, #d97706, #fbbf24); }
.stat-card--remain::before { background: linear-gradient(90deg, #059669, #34d399); }

/* 数字徽章 */
.num-badge {
  display: inline-block;
  font-size: 14px;
  font-weight: 800;
  padding: 2px 10px;
  border-radius: 8px;
  min-width: 36px;
  text-align: center;
}
.num-badge--blue   { background: #eff6ff; color: #2563eb; }
.num-badge--orange { background: #fffbeb; color: #d97706; }
.num-badge--green  { background: #ecfdf5; color: #059669; }

/* 使用率条 */
.usage-bar-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.usage-bar {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: #f3f4f6;
  overflow: hidden;
}
.usage-bar__fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #6366f1, #a78bfa);
  transition: width .6s ease;
}
.usage-bar__text {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  min-width: 32px;
}
</style>
