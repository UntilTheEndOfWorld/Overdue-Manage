<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable size="small"/>
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable size="small"/>
      </el-form-item>
      <el-form-item label="套餐类型" prop="planType">
        <el-select v-model="queryParams.planType" placeholder="请选择" clearable size="small">
          <el-option label="月卡" value="monthly"/>
          <el-option label="季卡" value="quarterly"/>
          <el-option label="年卡" value="yearly"/>
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="待支付" value="0"/>
          <el-option label="已支付" value="1"/>
          <el-option label="已取消" value="2"/>
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

    <el-table v-loading="loading" :data="orderList" border>
      <el-table-column label="ID" prop="id" width="80"/>
      <el-table-column label="订单号" prop="orderNo" width="200" show-overflow-tooltip/>
      <el-table-column label="用户ID" prop="userId" width="100"/>
      <el-table-column label="套餐类型" width="100">
        <template slot-scope="scope">
          {{ getPlanName(scope.row.planType) }}
        </template>
      </el-table-column>
      <el-table-column label="金额" width="100">
        <template slot-scope="scope">
          ￥{{ scope.row.amount ? scope.row.amount.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusName(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" prop="payTime" width="160"/>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:order:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-close" @click="handleCancel(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['item:order:edit']">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border v-if="currentRow">
        <el-descriptions-item label="订单ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="套餐类型">{{ getPlanName(currentRow.planType) }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">￥{{ currentRow.amount ? currentRow.amount.toFixed(2) : '0.00' }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(currentRow.status)" size="small">
            {{ getStatusName(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="微信交易号">{{ currentRow.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentRow.payTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentRow.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { listMemberOrder, getMemberOrder, cancelOrder } from "@/api/overdue/memberOrder";

export default {
  name: "MemberOrder",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      orderList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        userId: null,
        planType: null,
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
      listMemberOrder(this.queryParams).then(response => {
        this.orderList = response.rows;
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
    handleCancel(row) {
      this.$modal.confirm('是否确认取消该订单？').then(() => {
        return cancelOrder(row.id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("取消成功");
      }).catch(() => {});
    },
    getPlanName(planType) {
      const names = { monthly: '月卡', quarterly: '季卡', yearly: '年卡' };
      return names[planType] || planType || '-';
    },
    getStatusType(status) {
      const types = { '0': 'warning', '1': 'success', '2': 'info' };
      return types[status] || 'info';
    },
    getStatusName(status) {
      const names = { '0': '待支付', '1': '已支付', '2': '已取消' };
      return names[status] || '未知';
    }
  }
};
</script>
