package com.overdue.h5.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.overdue.common.utils.SecurityUtils;
import com.overdue.h5.config.SecurityUtil;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.mapper.SkuMapper;
import com.overdue.manager.ums.convert.MemberCartConvert;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.entity.MemberCart;
import com.overdue.manager.ums.mapper.MemberCartMapper;
import com.overdue.manager.ums.domain.query.MemberCartQuery;
import com.overdue.manager.ums.domain.vo.MemberCartVO;
import com.overdue.manager.ums.domain.form.UpdateMemberCartForm;
import com.github.pagehelper.PageHelper;
import com.overdue.common.constant.Constants;
import com.overdue.common.exception.base.BaseException;
import com.overdue.common.utils.MemberSecurityUtils;
import com.overdue.common.utils.SortUtil;
import com.overdue.framework.config.LocalDataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车Service业务层处理
 *
 * @author zcc
 */
@Slf4j
@Service
public class H5MemberCartService {
    @Autowired
    private MemberCartMapper memberCartMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MemberCartConvert memberCartConvert;

    /**
     * 查询购物车
     *
     * @param id 购物车主键
     * @return 购物车
     */
    public MemberCart selectById(Long id) {
        return memberCartMapper.selectById(id);
    }

    /**
     * 查询购物车列表
     *
     * @param query 查询条件
     * @param page  分页条件
     * @return 购物车
     */
    public List<MemberCartVO> selectList(MemberCartQuery query, Pageable page) {
        if (page != null) {
            PageHelper.startPage(page.getPageNumber() + 1, page.getPageSize(),
                    SortUtil.sort2string(page.getSort(), "id desc"));
        }
        QueryWrapper<MemberCart> qw = new QueryWrapper<>();
        if (query.getMemberId() != null) {
            qw.eq("member_id", query.getMemberId());
        }
        List<MemberCart> memberCartList = memberCartMapper.selectList(qw);
        if (CollectionUtil.isEmpty(memberCartList)) {
            return Collections.emptyList();
        }

        // 查sku
        List<Long> skuIdList = memberCartList.stream().map(MemberCart::getSkuId).collect(Collectors.toList());
        QueryWrapper<Sku> skuQw = new QueryWrapper<>();
        skuQw.in("id", skuIdList);
        Map<Long, Sku> skuMap = skuMapper.selectList(skuQw).stream()
                .collect(Collectors.toMap(Sku::getId, it -> it, (v1, v2) -> v2));

        // 查商品信息，用于获取商品图片
        List<Long> productIdList = memberCartList.stream().map(MemberCart::getProductId).collect(Collectors.toList());
        QueryWrapper<Product> productQw = new QueryWrapper<>();
        productQw.in("id", productIdList);
        Map<Long, Product> productMap = productMapper.selectList(productQw).stream()
                .collect(Collectors.toMap(Product::getId, it -> it, (v1, v2) -> v2));

        List<MemberCartVO> resList = new ArrayList<>();
        memberCartList.forEach(item -> {
            MemberCartVO memberCartVO = new MemberCartVO();
            BeanUtils.copyProperties(item, memberCartVO);

            // 添加调试日志
            log.info("处理购物车商品: ID={}, 商品名={}, SKU_ID={}, 数量={}",
                    item.getId(), item.getProductName(), item.getSkuId(), item.getQuantity());

            // 处理图片字段：优先使用购物车中的图片，如果为空则从SKU或商品中获取
            if (StrUtil.isEmpty(memberCartVO.getPic())) {
                // 优先从SKU获取图片
                if (skuMap.containsKey(item.getSkuId())) {
                    Sku sku = skuMap.get(item.getSkuId());
                    if (StrUtil.isNotEmpty(sku.getPic())) {
                        memberCartVO.setPic(sku.getPic());
                    }
                }

                // 如果SKU没有图片，从商品获取图片
                if (StrUtil.isEmpty(memberCartVO.getPic()) && productMap.containsKey(item.getProductId())) {
                    Product product = productMap.get(item.getProductId());
                    if (StrUtil.isNotEmpty(product.getPic())) {
                        memberCartVO.setPic(product.getPic());
                    }
                }
            }

            if (!skuMap.containsKey(item.getSkuId())) {
                memberCartVO.setStatus(0);
                memberCartVO.setSkuIfExist(0);
                log.warn("购物车商品[{}] 找不到对应的SKU: SKU_ID={}",
                        memberCartVO.getProductName(), item.getSkuId());

                // 当SKU不存在时，尝试从商品表获取价格作为备选
                if (productMap.containsKey(item.getProductId())) {
                    Product product = productMap.get(item.getProductId());
                    if (product.getPrice() != null) {
                        memberCartVO.setPrice(product.getPrice());
                        log.info("购物车商品[{}] 使用商品价格作为备选: {}",
                                memberCartVO.getProductName(), product.getPrice());
                    }
                }
            } else {
                Sku sku = skuMap.get(item.getSkuId());
                memberCartVO.setPrice(sku.getPrice());
                memberCartVO.setSkuIfExist(1);

                // 添加调试日志
                log.info("购物车商品[{}] SKU价格: {} (类型: {})",
                        memberCartVO.getProductName(),
                        sku.getPrice(),
                        sku.getPrice() != null ? sku.getPrice().getClass().getSimpleName() : "null");
            }

            // 添加最终价格调试日志
            log.info("购物车商品[{}] 最终设置的价格: {}",
                    memberCartVO.getProductName(), memberCartVO.getPrice());
            resList.add(memberCartVO);
        });
        return resList;
    }

