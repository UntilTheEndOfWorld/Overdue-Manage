<template>
  <div class="add-points-product-wrapper">
    <el-form label-width="108px" :model="form" ref="form" :rules="rules">
      <el-card style="margin: 20px 20px; font-size: 14px">
        <div slot="header">
          <span>基本信息</span>
        </div>
        <el-row>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品编码" prop="outProductId">
              <el-input v-model="form.outProductId" placeholder="请输入商品编码"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="品牌" prop="brandId">
              <brand-select v-model="form.brandId" @change="onBrandChange"></brand-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryId">
              <product-category-select v-model="form.categoryId" @change="categoryChange"></product-category-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sort">
              <el-input v-model="form.sort" placeholder="请输入排序"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="所需积分" prop="points">
              <el-input v-model="form.points" placeholder="请输入所需积分" type="number">
                <template slot="append">积分</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价" prop="originalPrice">
              <el-input v-model="form.originalPrice" placeholder="请输入原价" type="number">
                <template slot="append">元</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="商品类型" prop="productType">
              <el-select v-model="form.productType" placeholder="请选择商品类型">
                <el-option label="实物商品" :value="1"></el-option>
                <el-option label="虚拟商品" :value="2"></el-option>
                <el-option label="优惠券" :value="3"></el-option>
                <el-option label="服务" :value="4"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="兑换限制" prop="exchangeLimit">
              <el-input v-model="form.exchangeLimit" placeholder="每人限兑数量，0表示不限制" type="number">
                <template slot="append">件</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="总库存" prop="totalStock">
              <el-input v-model="form.totalStock" placeholder="请输入总库存" type="number">
                <template slot="append">件</template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" placeholder="请选择单位" clearable>
                <el-option
                  v-for="dict in dict.type.pms_product_unit"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="上架状态" prop="publishStatus">
              <el-radio-group v-model="form.publishStatus">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="是否轮播" prop="isBanner">
              <el-radio-group v-model="form.isBanner">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="支持快递" prop="supportExpress">
              <el-radio-group v-model="form.supportExpress">
                <el-radio :label="1">支持</el-radio>
                <el-radio :label="0">不支持</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <el-form-item label="限时活动" prop="isLimitedTime">
              <el-radio-group v-model="form.isLimitedTime" @change="onLimitedTimeChange">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.isLimitedTime === 1">
            <el-form-item label="活动开始时间" prop="activityStartTime">
              <el-date-picker
                v-model="form.activityStartTime"
                type="datetime"
                placeholder="选择活动开始时间"
                value-format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8" v-if="form.isLimitedTime === 1">
            <el-form-item label="活动结束时间" prop="activityEndTime">
              <el-date-picker
                v-model="form.activityEndTime"
                type="datetime"
                placeholder="选择活动结束时间"
                value-format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.isBanner === 1">
          <el-col :span="12">
            <el-form-item label="轮播标题" prop="bannerTitle">
              <el-input v-model="form.bannerTitle" placeholder="请输入轮播标题"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="商品描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入商品描述"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <el-card style="margin: 20px 20px; font-size: 14px">
        <div slot="header">
          <span>产品图片</span>
        </div>
        <el-form-item label="主图" prop="pic">
          <oss-image-upload v-model="form.pic" :limit="1"></oss-image-upload>
        </el-form-item>
        <el-form-item label="轮播图" prop="albumPics">
          <oss-image-upload v-model="albumPics" :limit="5"></oss-image-upload>
        </el-form-item>
      </el-card>

      <el-card style="margin: 20px 20px; font-size: 14px">
        <div slot="header">
          <span>产品规格</span>
        </div>
        <el-form-item label="规格类型">
          <div class="sku-wrapper">
            <div class="sku_sorts">
              <div class="sku_sort" v-for="(s, idx0) in productAttr" :key="s.name">
                <div class="label flex-center">
                  <div class="flex-one">
                    <dict-select v-model="s.name" prop-name="sku_sort_list" value-prop="label"></dict-select>
                  </div><a class="red" @click="deleteSkuSort(idx0)">删除规格类型</a>
                </div>
                <div class="values" v-if="s.name">
                  <div class="value" v-for="(it2, idx1) in s.options" :key="idx1">
                    <el-input :value="it2.name" @input="changeName(s, idx1, $event)" placeholder="请输入规格名称"></el-input><a class="red no-break ml8" v-if="idx1 < s.options.length - 1 || (s.options.length === maxOptionNum && idx1 === 3)" @click="deleteOption(s, idx1)">删除</a>
                  </div>
                </div>
              </div>
            </div>
            <el-button v-if="productAttr.length < 2" @click="addSkuSort">+添加规格类型</el-button>
          </div>
        </el-form-item>
        <el-form-item label="规格信息">
          <el-table :data="form.skuList" :max-height="400">
            <el-table-column v-for="s in skuAttr" :label="s.name" :key="s.name" :prop="s.name"></el-table-column>
            <el-table-column label="展示图片">
              <template v-slot="{ row }">
                <oss-image-upload class="img-upload-mini" v-model="row.pic" :limit="1" :is-show-tip="false"></oss-image-upload>
              </template>
            </el-table-column>
            <el-table-column label="所需积分" >
              <template v-slot="{ row,$index }">
                <el-form-item
                  :rules="{ required: true, message: '请填写积分', trigger: 'blur' }"
                  :prop="'skuList['+$index+'].points'">
                  <el-input v-model="row.points"></el-input>
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column label="原价">
              <template v-slot="{ row,$index }">
                <el-form-item
                  :prop="'skuList['+$index+'].originalPrice'">
                  <el-input v-model="row.originalPrice"></el-input>
                </el-form-item>
              </template>
            </el-table-column>
            <el-table-column label="库存">
              <template v-slot="{ row, $index }">
                <el-input v-model="row.stock" type="number"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="兑换限制">
              <template v-slot="{ row, $index }">
                <el-input v-model="row.exchangeLimit" type="number" placeholder="0表示不限制"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="编码">
              <template v-slot="{ row }">
                <el-form-item>
                  <el-input v-model="row.outSkuId"></el-input>
                  <el-input v-model="row.spData" v-show="false"></el-input>
                </el-form-item>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-card>
      <el-card style="margin: 20px 20px; font-size: 14px">
        <div slot="header">
          <span>详情页</span>
        </div>
        <el-form-item label="移动端" prop="detailMobileHtml">
          <Editor v-model="form.detailMobileHtml" placeholder="请输入内容" type="url"></Editor>
        </el-form-item>
        <el-form-item label="PC端" prop="detailHtml">
          <Editor v-model="form.detailHtml" placeholder="请输入内容" type="url"></Editor>
        </el-form-item>
      </el-card>

      <div class="tc">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import {addPointsProduct, getPointsProduct, updatePointsProduct} from "@/api/pms/pointsProduct";
