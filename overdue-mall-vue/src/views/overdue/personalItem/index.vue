<template>
  <div class="app-container">
    <!-- 页头 -->
    <div class="overdue-page-header">
      <div class="overdue-page-header__icon" style="background: #059669;">
        <i class="el-icon-goods"></i>
      </div>
      <div class="overdue-page-header__text">
        <h2>个人物品管理</h2>
        <p>查看用户添加的个人物品，监控过期状态</p>
      </div>
    </div>

    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="80px" size="medium">
      <el-form-item label="物品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入物品名称" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="请选择分类" clearable size="small">
          <el-option label="食品" value="食品"/>
          <el-option label="药品" value="药品"/>
          <el-option label="日用品" value="日用品"/>
        </el-select>
      </el-form-item>
      <el-form-item label="过期状态" prop="expiryStatus">
        <el-select v-model="queryParams.expiryStatus" placeholder="请选择" clearable size="small">
          <el-option label="正常" value="normal"/>
          <el-option label="即将过期" value="expiring"/>
          <el-option label="已过期" value="expired"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['item:personal:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="itemList" :row-class-name="tableRowClassName">
      <el-table-column label="ID" prop="id" width="70" align="center"/>
      <el-table-column label="物品名称" prop="name" show-overflow-tooltip min-width="140">
        <template slot-scope="scope">
          <span class="item-name">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" prop="category" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="getCategoryType(scope.row.category)">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="所属用户" width="120" show-overflow-tooltip>
        <template slot-scope="scope">{{ scope.row.userName || scope.row.userId }}</template>
      </el-table-column>
      <el-table-column label="生产日期" prop="productionDate" width="110"/>
      <el-table-column label="保质期" width="90" align="center">
        <template slot-scope="scope">
          <span class="shelf-life">{{ scope.row.shelfLife }}{{ scope.row.shelfLifeUnit }}</span>
        </template>
      </el-table-column>
      <el-table-column label="过期日期" width="110">
        <template slot-scope="scope">
          <span :class="getExpiryDateClass(scope.row.expiryDate)">{{ scope.row.expiryDate }}</span>
        </template>
      </el-table-column>
      <el-table-column label="过期状态" width="100" align="center">
        <template slot-scope="scope">
          <span :class="'expiry-pill expiry-pill--' + getExpiryKey(scope.row.expiryDate)">
            <i :class="getExpiryIcon(scope.row.expiryDate)"></i>
            {{ getExpiryLabel(scope.row.expiryDate) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['item:personal:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" style="color:#dc2626" @click="handleDelete(scope.row)" v-hasPermi="['item:personal:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 详情对话框 -->
    <el-dialog title="物品详情" :visible.sync="detailOpen" width="520px" append-to-body>
      <div v-if="currentRow">
        <div class="detail-top-bar" :class="'detail-top-bar--' + getExpiryKey(currentRow.expiryDate)">
          <i :class="getExpiryIcon(currentRow.expiryDate)"></i>
          <span>{{ getExpiryLabel(currentRow.expiryDate) }}</span>
          <span v-if="getDaysLeft(currentRow.expiryDate) !== null" class="detail-days">
            {{ getDaysLeft(currentRow.expiryDate) >= 0 ? '剩余 ' + getDaysLeft(currentRow.expiryDate) + ' 天' : '已过期 ' + Math.abs(getDaysLeft(currentRow.expiryDate)) + ' 天' }}
          </span>
        </div>
        <el-descriptions :column="1" border style="margin-top:16px">
          <el-descriptions-item label="物品名称">{{ currentRow.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">
            <el-tag size="mini" :type="getCategoryType(currentRow.category)">{{ currentRow.category }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属用户">{{ currentRow.userName || currentRow.userId }}</el-descriptions-item>
          <el-descriptions-item label="购买日期">{{ currentRow.purchaseDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="生产日期">{{ currentRow.productionDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="保质期">{{ currentRow.shelfLife }}{{ currentRow.shelfLifeUnit }}</el-descriptions-item>
          <el-descriptions-item label="过期日期">{{ currentRow.expiryDate }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPersonalItem, getPersonalItem, delPersonalItem } from "@/api/overdue/personalItem";

export default {
  name: "PersonalItem",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      itemList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        category: null,
        expiryStatus: null
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
      listPersonalItem(this.queryParams).then(response => {
        this.itemList = response.rows || [];
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
    handleDelete(row) {
      this.$confirm('确认要删除物品"' + row.name + '"吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return delPersonalItem(row.id);
      }).then(function() {
        this.getList();
        this.$modal.msgSuccess('删除成功');
      }.bind(this)).catch(function() {});
    },
    handleExport() {
      this.$download.excel('/item/personal/export', this.queryParams, '个人物品_' + new Date().getTime() + '.xlsx');
    },
    getDiffDays(expiryDate) {
      if (!expiryDate) return null;
      var now = new Date();
      var expiry = new Date(expiryDate);
      return Math.ceil((expiry - now) / (1000 * 60 * 60 * 24));
    },
    getDaysLeft(expiryDate) {
      return this.getDiffDays(expiryDate);
    },
    getExpiryKey(expiryDate) {
      var d = this.getDiffDays(expiryDate);
      if (d === null) return 'unknown';
      if (d < 0) return 'expired';
      if (d <= 7) return 'expiring';
      return 'normal';
    },
    getExpiryTagType(expiryDate) {
      var k = this.getExpiryKey(expiryDate);
      if (k === 'expired') return 'danger';
      if (k === 'expiring') return 'warning';
      return 'success';
    },
    getExpiryLabel(expiryDate) {
      var k = this.getExpiryKey(expiryDate);
      if (k === 'expired') return '已过期';
      if (k === 'expiring') return '即将过期';
      if (k === 'normal') return '正常';
      return '未知';
    },
    getExpiryIcon(expiryDate) {
      var k = this.getExpiryKey(expiryDate);
      if (k === 'expired') return 'el-icon-circle-close';
      if (k === 'expiring') return 'el-icon-warning';
      return 'el-icon-circle-check';
    },
    getExpiryDateClass(expiryDate) {
      var k = this.getExpiryKey(expiryDate);
      if (k === 'expired') return 'date-expired';
      if (k === 'expiring') return 'date-expiring';
      return '';
    },
    getCategoryType(cat) {
      if (cat === '食品') return '';
      if (cat === '药品') return 'danger';
      if (cat === '日用品') return 'warning';
      return 'info';
    },
    tableRowClassName(obj) {
      if (!obj || !obj.row) return '';
      var k = this.getExpiryKey(obj.row.expiryDate);
      if (k === 'expired') return 'row-expired';
      if (k === 'expiring') return 'row-expiring';
      return '';
    }
  }
};
</script>

<style scoped>
.item-name {
  font-weight: 600;
  color: #1f2937;
}
.shelf-life {
  font-weight: 600;
  color: #6366f1;
  font-size: 12px;
}
.date-expired {
  color: #dc2626;
  font-weight: 700;
}
.date-expiring {
  color: #d97706;
  font-weight: 600;
}

/* 过期状态胶囊 */
.expiry-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 12px;
}
.expiry-pill--normal { background: #ecfdf5; color: #059669; }
.expiry-pill--expiring { background: #fffbeb; color: #d97706; }
.expiry-pill--expired { background: #fef2f2; color: #dc2626; }
.expiry-pill--unknown { background: #f3f4f6; color: #6b7280; }

/* 行高亮 */
::v-deep .row-expired td { background: #fef2f2 !important; }
::v-deep .row-expiring td { background: #fffbeb !important; }

/* 详情顶部状态条 */
.detail-top-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
}
.detail-top-bar--normal { background: #ecfdf5; color: #059669; }
.detail-top-bar--expiring { background: #fffbeb; color: #d97706; }
.detail-top-bar--expired { background: #fef2f2; color: #dc2626; }
.detail-top-bar--unknown { background: #f3f4f6; color: #6b7280; }
.detail-days {
  margin-left: auto;
  font-size: 13px;
  opacity: .85;
}
</style>
