<template>
  <div class="app-container">
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #059669;"><i class="el-icon-document"></i></div>
      <div class="overdue-page-header__text"><h2>操作日志</h2><p>查看用户对物品的操作记录</p></div>
    </div>
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="操作类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="请选择" clearable size="small">
          <el-option label="新增" value="add"/>
          <el-option label="编辑" value="edit"/>
          <el-option label="删除" value="delete"/>
        </el-select>
      </el-form-item>
      <el-form-item label="物品类型" prop="itemType">
        <el-select v-model="queryParams.itemType" placeholder="请选择" clearable size="small">
          <el-option label="个人物品" value="personal"/>
          <el-option label="共享物品" value="shared"/>
        </el-select>
      </el-form-item>
      <el-form-item label="操作者" prop="operatorName">
        <el-input v-model="queryParams.operatorName" placeholder="请输入操作者" clearable size="small"/>
      </el-form-item>
      <el-form-item label="操作时间">
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
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" stripe>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="操作时间" prop="operationTime" width="160"/>
      <el-table-column label="操作类型" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="getOpTagType(scope.row.operationType)" size="mini">
            {{ getOpLabel(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作者" prop="operatorName" width="120" show-overflow-tooltip/>
      <el-table-column label="物品类型" width="100" align="center">
        <template slot-scope="scope">
          <span v-if="scope.row.itemType === 'personal'">个人物品</span>
          <span v-else-if="scope.row.itemType === 'shared'">共享物品</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="物品ID" prop="itemId" width="80"/>
      <el-table-column label="所属空间" width="120" show-overflow-tooltip>
        <template slot-scope="scope">{{ scope.row.spaceName || (scope.row.spaceId ? scope.row.spaceId : '-') }}</template>
      </el-table-column>
      <el-table-column label="操作描述" prop="operationDesc" show-overflow-tooltip/>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>
  </div>
</template>

<script>
import { listOperationLog } from "@/api/overdue/operationLog";

export default {
  name: "OperationLog",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      logList: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        operationType: null,
        itemType: null,
        operatorName: null
      }
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
      listOperationLog(params).then(response => {
        this.logList = response.rows || [];
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
    handleExport() {
      this.$download.excel('/item/log/export', this.queryParams, '操作日志_' + new Date().getTime() + '.xlsx');
    },
    getOpTagType(type) {
      if (type === 'add') return 'success';
      if (type === 'edit') return '';
      if (type === 'delete') return 'danger';
      return 'info';
    },
    getOpLabel(type) {
      if (type === 'add') return '新增';
      if (type === 'edit') return '编辑';
      if (type === 'delete') return '删除';
      return type || '-';
    }
  }
};
</script>
