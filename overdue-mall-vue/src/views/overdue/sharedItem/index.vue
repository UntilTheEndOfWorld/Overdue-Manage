<template>
  <div class="app-container">
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #7C3AED;"><i class="el-icon-box"></i></div>
      <div class="overdue-page-header__text"><h2>共享物品管理</h2><p>查看共享空间中的物品及过期状态</p></div>
    </div>
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="物品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入物品名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="所属空间" prop="spaceName">
        <el-input v-model="queryParams.spaceName" placeholder="请输入空间名称" clearable size="small"/>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择分类" clearable size="small">
          <el-option label="食品" value="食品"/>
          <el-option label="药品" value="药品"/>
          <el-option label="日用品" value="日用品"/>
        </el-select>
      </el-form-item>
      <el-form-item label="过期状态" prop="expiryStatus">
        <el-select v-model="queryParams.expiryStatus" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="normal"/>
          <el-option label="即将过期" value="expiring"/>
          <el-option label="已过期" value="expired"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:sharedItem:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" stripe>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="物品名称" prop="name" show-overflow-tooltip/>
      <el-table-column label="所属空间" prop="spaceName" show-overflow-tooltip width="140"/>
      <el-table-column label="分类" prop="category" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建者" width="120" show-overflow-tooltip>
        <template slot-scope="scope">{{ scope.row.creatorName || scope.row.creatorId }}</template>
      </el-table-column>
      <el-table-column label="过期日期" prop="expiryDate" width="120"/>
      <el-table-column label="过期状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getExpiryTagType(scope.row.expiryDate)" size="mini">
            {{ getExpiryLabel(scope.row.expiryDate) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:sharedItem:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleDelete(scope.row)" v-hasPermi="['item:sharedItem:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="共享物品详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="物品名称">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="所属空间">{{ currentRow.spaceName || currentRow.spaceId }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.category }}</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ currentRow.creatorName || currentRow.creatorId }}</el-descriptions-item>
        <el-descriptions-item label="购买日期">{{ currentRow.purchaseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产日期">{{ currentRow.productionDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保质期">{{ currentRow.shelfLife }}{{ currentRow.shelfLifeUnit }}</el-descriptions-item>
        <el-descriptions-item label="过期日期">{{ currentRow.expiryDate }}</el-descriptions-item>
        <el-descriptions-item label="过期状态">
          <el-tag :type="getExpiryTagType(currentRow.expiryDate)" size="mini">
            {{ getExpiryLabel(currentRow.expiryDate) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listSharedItem, getSharedItem, delSharedItem } from "@/api/overdue/sharedItem";

export default {
  name: "SharedItem",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      itemList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        spaceName: null,
        category: null,
        expiryStatus: null
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
      listSharedItem(this.queryParams).then(response => {
        this.itemList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      });
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
    handleDelete(row) {
      this.$confirm('确认要删除物品"' + row.name + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return delSharedItem(row.id);
      }).then(function() {
        this.getList();
        this.$modal.msgSuccess('删除成功');
      }.bind(this)).catch(function() {});
    },
    handleExport() {
      this.$download.excel('/item/sharedItem/export', this.queryParams, '共享物品_' + new Date().getTime() + '.xlsx');
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
