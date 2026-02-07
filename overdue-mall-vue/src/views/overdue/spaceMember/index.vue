<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="空间ID" prop="spaceId">
        <el-input v-model="queryParams.spaceId" placeholder="请输入空间ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="queryParams.role" placeholder="请选择" clearable size="small">
          <el-option label="创建者" value="creator"/>
          <el-option label="管理员" value="admin"/>
          <el-option label="成员" value="member"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
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
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['item:spaceMember:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="memberList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="空间ID" prop="spaceId" width="100"/>
      <el-table-column label="用户ID" prop="userId" width="100"/>
      <el-table-column label="角色" prop="role" width="100">
        <template slot-scope="scope">
          <el-tag :type="getRoleType(scope.row.role)" size="small">
            {{ getRoleName(scope.row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="加入时间" prop="joinTime" width="160"/>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已退出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['item:spaceMember:edit']">编辑角色</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['item:spaceMember:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 编辑角色对话框 -->
    <el-dialog title="编辑角色" :visible.sync="editOpen" width="400px" append-to-body>
      <el-form ref="editForm" :model="editForm" label-width="80px">
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"/>
            <el-option label="成员" value="member"/>
          </el-select>
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
import { listSpaceMember, updateSpaceMember, delSpaceMember } from "@/api/overdue/spaceMember";

export default {
  name: "SpaceMember",
  data() {
    return {
      loading: true,
      showSearch: true,
      ids: [],
      multiple: true,
      total: 0,
      memberList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        spaceId: null,
        userId: null,
        role: null,
        status: null
      },
      editOpen: false,
      editForm: {
        id: null,
        role: null
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
        this.memberList = response.rows;
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
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        role: row.role
      };
      this.editOpen = true;
    },
    submitEdit() {
      updateSpaceMember(this.editForm).then(() => {
        this.$modal.msgSuccess("修改成功");
        this.editOpen = false;
        this.getList();
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除该成员记录？').then(() => {
        return delSpaceMember(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    getRoleType(role) {
      const types = { creator: 'danger', admin: 'warning', member: 'info' };
      return types[role] || 'info';
    },
    getRoleName(role) {
      const names = { creator: '创建者', admin: '管理员', member: '成员' };
      return names[role] || role;
    }
  }
};
</script>