    /**
     * 新增购物车
     *
     * @param memberCart 购物车
     * @return 结果
     */
    public int insert(MemberCart memberCart) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        memberCart.setMemberId(member.getId());

        // 查询商品信息获取商品名称
        if (memberCart.getProductName() == null && memberCart.getProductId() != null) {
            try {
                Product product = productMapper.selectById(memberCart.getProductId());
                if (product != null) {
                    memberCart.setProductName(product.getName());
                } else {
                    // 如果查不到商品，设置默认名称
                    memberCart.setProductName("商品ID:" + memberCart.getProductId());
                }
            } catch (Exception e) {
                // 查询失败时设置默认名称
                memberCart.setProductName("商品ID:" + memberCart.getProductId());
                System.err.println("查询商品名称失败: " + e.getMessage());
            }
        }

        // 判断cart是否存在
        QueryWrapper<MemberCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", member.getId());
        queryWrapper.eq("sku_id", memberCart.getSkuId());
        queryWrapper.eq("product_id", memberCart.getProductId());
        List<MemberCart> memberCarts = memberCartMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(memberCarts)) {
            memberCart.setStatus(1);
            memberCart.setCreateTime(LocalDateTime.now());
            memberCart.setCreateBy(member.getId());
            return memberCartMapper.insert(memberCart);
        }
        MemberCart dbCart = memberCarts.get(0);
        dbCart.setUpdateTime(LocalDateTime.now());
        dbCart.setQuantity(dbCart.getQuantity() + memberCart.getQuantity());
        return memberCartMapper.updateById(dbCart);
    }

    /**
     * 修改购物车
     *
     * @param memberCart 购物车
     * @return 结果
     */
    public int update(MemberCart memberCart) {
        MemberCart cart = memberCartMapper.selectById(memberCart.getId());
        if (cart == null) {
            return 0;
        }
        cart.setQuantity(memberCart.getQuantity());
        cart.setUpdateTime(LocalDateTime.now());
        cart.setUpdateBy(SecurityUtil.getLocalMember().getId());
        return memberCartMapper.updateById(cart);
    }

    public int update(UpdateMemberCartForm form) {
        if (form.getNum() == null || form.getId() == null) {
            throw new BaseException("参数错误");
        }

        // 使用LocalDataUtil获取用户信息，兼容开发模式
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        Long userId;
        if (member == null) {
            // 如果LocalDataUtil中没有用户信息，尝试使用SecurityUtils
            try {
                userId = SecurityUtils.getUserId();
            } catch (Exception e) {
                System.err.println("获取用户ID失败: " + e.getMessage());
                throw new BaseException("获取用户ID失败");
            }
        } else {
            userId = member.getId();
        }

        LambdaQueryWrapper<MemberCart> qw = new LambdaQueryWrapper<>();
        qw.eq(MemberCart::getMemberId, userId);
        qw.eq(MemberCart::getId, form.getId());
        if (form.getNum() <= 0) {
            return memberCartMapper.delete(qw);
        }
        MemberCart e = new MemberCart();
        e.setQuantity(form.getNum());
        return memberCartMapper.update(e, qw);
    }

    /**
     * 删除购物车信息
     *
     * @param id 购物车主键
     * @return 结果
     */
    public int deleteById(Long id) {
        return memberCartMapper.deleteById(id);
    }

    /**
     * 删除购物车信息
     *
     * @param ids 购物车主键
     * @return 结果
     */
    public int deleteByIds(String ids) {
        List<Long> idList = Arrays.stream(ids.split(",")).map(it -> Long.parseLong(it)).collect(Collectors.toList());
        return memberCartMapper.deleteBatchIds(idList);
    }

    public Integer mineCartNum() {
        // 使用LocalDataUtil获取用户信息，兼容开发模式
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        if (member == null) {
            // 如果LocalDataUtil中没有用户信息，尝试使用SecurityUtils
            try {
                Long userId = SecurityUtils.getUserId();
                QueryWrapper<MemberCart> qw = new QueryWrapper<>();
                qw.eq("member_id", userId);
                qw.eq("status", 1);
                qw.select("count(quantity) quantity");
                MemberCart c = memberCartMapper.selectOne(qw);
                if (c == null) {
                    return 0;
                }
                return c.getQuantity();
            } catch (Exception e) {
                System.err.println("获取用户ID失败，返回0: " + e.getMessage());
                return 0;
            }
        }

        // 使用LocalDataUtil中的用户信息
        QueryWrapper<MemberCart> qw = new QueryWrapper<>();
        qw.eq("member_id", member.getId());
        qw.eq("status", 1);
        qw.select("count(quantity) quantity");
        MemberCart c = memberCartMapper.selectOne(qw);
        if (c == null) {
            return 0;
        }
        return c.getQuantity();
    }

    public List<Long> mineCartIds() {
        // 使用LocalDataUtil获取用户信息，兼容开发模式
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        Long userId;
        if (member == null) {
            // 如果LocalDataUtil中没有用户信息，尝试使用SecurityUtils
            try {
                userId = SecurityUtils.getUserId();
            } catch (Exception e) {
                System.err.println("获取用户ID失败，返回空列表: " + e.getMessage());
                return new ArrayList<>();
            }
        } else {
            userId = member.getId();
        }

        QueryWrapper<MemberCart> qw = new QueryWrapper<>();
        qw.eq("member_id", userId);
        qw.eq("status", 1);
        qw.select("id");
        List<MemberCart> list = memberCartMapper.selectList(qw);
        return list.stream().map(MemberCart::getId).collect(Collectors.toList());
    }

    /**
     * 清空当前用户的购物车
     *
     * @return 删除的记录数
     */
    public int clearCart() {
        // 使用LocalDataUtil获取用户信息，兼容开发模式
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        Long userId;
        if (member == null) {
            // 如果LocalDataUtil中没有用户信息，尝试使用SecurityUtils
            try {
                userId = SecurityUtils.getUserId();
            } catch (Exception e) {
                System.err.println("获取用户ID失败，返回0: " + e.getMessage());
                return 0;
            }
        } else {
            userId = member.getId();
        }

        QueryWrapper<MemberCart> qw = new QueryWrapper<>();
        qw.eq("member_id", userId);
        qw.eq("status", 1);
        return memberCartMapper.delete(qw);
    }
}
