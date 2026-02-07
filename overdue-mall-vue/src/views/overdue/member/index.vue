<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="是否会员" prop="isMember">
        <el-select v-model="queryParams.isMember" placeholder="请选择" clearable size="small">
          <el-option label="是" :value="1"/>
          <el-option label="否" :value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item label="套餐类型" prop="planType">
        <el-select v-model="queryParams.planType" placeholder="请选择" clearable size="small">
          <el-option label="月卡" value="monthly"/>
          <el-option label="季卡" value="quarterly"/>
          <el-option label="年卡" value="yearly"/>
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

    <el-table v-loading="loading" :data="memberList" border>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="用户ID" prop="userId" width="100"/>
      <el-table-column label="是否会员" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isMember === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.isMember === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="套餐类型" width="100">
        <template slot-scope="scope">
          {{ getPlanName(scope.row.planType) }}
        </template>
      </el-table-column>
      <el-table-column label="购买时间" prop="purchaseTime" width="160"/>
      <el-table-column label="到期时间" prop="expireTime" width="160">
        <template slot-scope="scope">
          <span :class="getExpireClass(scope.row.expireTime)">{{ scope.row.expireTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:member:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['item:member:edit']">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="会员详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="是否会员">
          <el-tag :type="currentRow.isMember === 1 ? 'success' : 'info'" size="small">
            {{ currentRow.isMember === 1 ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="套餐类型">{{ getPlanName(currentRow.planType) }}</el-descriptions-item>
        <el-descriptions-item label="购买时间">{{ currentRow.purchaseTime }}</el-descriptions-item>
        <el-descriptions-item label="到期时间">
          <span :class="getExpireClass(currentRow.expireTime)">{{ currentRow.expireTime }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'info'" size="small">
            {{ currentRow.status === '0' ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑会员" :visible.sync="editOpen" width="500px" append-to-body>
      <el-form ref="editForm" :model="editForm" label-width="100px">
        <el-form-item label="是否会员">
          <el-switch v-model="editForm.isMember" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="套餐类型">
          <el-select v-model="editForm.planType" placeholder="请选择">
            <el-option label="月卡" value="monthly"/>
            <el-option label="季卡" value="quarterly"/>
            <el-option label="年卡" value="yearly"/>
          </el-select>
        </el-form-item>
        <el-form-item label="到期时间">
          <el-date-picker v-model="editForm.expireTime" type="datetime" placeholder="选择到期时间" value-format="yyyy-MM-dd HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" placeholder="请选择">
            <el-option label="正常" value="0"/>
            <el-option label="禁用" value="1"/>
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
import { listMember, getMember, updateMember } from "@/api/overdue/member";

export default {
  name: "OverdueMember",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      memberList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        isMember: null,
        planType: null
      },
      detailOpen: false,
      currentRow: null,
      editOpen: false,
      editForm: {}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listMember(this.queryParams).then(response => {
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
    handleView(row) {
      this.currentRow = row;
      this.detailOpen = true;
    },
    handleEdit(row) {
      this.editForm = { ...row };
      this.editOpen = true;
    },
    submitEdit() {
      updateMember(this.editForm).then(() => {
        this.$modal.msgSuccess("修改成功");
        this.editOpen = false;
        this.getList();
      });
    },
    getPlanName(planType) {
      const names = { monthly: '月卡', quarterly: '季卡', yearly: '年卡' };
      return names[planType] || planType || '-';
    },
    getExpireClass(expireTime) {
      if (!expireTime) return '';
      const now = new Date();
      const expire = new Date(expireTime);
      if (expire < now) return 'text-danger';
      const diff = (expire - now) / (1000 * 60 * 60 * 24);
      if (diff <= 7) return 'text-warning';
      return '';
    }
  }
};
</script>

<style scoped>
.text-danger { color: #F56C6C; font-weight: bold; }
.text-warning { color: #E6A23C; font-weight: bold; }
</style>
