<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入昵称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="禁用" value="1"/>
        </el-select>
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

    <el-table v-loading="loading" :data="userList" border>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="头像" width="80">
        <template slot-scope="scope">
          <el-avatar :src="scope.row.avatarUrl" size="small" v-if="scope.row.avatarUrl"/>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="昵称" prop="nickname" show-overflow-tooltip/>
      <el-table-column label="OpenID" prop="openid" show-overflow-tooltip width="200"/>
      <el-table-column label="性别" prop="gender" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.gender === 1">男</span>
          <span v-else-if="scope.row.gender === 2">女</span>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column label="地区" show-overflow-tooltip>
        <template slot-scope="scope">
          {{ [scope.row.country, scope.row.province, scope.row.city].filter(Boolean).join(' ') || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
            {{ scope.row.status === '0' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="120" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:user:query']">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="用户详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentRow.nickname }}</el-descriptions-item>
        <el-descriptions-item label="OpenID">{{ currentRow.openid }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <span v-if="currentRow.gender === 1">男</span>
          <span v-else-if="currentRow.gender === 2">女</span>
          <span v-else>未知</span>
        </el-descriptions-item>
        <el-descriptions-item label="地区">{{ [currentRow.country, currentRow.province, currentRow.city].filter(Boolean).join(' ') || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'danger'" size="small">
            {{ currentRow.status === '0' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listUser, getUser } from "@/api/overdue/user";

export default {
  name: "OverdueUser",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      userList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nickname: null,
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
      listUser(this.queryParams).then(response => {
        this.userList = response.rows;
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
    handleView(row) {
      this.currentRow = row;
      this.detailOpen = true;
    },
    handleExport() {
      this.$download.excel('/item/user/export', this.queryParams, `用户数据_${new Date().getTime()}.xlsx`);
    }
  }
};
</script>
