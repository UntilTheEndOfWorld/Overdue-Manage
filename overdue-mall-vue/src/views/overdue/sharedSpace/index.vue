<template>
  <div class="app-container">
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #D97706;"><i class="el-icon-office-building"></i></div>
      <div class="overdue-page-header__text"><h2>共享空间管理</h2><p>管理共享空间及其成员与物品</p></div>
    </div>
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="空间名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入空间名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="已解散" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:space:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="spaceList" stripe>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="空间名称" prop="name" show-overflow-tooltip/>
      <el-table-column label="描述" prop="description" show-overflow-tooltip/>
      <el-table-column label="创建者" width="120" show-overflow-tooltip>
        <template slot-scope="scope">{{ scope.row.creatorName || scope.row.creatorId }}</template>
      </el-table-column>
      <el-table-column label="成员数" prop="memberCount" width="80" align="center"/>
      <el-table-column label="物品数" prop="itemCount" width="80" align="center"/>
      <el-table-column label="邀请码" width="120" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.inviteCode">{{ scope.row.inviteCode }}</span>
          <span v-else style="color:#C0C4CC">无</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已解散' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:space:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['item:space:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 - 包含成员列表和物品列表 -->
    <el-dialog title="空间详情" :visible.sync="detailOpen" width="800px" append-to-body>
      <el-descriptions :column="2" border v-if="currentRow" style="margin-bottom:20px">
        <el-descriptions-item label="空间名称">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ currentRow.creatorName || currentRow.creatorId }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentRow.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邀请码">{{ currentRow.inviteCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邀请码过期时间">{{ currentRow.inviteCodeExpireTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ currentRow.memberCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="物品数">{{ currentRow.itemCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'info'" size="small">
            {{ currentRow.status === '0' ? '正常' : '已解散' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs v-model="detailTab">
        <el-tab-pane label="成员列表" name="members">
          <el-table :data="detailMembers" border size="small" v-loading="detailLoading">
            <el-table-column label="用户昵称" prop="userName" show-overflow-tooltip/>
            <el-table-column label="角色" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getRoleTagType(scope.row.role)" size="mini">{{ getRoleLabel(scope.row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="加入时间" prop="joinTime" width="160"/>
            <el-table-column label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="mini">
                  {{ scope.row.status === '0' ? '正常' : '已退出' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="物品列表" name="items">
          <el-table :data="detailItems" border size="small" v-loading="detailLoading">
            <el-table-column label="物品名称" prop="name" show-overflow-tooltip/>
            <el-table-column label="分类" prop="category" width="90" align="center"/>
            <el-table-column label="过期日期" prop="expiryDate" width="120"/>
            <el-table-column label="过期状态" width="100" align="center">
              <template slot-scope="scope">
                <el-tag :type="getExpiryTagType(scope.row.expiryDate)" size="mini">
                  {{ getExpiryLabel(scope.row.expiryDate) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建者" prop="creatorName" width="120"/>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { listSharedSpace, getSharedSpace, delSharedSpace } from "@/api/overdue/sharedSpace";
import { listBySpace as listMembersBySpace } from "@/api/overdue/spaceMember";
import { listBySpace as listItemsBySpace } from "@/api/overdue/sharedItem";

export default {
  name: "SharedSpace",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      spaceList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        status: null
      },
      detailOpen: false,
      detailTab: "members",
      detailLoading: false,
      detailMembers: [],
      detailItems: [],
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
      listSharedSpace(params).then(response => {
        this.spaceList = response.rows || [];
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
      this.detailTab = "members";
      this.loadDetailData(row.id);
    },
    loadDetailData(spaceId) {
      this.detailLoading = true;
      this.detailMembers = [];
      this.detailItems = [];
      var self = this;
      Promise.all([
        listMembersBySpace(spaceId).catch(function() { return { data: [] }; }),
        listItemsBySpace(spaceId).catch(function() { return { data: [] }; })
      ]).then(function(results) {
        self.detailMembers = results[0].data || results[0].rows || [];
        self.detailItems = results[1].data || results[1].rows || [];
        self.detailLoading = false;
      });
    },
    handleDelete(row) {
      this.$confirm('确认要删除空间"' + row.name + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return delSharedSpace(row.id);
      }).then(function() {
        this.getList();
        this.$modal.msgSuccess('删除成功');
      }.bind(this)).catch(function() {});
    },
    handleExport() {
      this.$download.excel('/item/space/export', this.queryParams, '共享空间_' + new Date().getTime() + '.xlsx');
    },
    getRoleTagType(role) {
      if (role === 'creator') return 'danger';
      if (role === 'admin') return 'warning';
      return '';
    },
    getRoleLabel(role) {
      if (role === 'creator') return '创建者';
      if (role === 'admin') return '管理员';
      return '成员';
    },
    getExpiryTagType(expiryDate) {
      if (!expiryDate) return 'info';
      var now = new Date();
      var expiry = new Date(expiryDate);
      var diffDays = Math.ceil((expiry - now) / (1000 * 60 * 60 * 24));
      if (diffDays < 0) return 'danger';
      if (diffDays <= 7) return 'warning';
      return 'success';
    },
    getExpiryLabel(expiryDate) {
      if (!expiryDate) return '未知';
      var now = new Date();
      var expiry = new Date(expiryDate);
      var diffDays = Math.ceil((expiry - now) / (1000 * 60 * 60 * 24));
      if (diffDays < 0) return '已过期';
      if (diffDays <= 7) return '即将过期';
      return '正常';
    }
  }
};
</script>
