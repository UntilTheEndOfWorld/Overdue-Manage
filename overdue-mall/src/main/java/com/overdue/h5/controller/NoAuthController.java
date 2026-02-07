package com.overdue.h5.controller;

import cn.hutool.core.util.RandomUtil;
import com.overdue.h5.domain.vo.HomeConfigVO;
import com.overdue.manager.pms.convert.ProductConvert;
import com.overdue.manager.pms.service.ProductCategoryService;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.domain.entity.SysDictData;
import com.overdue.common.core.redis.RedisService;
import com.overdue.system.service.ISysConfigService;
import com.overdue.system.service.ISysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.h5.domain.vo.H5ProductVO;
import com.overdue.manager.pms.service.ProductService;

@RestController
@RequestMapping("/no-auth")
public class NoAuthController {
    @Autowired
    private ISysConfigService sysConfigService;
    @Autowired
    private ProductCategoryService categoryService;
    @Autowired
    private ProductConvert productConvert;
    @Autowired
    private ISysDictTypeService dictTypeService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ProductService productService;

    /**
     * 首页配置
     *
     * @return 首页配置
     */
    @GetMapping("/home/home-cfg")
    public ResponseEntity<HomeConfigVO> getHomeConfig() {
        HomeConfigVO res = new HomeConfigVO();
        res.setBanners(sysConfigService.selectConfigByKey("h5.home.banner"));
        res.setCategoryList(categoryService.queryCategoryWithProductsForH5());
        // 添加人气推荐商品
        res.setPopularProducts(productConvert.dos2dtos(categoryService.getPopularProductsForH5()));
        return ResponseEntity.ok(res);
    }

    /**
     * 首页配置
     *
     * @return 首页配置
     */
    @GetMapping("/home/product-count")
    public ResponseEntity<HomeConfigVO> productCount() {
        HomeConfigVO res = new HomeConfigVO();
        res.setBanners(sysConfigService.selectConfigByKey("h5.home.banner"));
        res.setCategoryList(categoryService.queryCategoryWithProductsForH5());
        return ResponseEntity.ok(res);
    }

    /**
     * 获取轮播图商品列表
     * 小程序不传参数，固定返回5个商品
     *
     * @return 轮播图商品列表
     */
    @GetMapping("/home/banner-products")
    public ResponseEntity<List<H5ProductVO>> getBannerProducts() {
        try {
            // 固定返回5个轮播图商品
            List<Product> bannerProducts = productService.getBannerProducts(5);
            // 如果没有轮播图商品，返回热门商品作为备选
            if (bannerProducts == null || bannerProducts.isEmpty()) {
                bannerProducts = productService.getHotProducts(5);
            }
            List<H5ProductVO> result = productConvert.dos2dtos(bannerProducts);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 如果出现异常，返回热门商品作为备选
            List<Product> hotProducts = productService.getHotProducts(5);
            List<H5ProductVO> result = productConvert.dos2dtos(hotProducts);
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取应用账号
     */
    @GetMapping("/app/account/{type}")
    public ResponseEntity<String> getAppAccount(@PathVariable String type) {
        List<SysDictData> sysAppAccount = dictTypeService.selectDictDataByType("sys_app_account");
        SysDictData sysDictData = sysAppAccount.stream().filter(it -> it.getDictValue().equals(type)).findFirst()
                .orElseGet(SysDictData::new);
        return ResponseEntity.ok(sysDictData.getDictLabel());
    }

    @GetMapping("/verified/code/generate")
    public AjaxResult createCode() {
        String code = RandomUtil.randomNumbers(6);
        redisService.setVerifyCode(code);
        return AjaxResult.successData(code);
    }

    /**
     * 获取人气推荐商品
     *
     * @return 人气推荐商品列表
     */
    @GetMapping("/home/popular-products")
    public AjaxResult getPopularProducts() {
        return AjaxResult.successData(productConvert.dos2dtos(categoryService.getPopularProductsForH5()));
    }

    /**
     * 根据分类获取人气推荐商品
     *
     * @param categoryIds 分类ID列表，用逗号分隔
     * @param limit       限制数量，默认10个
     * @return 人气推荐商品列表
     */
    @GetMapping("/home/popular-products-by-category")
    public AjaxResult getPopularProductsByCategory(
            @RequestParam String categoryIds,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Long> categoryIdList = Arrays.stream(categoryIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            return AjaxResult.successData(productConvert.dos2dtos(
                    categoryService.getPopularProductsByCategoryIds(categoryIdList, limit)));
        } catch (Exception e) {
            return AjaxResult.error("参数错误：" + e.getMessage());
        }
    }

    /**
     * 开发模式默认登录接口（用于前后端联调）
     * 正式上线时应该删除此接口
     * 已注释 - 确保登录流程正常工作
     *
     * @return 默认用户登录信息
     */
    /*
     * @PostMapping("/development/default-login")
     * public AjaxResult developmentDefaultLogin() {
     * // 开发模式开关，正式上线改为false
     * boolean developmentMode = false;
     * 
     * if (!developmentMode) {
     * return AjaxResult.error("开发模式已关闭");
     * }
     * 
     * // 创建默认用户信息
     * Map<String, Object> result = new HashMap<>();
     * result.put("token", "development_token_" + System.currentTimeMillis());
     * 
     * Map<String, Object> userInfo = new HashMap<>();
     * userInfo.put("id", 1);
     * userInfo.put("openId", "default_openid_123456");
     * userInfo.put("nickName", "测试用户");
     * userInfo.put("avatarUrl", "");
     * userInfo.put("phone", "13800138000");
     * userInfo.put("isGuest", false);
     * userInfo.put("createTime", new java.util.Date());
     * 
     * result.put("userInfo", userInfo);
     * result.put("message", "开发模式默认登录成功");
     * 
     * System.out.println("开发模式：创建默认用户登录信息");
     * 
     * return AjaxResult.successData(result);
     * }
     */
}
