<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="空间名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入空间名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建者ID" prop="creatorId">
        <el-input v-model="queryParams.creatorId" placeholder="请输入创建者ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="已解散" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['item:space:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="spaceList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="空间名称" prop="name" show-overflow-tooltip/>
      <el-table-column label="创建者ID" prop="creatorId" width="100"/>
      <el-table-column label="描述" prop="description" show-overflow-tooltip/>
      <el-table-column label="邀请码" prop="inviteCode" width="120"/>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已解散' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:space:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-user" @click="handleViewMembers(scope.row)" v-hasPermi="['item:spaceMember:list']">成员</el-button>
          <el-button size="mini" type="text" icon="el-icon-goods" @click="handleViewItems(scope.row)" v-hasPermi="['item:sharedItem:list']">物品</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="空间详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="空间名称">{{ currentRow.name }}</el-descriptions-item>
        <el-descriptions-item label="创建者ID">{{ currentRow.creatorId }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ currentRow.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邀请码">{{ currentRow.inviteCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'info'" size="small">
            {{ currentRow.status === '0' ? '正常' : '已解散' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 成员列表对话框 -->
    <el-dialog title="空间成员" :visible.sync="memberOpen" width="700px" append-to-body>
      <el-table :data="memberList" border v-loading="memberLoading">
        <el-table-column label="用户ID" prop="userId" width="100"/>
        <el-table-column label="角色" prop="role" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 'creator' ? 'danger' : scope.row.role === 'admin' ? 'warning' : 'info'" size="small">
              {{ scope.row.role === 'creator' ? '创建者' : scope.row.role === 'admin' ? '管理员' : '成员' }}
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
      </el-table>
    </el-dialog>

    <!-- 物品列表对话框 -->
    <el-dialog title="共享物品" :visible.sync="itemOpen" width="800px" append-to-body>
      <el-table :data="itemList" border v-loading="itemLoading">
        <el-table-column label="ID" prop="id" width="80"/>
        <el-table-column label="物品名称" prop="name" show-overflow-tooltip/>
        <el-table-column label="分类" prop="category" width="100"/>
        <el-table-column label="过期日期" prop="expiryDate" width="120"/>
        <el-table-column label="数量" prop="quantity" width="80"/>
        <el-table-column label="添加者ID" prop="addedBy" width="100"/>
        <el-table-column label="创建时间" prop="createTime" width="160"/>
      </el-table>
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
      ids: [],
      multiple: true,
      total: 0,
      spaceList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        creatorId: null,
        status: null
      },
      detailOpen: false,
      currentRow: null,
      memberOpen: false,
      memberList: [],
      memberLoading: false,
      itemOpen: false,
      itemList: [],
      itemLoading: false
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listSharedSpace(this.queryParams).then(response => {
        this.spaceList = response.rows;
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
    handleViewMembers(row) {
      this.memberLoading = true;
      this.memberOpen = true;
      listMembersBySpace(row.id).then(response => {
        this.memberList = response.data || [];
        this.memberLoading = false;
      });
    },
    handleViewItems(row) {
      this.itemLoading = true;
      this.itemOpen = true;
      listItemsBySpace(row.id).then(response => {
        this.itemList = response.data || [];
        this.itemLoading = false;
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除该共享空间？').then(() => {
        return delSharedSpace(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
  }
};
</script>
