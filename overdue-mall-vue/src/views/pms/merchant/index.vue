<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="100px" size="medium" class="ry_form">
      <el-form-item label="状态" prop="status">
        <DictRadio v-model="queryParams.status" @change="handleQuery" size="small"
                   :radioData="dict.type.sys_normal_disable" :showAll="'all'"/>
      </el-form-item>
      <el-form-item label="商户名称" prop="merchantName">
        <el-input
          v-model="queryParams.merchantName"
          placeholder="请输入商户名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="商户编码" prop="merchantCode">
        <el-input
          v-model="queryParams.merchantCode"
          placeholder="请输入商户编码"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="联系电话" prop="phone">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入联系电话"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item class="flex_one tr">
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
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="merchantList" @selection-change="handleSelectionChange" border>
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="商户Logo" prop="logo" width="80">
        <template slot-scope="{ row }">
          <el-image v-if="row.logo" :src="row.logo" :preview-src-list="[row.logo]" class="small-img circle-img"/>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="商户信息" min-width="200">
        <template slot-scope="{ row }">
          <div><strong>{{ row.merchantName }}</strong></div>
          <div class="text-muted">编码：{{ row.merchantCode }}</div>
          <div v-if="row.phone" class="text-muted">电话：{{ row.phone }}</div>
        </template>
      </el-table-column>
      <el-table-column label="地址" prop="address" min-width="150" show-overflow-tooltip>
        <template slot-scope="{ row }">
          <span v-if="row.address">{{ row.address }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="简介" prop="description" min-width="150" show-overflow-tooltip>
        <template slot-scope="{ row }">
          <span v-if="row.description">{{ row.description }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" prop="status" width="80">
        <template slot-scope="{ row }">
          <dict-tag :value="row.status" prop-name="sys_normal_disable"/>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="80"/>
      <el-table-column label="操作" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.status === 1 ? 'el-icon-lock' : 'el-icon-unlock'"
            @click="handleStatusChange(scope.row)"
          >{{ scope.row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <InBody v-show="total>0">
      <pagination
        :total="total"
        :page.sync="queryParams.pageNum"
        :limit.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </InBody>

    <!-- 添加或修改商户信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="80%" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px" class="dialog-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商户名称" prop="merchantName">
              <el-input v-model="form.merchantName" placeholder="请输入商户名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商户编码" prop="merchantCode">
              <el-input v-model="form.merchantCode" placeholder="请输入商户编码" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商户地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入商户地址" />
        </el-form-item>

        <el-form-item label="商户简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商户简介" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商户Logo" prop="logo">
              <oss-image-upload v-model="form.logo" :limit="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图片" prop="coverImage">
              <oss-image-upload v-model="form.coverImage" :limit="1" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="营业执照" prop="businessLicense">
          <oss-image-upload v-model="form.businessLicense" :limit="1" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <DictRadio v-model="form.status" size="small"
                         :radioData="dict.type.sys_normal_disable"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="999" placeholder="排序" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>

        <!-- 商户图片管理 -->
        <el-form-item label="商户图片">
          <div class="image-upload-section">
            <el-button type="primary" size="small" @click="addImage">添加图片</el-button>
            <div v-for="(image, index) in form.imageList" :key="index" class="image-item">
              <el-row :gutter="10">
                <el-col :span="8">
                  <oss-image-upload v-model="image.imageUrl" :limit="1" />
                </el-col>
                <el-col :span="4">
                  <el-select v-model="image.imageType" placeholder="图片类型">
                    <el-option label="环境照片" :value="1"></el-option>
                    <el-option label="产品照片" :value="2"></el-option>
                    <el-option label="其他" :value="3"></el-option>
                  </el-select>
                </el-col>
                <el-col :span="4">
                  <el-input-number v-model="image.sort" :min="0" placeholder="排序" />
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" size="small" @click="removeImage(index)">删除</el-button>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-form-item>

        <!-- 商户视频管理 -->
        <el-form-item label="商户视频">
          <div class="video-upload-section">
            <el-button type="primary" size="small" @click="addVideo">添加视频</el-button>
            <div v-for="(video, index) in form.videoList" :key="index" class="video-item">
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-input v-model="video.videoUrl" placeholder="视频URL" />
                </el-col>
                <el-col :span="4">
                  <oss-image-upload v-model="video.videoCover" :limit="1" />
                </el-col>
                <el-col :span="4">
                  <el-input v-model="video.videoTitle" placeholder="视频标题" />
                </el-col>
                <el-col :span="4">
                  <el-input-number v-model="video.sort" :min="0" placeholder="排序" />
                </el-col>
                <el-col :span="4">
                  <el-button type="danger" size="small" @click="removeVideo(index)">删除</el-button>
                </el-col>
              </el-row>
              <el-row :gutter="10" style="margin-top: 10px;">
                <el-col :span="12">
                  <el-input v-model="video.videoDescription" placeholder="视频描述" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="video.videoDuration" :min="0" placeholder="时长(秒)" />
                </el-col>
                <el-col :span="6">
                  <el-input-number v-model="video.videoSize" :min="0" placeholder="大小(字节)" />
                </el-col>
              </el-row>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看商户详情对话框 -->
    <el-dialog title="商户详情" :visible.sync="viewOpen" width="80%" append-to-body>
      <div v-if="viewData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商户名称">{{ viewData.merchantName }}</el-descriptions-item>
          <el-descriptions-item label="商户编码">{{ viewData.merchantCode }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ viewData.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ viewData.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商户地址" :span="2">{{ viewData.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商户简介" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <dict-tag :value="viewData.status" prop-name="sys_normal_disable"/>
          </el-descriptions-item>
          <el-descriptions-item label="排序">{{ viewData.sort }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ viewData.updateTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- 商户图片展示 -->
        <div v-if="viewData.imageList && viewData.imageList.length > 0" style="margin-top: 20px;">
          <h4>商户图片</h4>
          <el-row :gutter="10">
            <el-col :span="6" v-for="(image, index) in viewData.imageList" :key="index">
              <el-card>
                <el-image :src="image.imageUrl" :preview-src-list="viewData.imageList.map(img => img.imageUrl)" class="preview-image"/>
                <div style="padding: 10px;">
                  <div>{{ getImageTypeName(image.imageType) }}</div>
                  <div class="text-muted">排序：{{ image.sort }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 商户视频展示 -->
        <div v-if="viewData.videoList && viewData.videoList.length > 0" style="margin-top: 20px;">
          <h4>商户视频</h4>
          <el-row :gutter="10">
            <el-col :span="8" v-for="(video, index) in viewData.videoList" :key="index">
              <el-card>
                <div v-if="video.videoCover">
                  <el-image :src="video.videoCover" class="video-cover"/>
                </div>
                <div style="padding: 10px;">
                  <div><strong>{{ video.videoTitle || '无标题' }}</strong></div>
                  <div class="text-muted">{{ video.videoDescription || '无描述' }}</div>
                  <div class="text-muted">时长：{{ video.videoDuration }}秒 | 排序：{{ video.sort }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {addMerchant, delMerchant, exportMerchant, getMerchant, listMerchant, updateMerchant, changeMerchantStatus} from "@/api/pms/merchant";

export default {
  name: "Merchant",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
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
      // 商户信息表格数据
      merchantList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 是否显示查看弹出层
      viewOpen: false,
      // 查看数据
      viewData: null,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        merchantName: null,
        merchantCode: null,
        phone: null,
        status: null
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        merchantName: [
          { required: true, message: "商户名称不能为空", trigger: "blur" }
        ],
        merchantCode: [
          { required: true, message: "商户编码不能为空", trigger: "blur" }
        ],
        phone: [
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号码", trigger: "blur" }
        ],
        email: [
          { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询商户信息列表 */
    getList() {
      this.loading = true;
      const {pageNum, pageSize} = this.queryParams;
      const query = {...this.queryParams, pageNum: undefined, pageSize: undefined};
      const pageReq = {page: pageNum - 1, size: pageSize};
      listMerchant(query, pageReq).then(response => {
        const { content, totalElements } = response
        this.merchantList = content;
        this.total = totalElements;
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
        merchantName: null,
        merchantCode: null,
        description: null,
        address: null,
        phone: null,
        email: null,
        logo: null,
        coverImage: null,
        businessLicense: null,
        status: 1,
        sort: 0,
        remark: null,
        imageList: [],
        videoList: []
      };
      this.resetForm("form");
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加商户信息";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getMerchant(id).then(response => {
        this.form = response;
        this.open = true;
        this.title = "修改商户信息";
      });
    },
    /** 查看按钮操作 */
    handleView(row) {
      this.viewData = row;
      this.viewOpen = true;
    },
    /** 状态修改 */
    handleStatusChange(row) {
      let text = row.status === 1 ? "禁用" : "启用";
      this.$modal.confirm('确认要"' + text + '""' + row.merchantName + '"商户吗？').then(function() {
        return changeMerchantStatus(row.id, row.status === 1 ? 0 : 1);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess(text + "成功");
      }).catch(() => {
        row.status = row.status === 0 ? 1 : 0;
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateMerchant(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addMerchant(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除商户信息编号为"' + ids + '"的数据项？').then(function() {
        return delMerchant(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$modal.confirm('是否确认导出所有商户信息数据项？').then(() => {
        this.exportLoading = true;
        return exportMerchant(queryParams);
      }).then(response => {
        this.$download.download(response);
        this.exportLoading = false;
      }).catch(() => {});
    },
    /** 添加图片 */
    addImage() {
      this.form.imageList.push({
        imageUrl: null,
        imageType: 1,
        sort: 0
      });
    },
    /** 删除图片 */
    removeImage(index) {
      this.form.imageList.splice(index, 1);
    },
    /** 添加视频 */
    addVideo() {
      this.form.videoList.push({
        videoUrl: null,
        videoCover: null,
        videoTitle: null,
        videoDescription: null,
        videoDuration: 0,
        videoSize: 0,
        sort: 0
      });
    },
    /** 删除视频 */
    removeVideo(index) {
      this.form.videoList.splice(index, 1);
    },
    /** 获取图片类型名称 */
    getImageTypeName(type) {
      const typeMap = {
        1: '环境照片',
        2: '产品照片',
        3: '其他'
      };
      return typeMap[type] || '';
    }
  }
};
</script>

<style scoped>
.dialog-form {
  max-height: 600px;
  overflow-y: auto;
}

.image-upload-section, .video-upload-section {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background-color: #fafafa;
}

.image-item, .video-item {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #fff;
}

.preview-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.video-cover {
  width: 100%;
  height: 100px;
  object-fit: cover;
}

.small-img {
  width: 40px;
  height: 40px;
}

.circle-img {
  border-radius: 50%;
}

.text-muted {
  color: #909399;
}
</style>
