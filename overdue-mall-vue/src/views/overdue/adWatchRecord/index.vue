<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>总观看次数</span></div>
          <div class="stat-value">{{ statistics.totalWatchCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>总使用次数</span></div>
          <div class="stat-value">{{ statistics.totalUsedCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div slot="header"><span>剩余额度</span></div>
          <div class="stat-value">{{ statistics.totalRemaining || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyy-MM-dd" size="small"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['item:adWatch:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="recordList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="用户ID" prop="userId" width="100"/>
      <el-table-column label="观看次数" prop="watchCount" width="100"/>
      <el-table-column label="使用次数" prop="usedCount" width="100"/>
      <el-table-column label="剩余额度" width="100">
        <template slot-scope="scope">
          {{ (scope.row.watchCount || 0) - (scope.row.usedCount || 0) }}
        </template>
      </el-table-column>
      <el-table-column label="最后观看时间" prop="lastWatchTime" width="160"/>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:adWatch:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['item:adWatch:edit']">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="广告观看记录详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="观看次数">{{ currentRow.watchCount }}</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ currentRow.usedCount }}</el-descriptions-item>
        <el-descriptions-item label="剩余额度">{{ (currentRow.watchCount || 0) - (currentRow.usedCount || 0) }}</el-descriptions-item>
        <el-descriptions-item label="最后观看时间">{{ currentRow.lastWatchTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑广告记录" :visible.sync="editOpen" width="400px" append-to-body>
      <el-form ref="editForm" :model="editForm" label-width="100px">
        <el-form-item label="观看次数">
          <el-input-number v-model="editForm.watchCount" :min="0"/>
        </el-form-item>
        <el-form-item label="使用次数">
          <el-input-number v-model="editForm.usedCount" :min="0"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAdWatchRecord, getAdWatchRecord, updateAdWatchRecord, delAdWatchRecord, getStatistics } from "@/api/overdue/adWatchRecord";

export default {
  name: "AdWatchRecord",
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      recordList: [],
      statistics: {},
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null
      },
      detailOpen: false,
      currentRow: null,
      editOpen: false,
      editForm: {}
    };
  },
  created() {
    this.getList();
    this.loadStatistics();
  },
  methods: {
    getList() {
      this.loading = true;
      const params = { ...this.queryParams };
      if (this.dateRange && this.dateRange.length === 2) {
        params['params[beginTime]'] = this.dateRange[0];
        params['params[endTime]'] = this.dateRange[1];
      }
      listAdWatchRecord(params).then(response => {
        this.recordList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    loadStatistics() {
      getStatistics().then(response => {
        this.statistics = response.data || {};
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.dateRange = [];
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
    handleEdit(row) {
      this.editForm = { ...row };
      this.editOpen = true;
    },
    submitEdit() {
      updateAdWatchRecord(this.editForm).then(() => {
        this.$modal.msgSuccess("修改成功");
        this.editOpen = false;
        this.getList();
        this.loadStatistics();
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除该记录？').then(() => {
        return delAdWatchRecord(ids);
      }).then(() => {
        this.getList();
        this.loadStatistics();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
.mb20 { margin-bottom: 20px; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; text-align: center; }
</style>