import ProductCategorySelect from "@/views/components/ProductCategorySelect";
import BrandSelect from "@/views/components/BrandSelect";

export default {
  name: "AddPointsProduct",
  dicts: ['pms_publish_status', 'pms_is_banner', 'pms_support_express', 'pms_product_unit'],
  components: {BrandSelect, ProductCategorySelect},
  data() {
    return {
       rules: {
          name: [
            { required: true, message: '请输入商品名称', trigger: 'blur' },
          ],
          points: [
            { required: true, message: '请输入所需积分', trigger: 'blur' },
            { type: 'number', message: '积分必须为数字', trigger: 'blur' }
          ],
       },
      form: {
         publishStatus: 1,
         sort: 1000,
         isBanner: 0,
         bannerTitle: '',
         supportExpress: 1,
         productType: 1,
         exchangeLimit: 0,
         totalStock: 0,
         exchangedCount: 0,
         isLimitedTime: 0,
         hotness: 0
      },
      skuAttr:[],
      albumPics:null,
      productAttr: [
        {
          name: '颜色',
          options: [
            {name: '红'},
            {name: null}
          ]
        }
      ],
      maxOptionNum: 44
    }
  },
  created() {
    const {id} = this.$route.query
    if (id) {
      this.getInfo(id);
    }
  },
  methods: {
    refreshSku(){
      let skus = [];
      let attrs = this.productAttr.filter(it => it.name);
      if (attrs.length === 0) {
        this.form.skuList = [];
        return;
      }
      let first = attrs[0];
      if (attrs.length === 1) {
        first.options.filter(it => it.name).forEach(it => {
          skus.push({
            spData: it.name,
            spValue: it.name,
            points: this.form.points || 0,
            originalPrice: this.form.originalPrice || 0,
            stock: this.form.totalStock || 0,
            exchangeLimit: this.form.exchangeLimit || 0,
            outSkuId: this.form.outProductId + '-' + it.name
          });
        });
      } else {
        let second = attrs[1];
        first.options.filter(it => it.name).forEach(it1 => {
          second.options.filter(it => it.name).forEach(it2 => {
            skus.push({
              spData: JSON.stringify({[first.name]: it1.name, [second.name]: it2.name}),
              spValue: it1.name + ',' + it2.name,
              points: this.form.points || 0,
              originalPrice: this.form.originalPrice || 0,
              stock: this.form.totalStock || 0,
              exchangeLimit: this.form.exchangeLimit || 0,
              outSkuId: this.form.outProductId + '-' + it1.name + '-' + it2.name
            });
          });
        });
      }
      this.form.skuList = skus;
    },
    addSkuSort(){
      this.productAttr.push({
        name: null,
        options: [
          {name: null}
        ]
      });
    },
    deleteSkuSort(idx){
      this.productAttr.splice(idx, 1);
      this.refreshSku();
    },
    changeName(sort, idx, val){
      sort.options[idx].name = val;
      this.refreshSku();
    },
    deleteOption(sort, idx){
      sort.options.splice(idx, 1);
      this.refreshSku();
    },
    onBrandChange(brand) {
      this.form.brandName = brand.name;
    },
    categoryChange(category) {
      this.form.productCategoryName = category.name;
    },
    onLimitedTimeChange(value) {
      if (value === 0) {
        this.form.activityStartTime = null;
        this.form.activityEndTime = null;
      }
    },
    getInfo(id) {
      getPointsProduct(id).then(response => {
        this.form = response.data;
        if (this.form.albumPics) {
          this.albumPics = this.form.albumPics.split(',');
        }
        if (this.form.productAttr) {
          this.productAttr = JSON.parse(this.form.productAttr);
        }
        this.refreshSku();
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.albumPics) {
            this.form.albumPics = this.albumPics.join(',');
          }
          this.form.productAttr = JSON.stringify(this.productAttr);
          if (this.form.id != null) {
            updatePointsProduct(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.$router.push('/pms/points-product');
            });
          } else {
            addPointsProduct(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.$router.push('/pms/points-product');
            });
          }
        }
      });
    },
    cancel() {
      this.$router.push('/pms/points-product');
    }
  },
  watch: {
    'form.points'() {
      this.refreshSku();
    },
    'form.originalPrice'() {
      this.refreshSku();
    },
    'form.totalStock'() {
      this.refreshSku();
    },
    'form.exchangeLimit'() {
      this.refreshSku();
    },
    'form.outProductId'() {
      this.refreshSku();
    }
  }
};
</script>

<style scoped>
.add-points-product-wrapper {
  padding: 20px;
}

.sku-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
}

.sku_sorts {
  margin-bottom: 20px;
}

.sku_sort {
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.label {
  margin-bottom: 10px;
}

.values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.value {
  display: flex;
  align-items: center;
}

.flex-center {
  display: flex;
  align-items: center;
}

.flex-one {
  flex: 1;
}

.red {
  color: #f56c6c;
  cursor: pointer;
  margin-left: 10px;
}

.ml8 {
  margin-left: 8px;
}

.no-break {
  white-space: nowrap;
}

.tc {
  text-align: center;
  margin: 20px 0;
}

.img-upload-mini {
  width: 60px;
  height: 60px;
}
</style>
