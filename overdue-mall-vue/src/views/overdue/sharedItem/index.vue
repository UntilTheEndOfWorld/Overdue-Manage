<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="物品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入物品名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="空间ID" prop="spaceId">
        <el-input v-model="queryParams.spaceId" placeholder="请输入空间ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="已删除" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['item:sharedItem:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="物品名称" prop="name" show-overflow-tooltip/>
      <el-table-column label="所属空间" prop="spaceId" width="100"/>
      <el-table-column label="分类" prop="category" width="100"/>
      <el-table-column label="过期日期" prop="expiryDate" width="120">
        <template slot-scope="scope">
          <span :class="getExpiryClass(scope.row.expiryDate)">{{ scope.row.expiryDate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="quantity" width="80"/>
      <el-table-column label="添加者" prop="addedBy" width="100"/>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已删除' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:sharedItem:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['item:sharedItem:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="物品详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="所属空间">{{ currentRow.spaceId }}</el-descriptions-item>
        <el-descriptions-item label="物品名称">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentRow.category }}</el-descriptions-item>
        <el-descriptions-item label="过期日期" :span="2">
          <span :class="getExpiryClass(currentRow.expiryDate)">{{ currentRow.expiryDate }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="数量">{{ currentRow.quantity }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentRow.unit }}</el-descriptions-item>
        <el-descriptions-item label="添加者">{{ currentRow.addedBy }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'info'" size="small">
            {{ currentRow.status === '0' ? '正常' : '已删除' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime }}</el-descriptions-item>
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
      ids: [],
      multiple: true,
      total: 0,
      itemList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        spaceId: null,
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
      listSharedItem(this.queryParams).then(response => {
        this.itemList = response.rows;
        this.total = response.total;
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    handleView(row) {
      this.currentRow = row;
      this.detailOpen = true;
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除该物品？').then(() => {
        return delSharedItem(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    getExpiryClass(expiryDate) {
      if (!expiryDate) return '';
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const expiry = new Date(expiryDate);
      const diffDays = Math.ceil((expiry - today) / (1000 * 60 * 60 * 24));
      if (diffDays < 0) return 'text-danger';
      if (diffDays <= 7) return 'text-warning';
      return '';
    }
  }
};
</script>

<style scoped>
.text-danger { color: #F56C6C; font-weight: bold; }
.text-warning { color: #E6A23C; font-weight: bold; }
</style>
