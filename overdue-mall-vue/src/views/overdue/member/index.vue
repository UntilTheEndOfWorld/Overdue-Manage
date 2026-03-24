<template>
  <div class="app-container">
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #3B82F6;"><i class="el-icon-star-on"></i></div>
      <div class="overdue-page-header__text"><h2>会员管理</h2><p>查看与编辑会员信息及套餐</p></div>
    </div>
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="套餐类型" prop="planType">
        <el-select v-model="queryParams.planType" placeholder="请选择" clearable size="small">
          <el-option label="月度" value="monthly"/>
          <el-option label="季度" value="quarterly"/>
          <el-option label="年度" value="yearly"/>
          <el-option label="终身" value="lifetime"/>
        </el-select>
      </el-form-item>
      <el-form-item label="会员状态" prop="isMember">
        <el-select v-model="queryParams.isMember" placeholder="请选择" clearable size="small">
          <el-option label="是" :value="1"/>
          <el-option label="否" :value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="已过期" value="1"/>
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
      <el-table-column label="用户昵称" prop="userName" show-overflow-tooltip/>
      <el-table-column label="套餐类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="getPlanTagType(scope.row.planType)" size="mini">{{ getPlanLabel(scope.row.planType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="是否会员" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.isMember === 1 ? 'success' : 'info'" size="mini">
            {{ scope.row.isMember === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="购买时间" prop="purchaseTime" width="160"/>
      <el-table-column label="到期时间" width="160">
        <template slot-scope="scope">
          <span v-if="!scope.row.expireTime" style="color:#E6A23C">终身</span>
          <span v-else :style="getExpireStyle(scope.row.expireTime)">{{ scope.row.expireTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已过期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:member:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['item:member:edit']">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="会员详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ currentRow.userName || currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="套餐类型">
          <el-tag :type="getPlanTagType(currentRow.planType)" size="mini">{{ getPlanLabel(currentRow.planType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否会员">{{ currentRow.isMember === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="购买时间">{{ currentRow.purchaseTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="到期时间">{{ currentRow.expireTime || '终身' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'danger'" size="small">
            {{ currentRow.status === '0' ? '正常' : '已过期' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog title="编辑会员" :visible.sync="editOpen" width="500px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="用户">{{ editForm.userName }}</el-form-item>
        <el-form-item label="套餐类型" prop="planType">
          <el-select v-model="editForm.planType" placeholder="请选择套餐类型">
            <el-option label="月度" value="monthly"/>
            <el-option label="季度" value="quarterly"/>
            <el-option label="年度" value="yearly"/>
            <el-option label="终身" value="lifetime"/>
          </el-select>
        </el-form-item>
        <el-form-item label="到期时间" prop="expireTime" v-if="editForm.planType !== 'lifetime'">
          <el-date-picker v-model="editForm.expireTime" type="datetime" placeholder="选择到期时间" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">已过期</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEdit">确 定</el-button>
        <el-button @click="editOpen = false">取 消</el-button>
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
        planType: null,
        isMember: null,
        status: null
      },
      detailOpen: false,
      currentRow: null,
      editOpen: false,
      editForm: {
        id: null,
        userName: '',
        planType: '',
        expireTime: '',
        status: ''
      },
      editRules: {
        planType: [{ required: true, message: '请选择套餐类型', trigger: 'change' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listMember(this.queryParams).then(response => {
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
    handleView(row) {
      this.currentRow = row;
      this.detailOpen = true;
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        userName: row.userName || row.userId,
        planType: row.planType,
        expireTime: row.expireTime,
        status: row.status
      };
      this.editOpen = true;
    },
    submitEdit() {
      var self = this;
      this.$refs.editForm.validate(function(valid) {
        if (!valid) return;
        var data = {
          id: self.editForm.id,
          planType: self.editForm.planType,
          expireTime: self.editForm.planType === 'lifetime' ? null : self.editForm.expireTime,
          status: self.editForm.status
        };
        updateMember(data).then(function() {
          self.$modal.msgSuccess('修改成功');
          self.editOpen = false;
          self.getList();
        });
      });
    },
    getPlanTagType(type) {
      if (type === 'lifetime') return 'danger';
      if (type === 'yearly') return 'warning';
      if (type === 'quarterly') return '';
      return 'info';
    },
    getPlanLabel(type) {
      if (type === 'monthly') return '月度';
      if (type === 'quarterly') return '季度';
      if (type === 'yearly') return '年度';
      if (type === 'lifetime') return '终身';
      return type || '-';
    },
    getExpireStyle(expireTime) {
      if (!expireTime) return {};
      var now = new Date();
      var expire = new Date(expireTime);
      var diffDays = Math.ceil((expire - now) / (1000 * 60 * 60 * 24));
      if (diffDays < 0) return { color: '#F56C6C', fontWeight: 'bold' };
      if (diffDays <= 7) return { color: '#E6A23C', fontWeight: 'bold' };
      return {};
    }
  }
};
</script>
