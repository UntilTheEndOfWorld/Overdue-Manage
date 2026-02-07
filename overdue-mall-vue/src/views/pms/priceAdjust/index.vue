<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="任务名称" prop="taskName">
        <el-input
          v-model="queryParams.taskName"
          placeholder="请输入任务名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待执行" :value="0"/>
          <el-option label="执行中" :value="1"/>
          <el-option label="已完成" :value="2"/>
          <el-option label="失败" :value="3"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleBatchAdjust"
        >批量调价</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="任务名称" align="center" prop="taskName" />
      <el-table-column label="调价类型" align="center" prop="adjustTypeName" />
      <el-table-column label="调价值" align="center" prop="adjustValue">
        <template slot-scope="scope">
          <span v-if="scope.row.adjustType === 1">{{ scope.row.adjustValue }}元</span>
          <span v-else>{{ scope.row.adjustValue }}%</span>
        </template>
      </el-table-column>
      <el-table-column label="目标类型" align="center" prop="targetTypeName" />
      <el-table-column label="影响商品数" align="center" prop="affectCount" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" type="info">待执行</el-tag>
          <el-tag v-if="scope.row.status === 1" type="warning">执行中</el-tag>
          <el-tag v-if="scope.row.status === 2" type="success">已完成</el-tag>
          <el-tag v-if="scope.row.status === 3" type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="执行时间" align="center" prop="executeTime" width="180"/>
      <el-table-column label="操作人" align="center" prop="operatorName" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180"/>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 批量调价对话框 -->
    <el-dialog :title="'批量调价'" :visible.sync="batchDialogVisible" width="600px" append-to-body>
      <el-form ref="batchForm" :model="batchForm" :rules="batchRules" label-width="120px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="batchForm.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="调价类型" prop="adjustType">
          <el-radio-group v-model="batchForm.adjustType">
            <el-radio :label="1">按固定金额</el-radio>
            <el-radio :label="2">按百分比</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调价值" prop="adjustValue">
          <el-input v-model="batchForm.adjustValue" placeholder="请输入调价值">
            <template slot="append" v-if="batchForm.adjustType === 1">元</template>
            <template slot="append" v-if="batchForm.adjustType === 2">%</template>
          </el-input>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            <span v-if="batchForm.adjustType === 1">正数为涨价，负数为降价。例如：输入 5 表示每个商品涨价5元</span>
            <span v-if="batchForm.adjustType === 2">正数为涨价，负数为降价。例如：输入 10 表示每个商品涨价10%</span>
          </div>
        </el-form-item>
        <el-form-item label="目标类型" prop="targetType">
          <el-radio-group v-model="batchForm.targetType" @change="handleTargetTypeChange">
            <el-radio :label="3">全部商品</el-radio>
            <el-radio :label="2">指定分类</el-radio>
            <el-radio :label="1">指定商品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择分类" prop="targetIds" v-if="batchForm.targetType === 2">
          <product-category-select v-model="selectedCategories" :multiple="true"></product-category-select>
        </el-form-item>
        <el-form-item label="选择商品" prop="targetIds" v-if="batchForm.targetType === 1">
          <el-button size="small" @click="showProductSelector">选择商品</el-button>
          <div style="margin-top: 10px;">已选择 {{ selectedProducts.length }} 个商品</div>
        </el-form-item>
        <el-form-item label="调价原因" prop="reason">
          <el-input v-model="batchForm.reason" type="textarea" placeholder="请输入调价原因" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="batchDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitBatchAdjust">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 商品选择对话框 -->
    <el-dialog title="选择商品" :visible.sync="productSelectorVisible" width="800px" append-to-body>
      <el-table :data="productList" @selection-change="handleProductSelectionChange" ref="productTable">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column label="商品名称" prop="name" />
        <el-table-column label="当前价格" prop="price" width="120" />
        <el-table-column label="分类" prop="productCategoryName" width="150" />
      </el-table>
      <pagination
        v-show="productTotal>0"
        :total="productTotal"
        :page.sync="productQueryParams.pageNum"
        :limit.sync="productQueryParams.pageSize"
        @pagination="getProductList"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="productSelectorVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmProductSelection">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPriceAdjustTask, batchAdjustPrice } from "@/api/pms/priceAdjust";
