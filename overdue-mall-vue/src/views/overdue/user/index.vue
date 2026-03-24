<template>
  <div class="app-container">
    <!-- 页头 -->
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #1E40AF;">
        <i class="el-icon-user-solid"></i>
      </div>
      <div class="overdue-page-header__text">
        <h2>用户管理</h2>
        <p>管理小程序注册用户，查看用户信息及状态</p>
      </div>
    </div>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable size="small"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="禁用" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-date-picker v-model="dateRange" size="small" type="daterange" value-format="yyyy-MM-dd"
          start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:user:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList" stripe>
      <el-table-column label="ID" prop="id" width="70" align="center"/>
      <el-table-column label="用户信息" min-width="200">
        <template slot-scope="scope">
          <div class="user-cell">
            <el-avatar :src="scope.row.avatarUrl" :size="36" v-if="scope.row.avatarUrl"/>
            <el-avatar :size="36" icon="el-icon-user-solid" v-else/>
            <div class="user-cell__info">
              <div class="user-cell__name">{{ scope.row.nickname || '-' }}</div>
              <div class="user-cell__sub">{{ scope.row.phone || 'OpenID: ' + (scope.row.openid || '').substring(0, 10) + '...' }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="70" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.gender === 1" class="gender-tag gender-tag--male"><i class="el-icon-male"></i> 男</span>
          <span v-else-if="scope.row.gender === 2" class="gender-tag gender-tag--female"><i class="el-icon-female"></i> 女</span>
          <span v-else class="gender-tag">未知</span>
        </template>
      </el-table-column>
      <el-table-column label="地区" show-overflow-tooltip>
        <template slot-scope="scope">{{ formatRegion(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="会员" width="80" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.isMember === 1" class="vip-badge">VIP</span>
          <el-tag v-else type="info" size="mini">免费</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small" effect="light">
            {{ scope.row.status === '0' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:user:query']">详情</el-button>
          <el-button size="mini" type="text" @click="handleStatusChange(scope.row)" v-hasPermi="['item:user:edit']">
            <span v-if="scope.row.status === '0'" style="color:#dc2626">禁用</span>
            <span v-else style="color:#059669">启用</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="用户详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <div class="user-detail-header" v-if="currentRow">
        <el-avatar :src="currentRow.avatarUrl" :size="56" v-if="currentRow.avatarUrl"/>
        <el-avatar :size="56" icon="el-icon-user-solid" v-else/>
        <div class="user-detail-header__info">
          <div class="user-detail-header__name">{{ currentRow.nickname || '-' }}</div>
          <div class="user-detail-header__meta">
            <span v-if="currentRow.isMember === 1" class="vip-badge" style="font-size:11px">VIP</span>
            <el-tag :type="currentRow.status === '0' ? 'success' : 'danger'" size="mini">
              {{ currentRow.status === '0' ? '正常' : '禁用' }}
            </el-tag>
          </div>
        </div>
      </div>
      <el-descriptions :column="2" border v-if="currentRow" style="margin-top:16px">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentRow.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="OpenID" :span="2"><code style="font-size:12px;color:#6366f1">{{ currentRow.openid }}</code></el-descriptions-item>
        <el-descriptions-item label="UnionID" :span="2"><code style="font-size:12px;color:#6366f1">{{ currentRow.unionid || '-' }}</code></el-descriptions-item>
        <el-descriptions-item label="性别">
          <span v-if="currentRow.gender === 1">男</span>
          <span v-else-if="currentRow.gender === 2">女</span>
          <span v-else>未知</span>
        </el-descriptions-item>
        <el-descriptions-item label="地区">{{ formatRegion(currentRow) }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, getUser, updateUser } from "@/api/overdue/user";

export default {
  name: "OverdueUser",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      userList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nickname: null,
        phone: null,
        status: null
      },
      detailOpen: false,
      currentRow: null
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      var params = Object.assign({}, this.queryParams);
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0];
        params.endTime = this.dateRange[1];
      }
      listUser(params).then(response => {
        this.userList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleView(row) {
      this.currentRow = row;
      this.detailOpen = true;
    },
    handleStatusChange(row) {
      var newStatus = row.status === '0' ? '1' : '0';
      var text = newStatus === '0' ? '启用' : '禁用';
      this.$confirm('确认要' + text + '用户"' + row.nickname + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return updateUser({ id: row.id, status: newStatus });
      }).then(function() {
        row.status = newStatus;
        this.$modal.msgSuccess(text + '成功');
      }.bind(this)).catch(function() {});
    },
    handleExport() {
      this.$download.excel('/item/user/export', this.queryParams, '用户数据_' + new Date().getTime() + '.xlsx');
    },
    formatRegion(row) {
      if (!row) return '-';
      var parts = [row.country, row.province, row.city].filter(Boolean);
      return parts.length > 0 ? parts.join(' ') : '-';
    }
  }
};
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-cell__name {
  font-weight: 600;
  color: #1f2937;
  font-size: 13px;
  line-height: 1.3;
}
.user-cell__sub {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 1px;
}
.gender-tag {
  font-size: 12px;
  color: #6b7280;
}
.gender-tag--male { color: #2563eb; }
.gender-tag--female { color: #ec4899; }
.vip-badge {
  display: inline-block;
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 10px;
  letter-spacing: 1px;
  box-shadow: 0 2px 6px rgba(245, 158, 11, .35);
}
.user-detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}
.user-detail-header__name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}
.user-detail-header__meta {
  display: flex;
  gap: 8px;
  margin-top: 4px;
  align-items: center;
}
</style>
