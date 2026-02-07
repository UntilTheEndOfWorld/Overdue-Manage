<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="空间ID" prop="spaceId">
        <el-input v-model="queryParams.spaceId" placeholder="请输入空间ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="操作人ID" prop="operatorId">
        <el-input v-model="queryParams.operatorId" placeholder="请输入操作人ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="操作类型" prop="operationType">
        <el-select v-model="queryParams.operationType" placeholder="请选择" clearable size="small">
          <el-option label="添加" value="add"/>
          <el-option label="修改" value="update"/>
          <el-option label="删除" value="delete"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logList" border>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="空间ID" prop="spaceId" width="100"/>
      <el-table-column label="物品ID" prop="itemId" width="100"/>
      <el-table-column label="物品类型" prop="itemType" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.itemType === 'personal' ? 'primary' : 'success'" size="small">
            {{ scope.row.itemType === 'personal' ? '个人' : '共享' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作类型" prop="operationType" width="100">
        <template slot-scope="scope">
          <el-tag :type="getOperationType(scope.row.operationType)" size="small">
            {{ getOperationName(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人ID" prop="operatorId" width="100"/>
      <el-table-column label="操作内容" prop="content" show-overflow-tooltip/>
      <el-table-column label="操作时间" prop="createTime" width="160"/>
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
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        spaceId: null,
        operatorId: null,
        operationType: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listOperationLog(this.queryParams).then(response => {
        this.logList = response.rows;
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
    getOperationType(type) {
      const types = { add: 'success', update: 'warning', delete: 'danger' };
      return types[type] || 'info';
    },
    getOperationName(type) {
      const names = { add: '添加', update: '修改', delete: '删除' };
      return names[type] || type;
    }
  }
};
</script>