import { listPmsProduct } from "@/api/pms/product";
import ProductCategorySelect from "@/views/components/ProductCategorySelect";

export default {
  name: "PriceAdjust",
  components: {
    ProductCategorySelect
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 任务表格数据
      taskList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        taskName: null,
        status: null
      },
      // 批量调价对话框
      batchDialogVisible: false,
      batchForm: {
        taskName: '',
        adjustType: 2,
        adjustValue: null,
        targetType: 3,
        targetIds: null,
        reason: ''
      },
      batchRules: {
        taskName: [
          { required: true, message: "任务名称不能为空", trigger: "blur" }
        ],
        adjustType: [
          { required: true, message: "请选择调价类型", trigger: "change" }
        ],
        adjustValue: [
          { required: true, message: "调价值不能为空", trigger: "blur" }
        ],
        reason: [
          { required: true, message: "调价原因不能为空", trigger: "blur" }
        ]
      },
      // 商品选择相关
      productSelectorVisible: false,
      productList: [],
      productTotal: 0,
      productQueryParams: {
        pageNum: 1,
        pageSize: 10
      },
      selectedProducts: [],
      selectedCategories: []
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询任务列表 */
    getList() {
      this.loading = true;
      listPriceAdjustTask(this.queryParams).then(response => {
        this.taskList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 批量调价按钮 */
    handleBatchAdjust() {
      this.reset();
      this.batchDialogVisible = true;
    },
    /** 表单重置 */
    reset() {
      this.batchForm = {
        taskName: '',
        adjustType: 2,
        adjustValue: null,
        targetType: 3,
        targetIds: null,
        reason: ''
      };
      this.selectedProducts = [];
      this.selectedCategories = [];
      this.resetForm("batchForm");
    },
    /** 目标类型改变 */
    handleTargetTypeChange(value) {
      this.selectedProducts = [];
      this.selectedCategories = [];
    },
    /** 显示商品选择器 */
    showProductSelector() {
      this.productSelectorVisible = true;
      this.getProductList();
    },
    /** 查询商品列表 */
    getProductList() {
      listPmsProduct(this.productQueryParams).then(response => {
        this.productList = response.rows;
        this.productTotal = response.total;
      });
    },
    /** 商品选择改变 */
    handleProductSelectionChange(selection) {
      this.selectedProducts = selection;
    },
    /** 确认商品选择 */
    confirmProductSelection() {
      this.productSelectorVisible = false;
    },
    /** 提交批量调价 */
    submitBatchAdjust() {
      this.$refs["batchForm"].validate(valid => {
        if (valid) {
          // 准备目标ID
          let targetIds = [];
          if (this.batchForm.targetType === 1) {
            // 指定商品
            if (this.selectedProducts.length === 0) {
              this.$modal.msgError("请至少选择一个商品");
              return;
            }
            targetIds = this.selectedProducts.map(item => item.id);
          } else if (this.batchForm.targetType === 2) {
            // 指定分类
            if (!this.selectedCategories || (Array.isArray(this.selectedCategories) && this.selectedCategories.length === 0)) {
              this.$modal.msgError("请选择至少一个分类");
              return;
            }
            // 如果是级联选择器返回的数组，取最后一个
            if (Array.isArray(this.selectedCategories)) {
              targetIds = [this.selectedCategories[this.selectedCategories.length - 1]];
            } else {
              targetIds = [this.selectedCategories];
            }
          }
          
          const data = {
            ...this.batchForm,
            targetIds: JSON.stringify(targetIds)
          };
          
          batchAdjustPrice(data).then(response => {
            this.$modal.msgSuccess("调价任务已提交，影响商品数：" + response.data);
            this.batchDialogVisible = false;
            this.getList();
          });
        }
      });
    }
  }
};
</script>

<style scoped>
</style>

