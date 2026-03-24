<template>
  <div class="app-container">
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #F59E0B;"><i class="el-icon-tickets"></i></div>
      <div class="overdue-page-header__text"><h2>会员订单</h2><p>管理会员订单及支付记录</p></div>
    </div>
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="套餐类型" prop="planType">
        <el-select v-model="queryParams.planType" placeholder="请选择" clearable size="small">
          <el-option label="月度" value="monthly"/>
          <el-option label="季度" value="quarterly"/>
          <el-option label="年度" value="yearly"/>
          <el-option label="终身" value="lifetime"/>
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择" clearable size="small">
          <el-option label="未支付" value="pending"/>
          <el-option label="已支付" value="paid"/>
          <el-option label="支付失败" value="failed"/>
          <el-option label="已退款" value="refunded"/>
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="0"/>
          <el-option label="已取消" value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:order:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" stripe>
      <el-table-column label="ID" prop="id" width="60"/>
      <el-table-column label="订单号" prop="orderNo" width="200" show-overflow-tooltip/>
      <el-table-column label="用户昵称" prop="userName" show-overflow-tooltip width="120"/>
      <el-table-column label="套餐类型" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="getPlanTagType(scope.row.planType)" size="mini">{{ getPlanLabel(scope.row.planType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="90" align="right">
        <template slot-scope="scope">
          <span style="color:#F56C6C;font-weight:bold">{{ scope.row.price != null ? '¥' + scope.row.price : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" width="90" align="center">
        <template slot-scope="scope">{{ scope.row.paymentMethod === 'wechat' ? '微信支付' : (scope.row.paymentMethod || '-') }}</template>
      </el-table-column>
      <el-table-column label="支付状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag :type="getPayStatusType(scope.row.paymentStatus)" size="mini">
            {{ getPayStatusLabel(scope.row.paymentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="80" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '正常' : '已取消' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160"/>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:order:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-close" style="color:#F56C6C" @click="handleCancel(scope.row)"
            v-hasPermi="['item:order:edit']" v-if="scope.row.status === '0' && scope.row.paymentStatus === 'pending'">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="订单详情" :visible.sync="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="订单号" :span="2">{{ currentRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ currentRow.userName || currentRow.userId }}</el-descriptions-item>
        <el-descriptions-item label="套餐类型">
          <el-tag :type="getPlanTagType(currentRow.planType)" size="mini">{{ getPlanLabel(currentRow.planType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额">
          <span style="color:#F56C6C;font-weight:bold">{{ currentRow.price != null ? '¥' + currentRow.price : '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentRow.paymentMethod === 'wechat' ? '微信支付' : (currentRow.paymentMethod || '-') }}</el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="getPayStatusType(currentRow.paymentStatus)" size="mini">
            {{ getPayStatusLabel(currentRow.paymentStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentRow.paymentTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交易流水号" :span="2">{{ currentRow.transactionId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="currentRow.status === '0' ? 'success' : 'info'" size="small">
            {{ currentRow.status === '0' ? '正常' : '已取消' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
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
        planType: null,
        paymentStatus: null,
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
        this.orderList = response.rows || [];
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
    handleCancel(row) {
      this.$confirm('确认要取消订单"' + row.orderNo + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return cancelOrder(row.id);
      }).then(function() {
        this.getList();
        this.$modal.msgSuccess('取消成功');
      }.bind(this)).catch(function() {});
    },
    handleExport() {
      this.$download.excel('/item/order/export', this.queryParams, '会员订单_' + new Date().getTime() + '.xlsx');
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
    getPayStatusType(status) {
      if (status === 'paid') return 'success';
      if (status === 'failed') return 'danger';
      if (status === 'refunded') return 'warning';
      return 'info';
    },
    getPayStatusLabel(status) {
      if (status === 'pending') return '未支付';
      if (status === 'paid') return '已支付';
      if (status === 'failed') return '支付失败';
      if (status === 'refunded') return '已退款';
      return status || '-';
    }
  }
};
</script>
