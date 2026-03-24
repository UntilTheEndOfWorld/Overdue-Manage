<template>
  <div class="app-container">
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #7C3AED;"><i class="el-icon-s-custom"></i></div>
      <div class="overdue-page-header__text"><h2>空间成员管理</h2><p>查看与管理共享空间的成员角色</p></div>
    </div>
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="空间名称" prop="spaceName">
        <el-input v-model="queryParams.spaceName" placeholder="请输入空间名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="queryParams.role" placeholder="请选择角色" clearable size="small">
          <el-option label="创建者" value="creator"/>
          <el-option label="管理员" value="admin"/>
          <el-option label="成员" value="member"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="已退出" value="1"/>
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

    <el-table v-loading="loading" :data="memberList" stripe>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="空间名称" prop="spaceName" show-overflow-tooltip/>
      <el-table-column label="用户昵称" prop="userName" show-overflow-tooltip/>
      <el-table-column label="角色" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getRoleTagType(scope.row.role)" size="mini">{{ getRoleLabel(scope.row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="加入时间" prop="joinTime" width="160"/>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已退出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEditRole(scope.row)"
            v-hasPermi="['item:spaceMember:edit']" v-if="scope.row.role !== 'creator' && scope.row.status === '0'">修改角色</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#F56C6C" @click="handleRemove(scope.row)"
            v-hasPermi="['item:spaceMember:remove']" v-if="scope.row.role !== 'creator' && scope.row.status === '0'">移除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 修改角色对话框 -->
    <el-dialog title="修改成员角色" :visible.sync="roleOpen" width="400px" append-to-body>
      <el-form ref="roleForm" :model="roleForm" label-width="80px">
        <el-form-item label="用户">{{ roleForm.userName }}</el-form-item>
        <el-form-item label="当前角色">
          <el-tag :type="getRoleTagType(roleForm.currentRole)" size="mini">{{ getRoleLabel(roleForm.currentRole) }}</el-tag>
        </el-form-item>
        <el-form-item label="新角色" prop="role">
          <el-select v-model="roleForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"/>
            <el-option label="成员" value="member"/>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRoleChange">确 定</el-button>
        <el-button @click="roleOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSpaceMember, updateSpaceMember, delSpaceMember } from "@/api/overdue/spaceMember";

export default {
  name: "SpaceMember",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      memberList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        spaceName: null,
        role: null,
        status: null
      },
      roleOpen: false,
      roleForm: {
        id: null,
        userName: '',
        currentRole: '',
        role: ''
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listSpaceMember(this.queryParams).then(response => {
        this.memberList = response.rows || [];
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
    handleEditRole(row) {
      this.roleForm = {
        id: row.id,
        userName: row.userName || row.userId,
        currentRole: row.role,
        role: row.role
      };
      this.roleOpen = true;
    },
    submitRoleChange() {
      if (this.roleForm.role === this.roleForm.currentRole) {
        this.$modal.msgWarning('角色未变更');
        return;
      }
      var self = this;
      updateSpaceMember({ id: this.roleForm.id, role: this.roleForm.role }).then(function() {
        self.$modal.msgSuccess('修改成功');
        self.roleOpen = false;
        self.getList();
      });
    },
    handleRemove(row) {
      this.$confirm('确认要移除成员"' + (row.userName || row.userId) + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return delSpaceMember(row.id);
      }).then(function() {
        this.getList();
        this.$modal.msgSuccess('移除成功');
      }.bind(this)).catch(function() {});
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
    }
  }
};
</script>
