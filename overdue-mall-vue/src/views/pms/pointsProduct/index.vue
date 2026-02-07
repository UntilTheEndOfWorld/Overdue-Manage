<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="search">
        <el-input
          v-model="queryParams.search"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <product-category-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable />
      </el-form-item>
      <el-form-item label="上架状态" prop="publishStatus">
        <el-select v-model="queryParams.publishStatus" placeholder="请选择上架状态" clearable>
          <el-option
            v-for="dict in dict.type.pms_publish_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="商品类型" prop="productType">
        <el-select v-model="queryParams.productType" placeholder="请选择商品类型" clearable>
          <el-option label="实物商品" value="1" />
          <el-option label="虚拟商品" value="2" />
          <el-option label="优惠券" value="3" />
          <el-option label="服务" value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否轮播" prop="isBanner">
        <el-select v-model="queryParams.isBanner" placeholder="请选择是否轮播" clearable>
          <el-option
            v-for="dict in dict.type.pms_is_banner"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
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
          @click="handleAdd"
          v-hasPermi="['pms:points-product:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['pms:points-product:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['pms:points-product:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['pms:points-product:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="pointsProductList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="商品图片" align="center" prop="pic" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.pic" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="商品名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" prop="productCategoryName" width="120" />
      <el-table-column label="所需积分" align="center" prop="points" width="100">
        <template slot-scope="scope">
          <span style="color: #ff6b35; font-weight: bold;">{{ scope.row.points }}</span>
        </template>
      </el-table-column>
      <el-table-column label="原价" align="center" prop="originalPrice" width="100">
        <template slot-scope="scope">
          <span style="text-decoration: line-through; color: #999;">¥{{ scope.row.originalPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商品类型" align="center" prop="productType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.pms_points_product_type" :value="scope.row.productType"/>
        </template>
      </el-table-column>
      <el-table-column label="库存" align="center" prop="totalStock" width="80" />
      <el-table-column label="已兑换" align="center" prop="exchangedCount" width="80" />
      <el-table-column label="上架状态" align="center" prop="publishStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.pms_publish_status" :value="scope.row.publishStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="是否轮播" align="center" prop="isBanner" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.pms_is_banner" :value="scope.row.isBanner"/>
        </template>
      </el-table-column>
      <el-table-column label="限时活动" align="center" prop="isLimitedTime" width="100">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.isLimitedTime === 1" type="danger">限时</el-tag>
          <el-tag v-else type="info">常规</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['pms:points-product:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['pms:points-product:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.page"
      :limit.sync="queryParams.size"
      @pagination="getList"
    />

  </div>
</template>

<script>
import { listPointsProduct, getPointsProduct, delPointsProduct, delPointsProductBatch, exportPointsProduct } from "@/api/pms/pointsProduct";
import ProductCategorySelect from "@/views/components/ProductCategorySelect";

export default {
  name: "PointsProduct",
  dicts: ['pms_publish_status', 'pms_is_banner', 'pms_points_product_type'],
  components: { ProductCategorySelect },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 积分商品信息表格数据
      pointsProductList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        page: 0,
        size: 10,
        search: null,
        categoryId: null,
        publishStatus: null,
        productType: null,
        isBanner: null,
        isLimitedTime: null,
        orderField: null,
        orderSort: null
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询积分商品信息列表 */
    getList() {
      this.loading = true;
      listPointsProduct(this.queryParams).then(response => {
        this.pointsProductList = response.content;
        this.total = response.totalElements;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        brandId: null,
        categoryId: null,
        outProductId: null,
        name: null,
        pic: null,
        albumPics: null,
        publishStatus: 0,
        sort: 1000,
        points: null,
        originalPrice: null,
        unit: null,
        weight: null,
        productAttr: null,
        detailHtml: null,
        detailMobileHtml: null,
        brandName: null,
        productCategoryName: null,
        hotness: 0,
        isBanner: 0,
        bannerTitle: null,
        supportExpress: 1,
        productType: 1,
        exchangeLimit: 0,
        totalStock: 0,
        exchangedCount: 0,
        activityStartTime: null,
        activityEndTime: null,
        isLimitedTime: 0,
        description: null,
        skuList: []
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.page = 0;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push('/pms/points-product/add');
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      const id = row.id || this.ids[0];
      this.$router.push('/pms/points-product/edit/' + id);
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePointsProduct(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addPointsProduct(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除积分商品信息编号为"' + ids + '"的数据项？').then(function() {
        if (Array.isArray(ids)) {
          return delPointsProductBatch(ids);
        } else {
          return delPointsProduct(ids);
        }
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('pms/points-product/export', {
        ...this.queryParams
      }, `积分商品信息_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}
</style>
