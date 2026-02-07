package com.overdue.h5.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.overdue.h5.config.SecurityUtil;
import com.overdue.h5.domain.dto.OrderProductListDTO;
import com.overdue.h5.domain.dto.PayNotifyMessageDTO;
import com.overdue.h5.domain.form.*;
import com.overdue.h5.domain.vo.*;
import com.overdue.manager.act.domain.entity.MemberCoupon;
import com.overdue.manager.act.service.IntegralHistoryService;
import com.overdue.manager.act.service.MemberCouponService;
import com.overdue.manager.oms.convert.AftersaleItemConvert;
import com.overdue.manager.oms.convert.OrderItemConvert;
import com.overdue.manager.oms.domain.entity.*;
import com.overdue.manager.oms.mapper.*;
import com.overdue.manager.oms.service.*;
import com.overdue.manager.pms.domain.entity.Product;
import com.overdue.manager.pms.domain.entity.Sku;
import com.overdue.manager.pms.mapper.ProductMapper;
import com.overdue.manager.pms.mapper.SkuMapper;
import com.overdue.manager.pms.service.ProductService;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.entity.MemberAddress;
import com.overdue.manager.ums.domain.entity.MemberCart;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import com.overdue.manager.ums.mapper.MemberAddressMapper;
import com.overdue.manager.ums.mapper.MemberCartMapper;
import com.overdue.manager.ums.mapper.MemberWechatMapper;
// import com.cyl.wechat.WechatPayData;
// import com.cyl.wechat.WechatPayService;
// import com.cyl.wechat.WechatPayUtil;
import com.overdue.wechat.WechatPayData;
import com.overdue.wechat.WechatPayService;
import java.util.stream.Stream;
import com.overdue.wechat.WechatPayUtil;
import com.github.pagehelper.PageHelper;
import com.overdue.common.constant.Constants;
import com.overdue.common.core.redis.OrderCountdownService;
import com.overdue.common.core.redis.RedisService;
import com.overdue.common.enums.AftersaleStatus;
import com.overdue.common.enums.OrderRefundStatus;
import com.overdue.common.enums.OrderStatus;
import com.overdue.common.utils.DateUtils;
import com.overdue.common.utils.IDGenerator;
import com.overdue.framework.config.LocalDataUtil;
import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class H5OrderService {

    @Autowired
    private MemberAddressMapper memberAddressMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private MemberCartMapper memberCartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderOperateHistoryMapper orderOperateHistoryMapper;

    @Autowired
    private PaymentPostProcessService paymentPostProcessService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private OrderOperateHistoryService orderOperateHistoryService;

    // @Autowired(required = false)
    // private WechatPayService wechatPayService;

    @Autowired
    private MemberWechatMapper memberWechatMapper;

    @Autowired
    private WechatPaymentHistoryMapper wechatPaymentHistoryMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrderCountdownService orderCountdownService;

    @Autowired
    private AftersaleMapper aftersaleMapper;

    @Autowired
    private AftersaleItemMapper aftersaleItemMapper;

    @Autowired
    private AftersaleItemConvert aftersaleItemConvert;

    @Autowired
    private OrderItemConvert orderItemConvert;

    @Autowired
    private IntegralHistoryService integralHistoryService;

    @Autowired
    private MemberCouponService memberCouponService;

    @Autowired
    private WechatPayService wechatPayService;

    @Autowired
    private WechatPayData wechatPayData;

    @Autowired
    private PickupCodeService pickupCodeService;

    @Autowired
    private LocalDeliveryCodeService localDeliveryCodeService;

    @Autowired
    private PointsOrderService pointsOrderService;

    @Autowired
    private com.overdue.wechat.WechatSubscribeMessageService wechatSubscribeMessageService;

    @Autowired
    private com.overdue.manager.ums.mapper.MemberMapper memberMapper;

    @Transactional
    public Long submit(OrderSubmitForm form) {
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        // 根据配送方式处理地址
        Long addressId = form.getAddressId();
        String deliveryMethod = form.getDeliveryMethod() != null ? form.getDeliveryMethod() : "express";
        MemberAddress memberAddress = null;

        if ("pickup".equals(deliveryMethod)) {
            // 到店取货时，使用商家地址
            memberAddress = getStoreAddress();
        } else if ("local".equals(deliveryMethod)) {
            // 同城配送需要用户地址
            if (addressId == null) {
                throw new RuntimeException("收货地址不能为空");
            }
            memberAddress = memberAddressMapper.selectById(addressId);
            if (memberAddress == null) {
                throw new RuntimeException("收货地址不能为空");
            }
        } else {
            // 快递配送需要用户地址
            if (addressId == null) {
                throw new RuntimeException("收货地址不能为空");
            }
            memberAddress = memberAddressMapper.selectById(addressId);
            if (memberAddress == null) {
                throw new RuntimeException("收货地址不能为空");
            }
        }
        // sku不能为空
        List<OrderProductListDTO> skuList = form.getSkuList();
        if (CollectionUtil.isEmpty(skuList)) {
            throw new RuntimeException("商品SKU信息不能为空");
        }
        // 将sku信息转换为 key：skuId ，value：购买数量
        Map<Long, Integer> skuQuantityMap = skuList.stream()
                .collect(Collectors.toMap(OrderProductListDTO::getSkuId, OrderProductListDTO::getQuantity,
                        (v1, v2) -> v2));
        // 查询所有sku信息
        Map<Long, Sku> querySkuMap = skuMapper
                .selectBatchIds(skuList.stream().map(OrderProductListDTO::getSkuId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(Sku::getId, it -> it, (v1, v2) -> v2));
        // 校验优惠券
        BigDecimal couponAmount = BigDecimal.ZERO;
        if (form.getMemberCouponId() != null) {
            MemberCoupon coupon = memberCouponService.selectValidCoupon(form.getMemberCouponId());
            if (coupon == null) {
                throw new RuntimeException("优惠券未找到");
            }
            // 将sku转换成products
            Map<Long, Product> products = new HashMap<>();
            querySkuMap.forEach((k, v) -> {
                Integer count = skuQuantityMap.get(k);
                Long productId = v.getProductId();
                Product product;
                BigDecimal amount = v.getPrice().multiply(BigDecimal.valueOf(count));
                if (products.containsKey(k)) {
                    product = products.get(k);
                    product.setPrice(amount.add(product.getPrice()));
                } else {
                    product = new Product();
                    product.setId(productId);
                    product.setPrice(amount);
                }
                products.put(k, product);
            });
            if (!memberCouponService.judgeCouponCanUse(coupon, products.values())) {
                throw new RuntimeException("优惠券未达到使用条件");
            }
            couponAmount = coupon.getCouponAmount();
        }
        // 计算商品总额、订单总额（订单总金额=商品总金额+运费）
        BigDecimal productTotalAmount = BigDecimal.ZERO;
        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        for (OrderProductListDTO dto : skuList) {
            if (!querySkuMap.containsKey(dto.getSkuId())) {
                throw new RuntimeException("商品SKU不存在");
            }
            Sku sku = querySkuMap.get(dto.getSkuId());
            Product product = productMapper.selectById(sku.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            if (Constants.PublishStatus.UNDERCARRIAGE.equals(product.getPublishStatus())) {
                throw new RuntimeException("商品" + product.getName() + "已下架");
            }
            if (sku.getStock() < skuQuantityMap.get(sku.getId())) {
                throw new RuntimeException("库存不足");
            }
            productTotalAmount = productTotalAmount
                    .add(sku.getPrice().multiply(BigDecimal.valueOf(skuQuantityMap.get(sku.getId()))));
            orderTotalAmount = orderTotalAmount
                    .add(sku.getPrice().multiply(BigDecimal.valueOf(skuQuantityMap.get(sku.getId()))));
            dto.setSku(sku);
            dto.setProduct(product);
        }

        // 添加运费到订单总金额
        BigDecimal freightAmount = form.getFreightAmount() != null ? form.getFreightAmount() : BigDecimal.ZERO;
        log.info("订单运费处理 - 前端传递运费: {}, 处理后运费: {}, 商品总金额: {}, 订单总金额: {}",
                form.getFreightAmount(), freightAmount, productTotalAmount, orderTotalAmount);
        orderTotalAmount = orderTotalAmount.add(freightAmount);
        log.info("订单运费处理 - 添加运费后订单总金额: {}", orderTotalAmount);
        LocalDateTime optTime = LocalDateTime.now();

        // 生成订单主键ID
        Long orderId = IDGenerator.generateId();

        // 生成商户订单号（用于微信支付的out_trade_no）
        String outTradeNo = generateOutTradeNo(orderId);

        // 创建订单
        Order order = new Order();
        // 注意：payId字段保留为空，在微信支付成功后设置为微信交易号
        order.setPayId(null);
        // 设置商户订单号
        order.setOutTradeNo(outTradeNo);
        order.setId(orderId);
        order.setOrderSn(this.getOrderIdPrefix() + orderId);
        order.setMemberId(member.getId());
        order.setMemberUsername(member.getNickname());
        order.setPayType(Constants.PayType.WECHAT);
        order.setCouponAmount(couponAmount);
        order.setMemberCouponId(form.getMemberCouponId());
        order.setTotalAmount(orderTotalAmount);
        order.setPurchasePrice(BigDecimal.ZERO);
        order.setFreightAmount(freightAmount);
        log.info("订单创建 - 设置订单金额: totalAmount={}, freightAmount={}, payAmount={}",
                orderTotalAmount, freightAmount, order.getPayAmount());
        BigDecimal subtract = orderTotalAmount.subtract(couponAmount);
        order.setPayAmount(subtract.compareTo(BigDecimal.ZERO) > 0 ? subtract : BigDecimal.ZERO);
        if (order.getPayAmount().compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(Constants.OrderStatus.SEND);
        } else {
            order.setStatus(Constants.OrderStatus.NOTPAID);
        }
        order.setAftersaleStatus(1);
        order.setReceiverName(memberAddress.getName());
        order.setReceiverPhone(memberAddress.getPhoneHidden());
        order.setReceiverPhoneEncrypted(memberAddress.getPhoneEncrypted());
        order.setReceiverPostCode(memberAddress.getPostCode());
        order.setReceiverProvince(memberAddress.getProvince());
        order.setReceiverCity(memberAddress.getCity());
        order.setReceiverDistrict(memberAddress.getDistrict());
        order.setReceiverProvinceId(memberAddress.getProvinceId());
        order.setReceiverCityId(memberAddress.getCityId());
        order.setReceiverDistrictId(memberAddress.getDistrictId());
        order.setReceiverDetailAddress(memberAddress.getDetailAddress());
        order.setNote(form.getNote());
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        // 设置配送方式
        order.setDeliveryMethod(form.getDeliveryMethod() != null ? form.getDeliveryMethod() : "express");
        // order.setPaymentTime(optTime);
        order.setCreateTime(optTime);
        order.setCreateBy(member.getId());
        int rows = orderMapper.insert(order);
        if (rows < 1) {
            throw new RuntimeException("订单新增失败");
        }
        // 保存orderItem
        orderItemService.saveOrderItem(member, optTime, orderId, skuList);
        skuList.forEach(item -> {
            // 减少sku的库存
            skuMapper.updateStockById(item.getSkuId(), LocalDateTime.now(), item.getQuantity());

            // 增加商品热度值
            Sku sku = querySkuMap.get(item.getSkuId());
            if (sku != null && sku.getProductId() != null) {
                productService.increaseHotness(sku.getProductId(), item.getQuantity());
            }
        });
        // 保存订单操作记录
        OrderOperateHistory orderOperateHistory = new OrderOperateHistory();
        orderOperateHistory.setOrderId(orderId);
        orderOperateHistory.setOrderSn(order.getOrderSn());
        orderOperateHistory.setOperateMan(member.getNickname());
        orderOperateHistory.setOrderStatus(Constants.OrderStatus.NOTPAID);
        orderOperateHistory.setCreateTime(optTime);
        orderOperateHistory.setCreateBy(member.getId());
        rows = orderOperateHistoryMapper.insert(orderOperateHistory);
        if (rows < 1) {
            throw new RuntimeException("保存订单操作记录失败");
        }
        // 若来源为购物车，删除购物车
        if (Constants.OrderFrom.CART.equals(form.getFrom())) {
            List<Long> skuIdList = skuList.stream().map(OrderProductListDTO::getSkuId).collect(Collectors.toList());
            LambdaUpdateWrapper<MemberCart> wrapper = Wrappers.lambdaUpdate();
            wrapper.eq(MemberCart::getMemberId, member.getId());
            wrapper.in(MemberCart::getSkuId, skuIdList);
            rows = memberCartMapper.delete(wrapper);
            if (rows < 1) {
                throw new RuntimeException("删除购物车失败");
            }
        }
        // 当前订单id，接入支付后可返回payId
        // 如果是使用了优惠券，更新优惠券状态
        if (form.getMemberCouponId() != null) {
            memberCouponService.updateCouponStatus(form.getMemberCouponId(), orderId);
        }

        // 如果订单需要支付（支付金额大于0），设置倒计时
        if (order.getPayAmount().compareTo(BigDecimal.ZERO) > 0) {
            orderCountdownService.setOrderCountdown(orderId, order.getOrderSn());
            log.info("订单创建成功，设置倒计时 - 订单ID: {}, 订单号: {}, 支付金额: {}", orderId, order.getOrderSn(), order.getPayAmount());
        } else {
            log.info("订单创建成功，无需支付 - 订单ID: {}, 订单号: {}, 支付金额: {}", orderId, order.getOrderSn(), order.getPayAmount());
        }

        // 返回商户订单号，用于支付流程
        return orderId;
    }

    public OrderCalcVO addOrderCheck(OrderCreateForm orderCreateForm) {
        OrderCalcVO res = new OrderCalcVO();
        List<SkuViewVO> skuList = new ArrayList<>();
        List<OrderProductListDTO> list = orderCreateForm.getSkuList();
        if (CollectionUtil.isEmpty(list)) {
            throw new RuntimeException("商品SKU信息不能为空");
        }

        // 地址校验（除了到店取货外，其他配送方式都需要地址）
        Long receiveAddressId = orderCreateForm.getReceiveAddressId();
        Integer deliveryType = orderCreateForm.getDeliveryType();

        // 如果明确指定了自提（deliveryType = 2），或者没有传递地址ID，则跳过地址校验
        if ((deliveryType != null && deliveryType == 2) || receiveAddressId == null) {
            // 自提（到店取货）不需要地址校验
            log.info("到店取货模式，跳过地址校验");
        } else {
            // 快递配送需要地址校验
            MemberAddress memberAddress = memberAddressMapper.selectById(receiveAddressId);
            if (memberAddress == null) {
                throw new RuntimeException("收货地址不能为空");
            }
        }
        // 将购买的sku信息转化为key：skuId value：数量
        Map<Long, Integer> quantityMap = list.stream().collect(
                Collectors.toMap(OrderProductListDTO::getSkuId, OrderProductListDTO::getQuantity, (v1, v2) -> v2));
        // 查询所有sku信息
        Set<Long> collect = list.stream().map(OrderProductListDTO::getSkuId).collect(Collectors.toSet());
        Map<Long, Sku> querySkuMap = skuMapper.selectBatchIds(collect).stream()
                .collect(Collectors.toMap(Sku::getId, it -> it, (v1, v2) -> v2));
        // 计算商品总金额、订单总金额
        BigDecimal productTotalAmount = BigDecimal.ZERO;
        BigDecimal orderTotalAmount = BigDecimal.ZERO;
        for (OrderProductListDTO dto : list) {
            if (!querySkuMap.containsKey(dto.getSkuId())) {
                throw new RuntimeException("商品SKU不存在");
            }
            Sku sku = querySkuMap.get(dto.getSkuId());
            // 查product
            Product product = productMapper.selectById(sku.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            if (Constants.PublishStatus.UNDERCARRIAGE.equals(product.getPublishStatus())) {
                throw new RuntimeException("商品" + product.getName() + "已下架");
            }
            if (sku.getStock() < quantityMap.get(sku.getId())) {
                throw new RuntimeException("库存不足");
            }
            BigDecimal addAmount = sku.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
            // 由于目前没有运费等数据，暂时订单总金额=商品总金额了
            productTotalAmount = productTotalAmount.add(addAmount);
            orderTotalAmount = orderTotalAmount.add(addAmount);
            // 封装sku信息
            SkuViewVO skuViewVO = new SkuViewVO();
            skuViewVO.setPic(product.getPic());
            skuViewVO.setPrice(sku.getPrice());
            skuViewVO.setProductId(product.getId());
            skuViewVO.setProductName(product.getName());
            skuViewVO.setQuantity(quantityMap.get(sku.getId()));
            skuViewVO.setSkuId(sku.getId());
            skuViewVO.setSpData(sku.getSpData());
            skuList.add(skuViewVO);
        }
        res.setSkuList(skuList);
        res.setOrderTotalAmount(orderTotalAmount);
        res.setProductTotalAmount(productTotalAmount);
        // 获取能使用的优惠券列表
        Map<Long, Product> products = new HashMap<>();
        querySkuMap.forEach((k, v) -> {
            Integer count = quantityMap.get(k);
            Long productId = v.getProductId();
            Product product;
            BigDecimal amount = v.getPrice().multiply(BigDecimal.valueOf(count));
            if (products.containsKey(k)) {
                product = products.get(k);
                product.setPrice(amount.add(product.getPrice()));
            } else {
                product = new Product();
                product.setId(productId);
                product.setPrice(amount);
            }
            products.put(k, product);
        });
        res.setCouponList(memberCouponService.getCanUseList(products.values()));
        return res;
    }

    private String getOrderIdPrefix() {
        LocalDateTime time = LocalDateTime.now();
        return time.format(DateTimeFormatter.ofPattern("yyMMdd")) + "-";
    }

    /**
     * 生成商户订单号（用于微信支付的out_trade_no）
     * 格式：WX + yyMMddHHmmss + 6位随机数
     * 例如：WX25082616301234567890
     * 
     * @param orderId 订单ID（用于确保唯一性）
     * @return 商户订单号
     */
    private String generateOutTradeNo(Long orderId) {
        LocalDateTime now = LocalDateTime.now();
        String timeStr = now.format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        // 使用订单ID的后6位作为唯一标识
        String orderIdSuffix = String.valueOf(orderId % 1000000);
        return "WX" + timeStr + String.format("%06d", Long.parseLong(orderIdSuffix));
    }

    /**
     * h5订单分页查询
     *
     * @param status   订单状态 -1->全部；0->待付款；1->待发货；2->待收货；-2->售后单
     * @param memberId 会员id
     * @param pageable 分页
     * @return 结果
     */
    public PageImpl<H5OrderVO> orderPage(Integer status, Long memberId, Pageable pageable) {
        // 如果全部且页数为1，看看有无待付款单
        List<H5OrderVO> unpaidOrderList = new ArrayList<>();
        if (Constants.H5OrderStatus.ALL.equals(status) && pageable.getPageNumber() == 0) {
            unpaidOrderList = orderMapper.orderPage(Constants.H5OrderStatus.UN_PAY, memberId);
        }
        if (pageable != null) {
            PageHelper.startPage(pageable.getPageNumber() + 1, pageable.getPageSize());
        }
        List<H5OrderVO> orderList = orderMapper.orderPage(status, memberId);
        long total = ((com.github.pagehelper.Page) orderList).getTotal();
        // 两个list都没数据那肯定返回空了
        if (CollectionUtil.isEmpty(unpaidOrderList) && CollectionUtil.isEmpty(orderList)) {
            return new PageImpl<>(Collections.EMPTY_LIST, pageable, total);
        }
        // 开始组装item了
        // 拿出所有orderId，查item，然后分组 by orderId
        List<Long> idList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(unpaidOrderList)) {
            idList.addAll(unpaidOrderList.stream().map(H5OrderVO::getOrderId).collect(Collectors.toList()));
        }
        if (CollectionUtil.isNotEmpty(orderList)) {
            idList.addAll(orderList.stream().map(H5OrderVO::getOrderId).collect(Collectors.toList()));
        }
        QueryWrapper<OrderItem> orderItemQw = new QueryWrapper<>();
        orderItemQw.in("order_id", idList);
        Map<Long, List<OrderItem>> orderItemMap = orderItemMapper.selectList(orderItemQw).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
        orderList.addAll(0, unpaidOrderList);
        orderList.forEach(item -> {
            item.setOrderItemList(orderItemMap.get(item.getOrderId()));
        });
        return new PageImpl<>(orderList, pageable, total);
    }

    public H5OrderVO orderDetail(Long orderId) {
        H5OrderVO order = orderMapper.selectOrderDetail(orderId);
        if (order == null) {
            throw new RuntimeException("未查询到该订单");
        }
        // 组装item
        QueryWrapper<OrderItem> orderItemQw = new QueryWrapper<>();
        orderItemQw.eq("order_id", orderId);
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQw);
        order.setOrderItemList(orderItemList);
        // 如果未付款，计算倒计时
        if (Constants.OrderStatus.NOTPAID.equals(order.getStatus())) {
            // 订单超时时间900s，后面可以配置到字典等
            Integer time = 900;
            Date addDate = Date
                    .from(order.getCreateTime().plusSeconds(time).atZone(ZoneId.systemDefault()).toInstant());
            if (addDate.after(new Date())) {
                order.setTimeToPay(addDate.getTime());
            }
        }
        return order;
    }

    /**
     * 通过商户订单号查询订单详情（支付流程使用）
     * 
     * @param outTradeNo 商户订单号
     * @return 订单详情
     */
    public H5OrderVO orderDetailByOutTradeNo(String outTradeNo) {
        // 先通过商户订单号查找订单
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("out_trade_no", outTradeNo);
        qw.eq("delete_status", 0);
        Order order = orderMapper.selectOne(qw);
        if (order == null) {
            throw new RuntimeException("未查询到该订单");
        }
        // 再通过orderId查询详情
        return orderDetail(order.getId());
    }

    /**
     * 通过支付ID查询订单详情
     * 
     * @param payId 支付ID
     * @return 订单详情
     */
    public H5OrderVO orderDetailByPayId(String payId) {
        // 先通过payId查找订单
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("pay_id", payId);
        qw.eq("delete_status", 0);
        Order order = orderMapper.selectOne(qw);
        if (order == null) {
            throw new RuntimeException("未查询到该订单");
        }
        // 再通过orderId查询详情
        return orderDetail(order.getId());
    }

    @Transactional
    public void orderCompleteByJob(List<Order> idList) {
        idList.forEach(order -> {
            LocalDateTime optDate = LocalDateTime.now();
            OrderItem queryOrderItem = new OrderItem();
            queryOrderItem.setOrderId(order.getId());
            // 更新订单
            order.setStatus(Constants.H5OrderStatus.COMPLETED);
            order.setReceiveTime(optDate);
            order.setConfirmStatus(1);
            order.setUpdateTime(optDate);
            order.setUpdateBy(null);
            orderMapper.updateById(order);
            // 创建订单操作记录
            OrderOperateHistory optHistory = new OrderOperateHistory();
            optHistory.setOrderId(order.getId());
            optHistory.setOrderSn(order.getOrderSn());
            optHistory.setOperateMan("后台管理员");
            optHistory.setOrderStatus(Constants.H5OrderStatus.COMPLETED);
            optHistory.setCreateTime(optDate);
            optHistory.setUpdateTime(optDate);
            orderOperateHistoryMapper.insert(optHistory);
        });
    }

    @Transactional
    public String orderComplete(Long orderId) {
        LocalDateTime optDate = LocalDateTime.now();
        Order order = orderMapper.selectById(orderId);
        OrderItem queryOrderItem = new OrderItem();
        queryOrderItem.setOrderId(orderId);
        List<OrderItem> orderItemList = orderItemMapper.selectByEntity(queryOrderItem);
        if (order == null || CollectionUtil.isEmpty(orderItemList)) {
            throw new RuntimeException("未查询到订单信息");
        }
        // 只有【待收货】状态才能确认
        if (!order.getStatus().equals(Constants.H5OrderStatus.DELIVERED)) {
            throw new RuntimeException("订单状态已改变，请刷新");
        }
        order.setStatus(Constants.H5OrderStatus.COMPLETED);
        order.setReceiveTime(optDate);
        order.setConfirmStatus(1);
        order.setUpdateTime(optDate);
        order.setUpdateBy(SecurityUtil.getLocalMember().getId());
        orderMapper.updateById(order);
        // 创建订单操作记录
        OrderOperateHistory optHistory = new OrderOperateHistory();
        optHistory.setOrderId(order.getId());
        optHistory.setOrderSn(order.getOrderSn());
        optHistory.setOperateMan("" + order.getMemberId());
        optHistory.setOrderStatus(Constants.H5OrderStatus.COMPLETED);
        optHistory.setCreateTime(optDate);
        optHistory.setCreateBy(order.getMemberId());
        optHistory.setUpdateBy(order.getMemberId());
        optHistory.setUpdateTime(optDate);
        orderOperateHistoryMapper.insert(optHistory);
        return order.getOrderSn();
    }

    /**
     * 统计待付款、待发货、待收货和售后订单数量
     *
     * @param memberId
     * @return
     */
    public CountOrderVO orderNumCount(Long memberId) {
        return orderMapper.countByStatusAndMemberId(memberId);
    }

    @Transactional
    public String orderBatchCancel(CancelOrderForm request, Long userId) {
        LocalDateTime optDate = LocalDateTime.now();
        if (CollectionUtil.isEmpty(request.getIdList())) {
            throw new RuntimeException("未指定需要取消的订单号");
        }

        // 添加调试信息
        log.info("订单取消请求 - 用户ID: {}, 订单ID列表: {}", userId, request.getIdList());

        QueryWrapper<Order> orderQw = new QueryWrapper<>();
        orderQw.in("id", request.getIdList());
        List<Order> orderList = orderMapper.selectList(orderQw);
        if (orderList.size() < request.getIdList().size()) {
            throw new RuntimeException("未查询到订单信息");
        }

        // 添加调试信息
        log.info("查询到的订单信息: {}", orderList.stream()
                .map(order -> String.format("订单ID:%d, 状态:%d, 用户ID:%d", order.getId(), order.getStatus(),
                        order.getMemberId()))
                .collect(Collectors.joining(", ")));

        // 检查用户权限，确保只能取消自己的订单
        long unauthorizedCount = orderList.stream().filter(it -> !userId.equals(it.getMemberId())).count();
        if (unauthorizedCount > 0) {
            log.error("订单取消失败 - 用户权限检查失败: 用户{}尝试取消不属于自己的订单", userId);
            throw new RuntimeException("只能取消自己的订单");
        }

        // 查orderItem
        QueryWrapper<OrderItem> qw = new QueryWrapper<>();
        qw.in("order_id", request.getIdList());
        List<OrderItem> orderItem = orderItemMapper.selectList(qw);
        if (CollectionUtil.isEmpty(orderItem)) {
            throw new RuntimeException("未查询到订单信息");
        }
        long count = orderList.stream().filter(it -> !Constants.H5OrderStatus.UN_PAY.equals(it.getStatus()) &&
                !Constants.H5OrderStatus.NOT_DELIVERED.equals(it.getStatus())).count();
        if (count > 0) {
            // 添加调试信息
            String orderStatusInfo = orderList.stream()
                    .map(order -> String.format("订单ID:%d, 状态:%d", order.getId(), order.getStatus()))
                    .collect(Collectors.joining(", "));
            log.error("订单取消失败 - 订单状态检查失败: {}", orderStatusInfo);
            throw new RuntimeException("只能取消待付款或待发货的订单，当前订单状态不允许取消");
        }
        List<OrderOperateHistory> addHistoryList = new ArrayList<>();
        orderList.forEach(item -> {
            item.setStatus(Constants.H5OrderStatus.CLOSED);
            item.setUpdateTime(optDate);
            item.setUpdateBy(userId);
            OrderOperateHistory history = new OrderOperateHistory();
            history.setOrderId(item.getId());
            history.setOrderSn(item.getOrderSn());
            history.setOperateMan(userId == null ? "后台管理员" : "" + item.getMemberId());
            history.setOrderStatus(Constants.H5OrderStatus.CLOSED);
            history.setCreateTime(optDate);
            history.setCreateBy(userId);
            history.setUpdateBy(userId);
            history.setUpdateTime(optDate);
            addHistoryList.add(history);

        });
        // 取消订单
        int rows = orderMapper.cancelBatch(orderList);
        if (rows < 1) {
            throw new RuntimeException("更改订单状态失败");
        }
        orderItem.stream().collect(Collectors.groupingBy(it -> it.getSkuId())).forEach((k, v) -> {
            AtomicReference<Integer> totalCount = new AtomicReference<>(0);
            v.forEach(it -> totalCount.updateAndGet(v1 -> v1 + it.getQuantity()));
            skuMapper.updateStockById(k, optDate, -1 * totalCount.get());
        });

        // 创建订单操作记录
        boolean flag = orderOperateHistoryService.saveBatch(addHistoryList);
        if (!flag) {
            throw new RuntimeException("创建订单操作记录失败");
        }
        // 判断是否使用优惠券，有的话，把优惠券还回去
        List<Long> couponIdList = orderList.stream().filter(it -> it.getMemberCouponId() != null)
                .map(Order::getMemberCouponId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(couponIdList)) {
            memberCouponService.backCoupon(couponIdList);
        }
        return "取消订单成功";
    }

    /**
     * 订单支付
     *
     * @param req 支付请求
     * @return
     */
    public OrderPayVO orderPay(OrderPayForm req) {
        // 使用orderId查询订单
        Order order = orderMapper.selectById(req.getOrderId());
        if (Objects.isNull(order)) {
            throw new RuntimeException("没有待支付的订单");
        }
        QueryWrapper<MemberWechat> memberWechatQw = new QueryWrapper<>();
        memberWechatQw.eq("member_id", req.getMemberId());
        MemberWechat memberWechat = memberWechatMapper.selectOne(memberWechatQw);
        if (memberWechat == null) {
            throw new RuntimeException("获取用户openId失败");
        }
        String openId = null;
        String appId = null;
        // 公众号支付流程
        if (req.getWechatType() == 1) {
            if (StrUtil.isBlank(memberWechat.getOpenid())) {
                throw new RuntimeException("获取用户openId失败");
            }
            openId = memberWechat.getOpenid();
        }
        // 小程序支付流程
        if (req.getWechatType() == 2) {
            if (StrUtil.isBlank(memberWechat.getRoutineOpenid())) {
                throw new RuntimeException("获取用户openId失败");
            }
            openId = memberWechat.getRoutineOpenid();
        }
        QueryWrapper<OrderItem> orderItemQw = new QueryWrapper<>();
        orderItemQw.eq("order_id", order.getId());
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQw);
        String orderDesc = orderItemList.get(0).getProductName().substring(0,
                Math.min(40, orderItemList.get(0).getProductName().length()));
        // 保存微信支付历史
        LocalDateTime optDate = LocalDateTime.now();
        QueryWrapper<WechatPaymentHistory> wxPaymentQw = new QueryWrapper<>();
        wxPaymentQw.eq("order_id", order.getId());
        wxPaymentQw.eq("op_type", Constants.PaymentOpType.PAY);
        WechatPaymentHistory wechatPaymentHistory = wechatPaymentHistoryMapper.selectOne(wxPaymentQw);
        if (wechatPaymentHistory == null) {
            wechatPaymentHistory = new WechatPaymentHistory();
            wechatPaymentHistory.setOrderId(order.getId());
            // 保存商户订单号
            wechatPaymentHistory.setOutTradeNo(order.getOutTradeNo());
            wechatPaymentHistory.setMemberId(req.getMemberId());
            wechatPaymentHistory.setOpenid(openId);
            wechatPaymentHistory.setTitle(orderItemList.get(0).getProductName());
            wechatPaymentHistory.setMoney(order.getPayAmount());
            wechatPaymentHistory.setOpType(Constants.PaymentOpType.PAY);
            wechatPaymentHistory.setPaymentStatus(0);
            wechatPaymentHistory.setCreateBy(req.getMemberId());
            wechatPaymentHistory.setCreateTime(optDate);
            wechatPaymentHistory.setUpdateBy(req.getMemberId());
            wechatPaymentHistory.setUpdateTime(optDate);
            wechatPaymentHistoryMapper.insert(wechatPaymentHistory);
        } else {
            wechatPaymentHistory.setMoney(order.getPayAmount());
            // 更新商户订单号
            wechatPaymentHistory.setOutTradeNo(order.getOutTradeNo());
            wechatPaymentHistoryMapper.updateById(wechatPaymentHistory);
        }
        // 调用微信支付接口获取prepayId
        String prepayId;
        try {
            // 计算支付金额（分）
            int totalAmount = order.getPayAmount().multiply(new BigDecimal(100)).intValue();

            // 根据支付类型设置appId
            if (req.getWechatType() == 1) {
                // 公众号支付
                appId = wechatPayData.getAppId();
            } else {
                // 小程序支付
                appId = wechatPayData.getMiniProgramAppId();
            }

            prepayId = wechatPayService.jsapiPay(
                    order.getOutTradeNo(), // 使用商户订单号作为orderNo
                    orderDesc,
                    totalAmount,
                    openId,
                    req.getMemberId(),
                    appId);

            log.info("微信支付prepayId获取成功: {}", prepayId);
        } catch (Exception e) {
            log.error("微信支付prepayId获取失败", e);
            throw new RuntimeException("支付接口调用失败: " + e.getMessage());
        }
        OrderPayVO response = new OrderPayVO();
        response.setPayType(2);
        String nonceStr = WechatPayUtil.generateNonceStr();
        long timeStamp = WechatPayUtil.getCurrentTimestamp();
        prepayId = "prepay_id=" + prepayId;
        String signType = "RSA";
        String paySign = null;
        String signatureStr = Stream.of(appId, String.valueOf(timeStamp), nonceStr, prepayId)
                .collect(Collectors.joining("\n", "", "\n"));
        try {
            paySign = WechatPayUtil.getSign(signatureStr, wechatPayData.getPrivateKeyPath());
            log.info("微信支付签名生成成功");
        } catch (Exception e) {
            log.error("微信支付签名生成失败", e);
            throw new RuntimeException("支付签名生成失败: " + e.getMessage());
        }
        response.setAppId(appId);
        response.setTimeStamp(String.valueOf(timeStamp));
        response.setNonceStr(nonceStr);
        response.setSignType(signType);
        response.setPackage_(prepayId);
        response.setPaySign(paySign);
        return response;
    }

    /**
     * 支付回调方法
     *
     * @param messageDTO
     * @return
     */
    @Transactional
    public ResponseEntity<String> payCallBack(PayNotifyMessageDTO messageDTO) {
        log.info("【订单支付回调】" + JSONObject.toJSON(messageDTO));
        String redisKey = "h5_oms_order_pay_notify_" + messageDTO.getOutTradeNo();
        String redisValue = messageDTO.getOutTradeNo() + "_" + System.currentTimeMillis();
        LocalDateTime optDate = LocalDateTime.now();
        try {
            redisService.lock(redisKey, redisValue, 60);
            // 先判断回信回调的是否未success
            if (!Transaction.TradeStateEnum.SUCCESS.equals(messageDTO.getTradeStatus())) {
                log.error("【订单支付回调】订单状态不是支付成功状态" + messageDTO.getTradeStatus());
                throw new RuntimeException();
            }
            QueryWrapper<WechatPaymentHistory> paymentWrapper = new QueryWrapper<>();
            paymentWrapper.eq("out_trade_no", messageDTO.getOutTradeNo().toString());
            paymentWrapper.eq("op_type", Constants.PaymentOpType.PAY);
            WechatPaymentHistory paymentHistory = wechatPaymentHistoryMapper.selectOne(paymentWrapper);
            if (!paymentHistory.getPaymentStatus().equals(Constants.PaymentStatus.INCOMPLETE)) {
                log.info("【订单支付回调】支付订单不是未支付状态，不再处理" + "orderId" + paymentHistory.getOrderId() + "status"
                        + paymentHistory.getPaymentStatus());
                throw new RuntimeException();
            }
            QueryWrapper<Order> orderQw = new QueryWrapper<>();
            // 使用商户订单号查询
            orderQw.eq("out_trade_no", messageDTO.getOutTradeNo());
            orderQw.eq("status", OrderStatus.UN_PAY.getType());
            List<Order> orderList = orderMapper.selectList(orderQw);
            orderList.forEach(order -> {
                order.setPaymentTime(
                        messageDTO.getPayTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                order.setStatus(OrderStatus.NOT_DELIVERED.getType());
                // 设置微信支付交易号到payId字段
                order.setPayId(messageDTO.getTradeNo());

                // 如果是到店取货订单，生成取件码
                if ("pickup".equals(order.getDeliveryMethod())) {
                    String pickupCode = pickupCodeService.generatePickupCode();
                    order.setPickupCode(pickupCode);
                    log.info("到店取货订单支付成功，生成取件码 - 订单ID: {}, 订单号: {}, 取件码: {}",
                            order.getId(), order.getOrderSn(), pickupCode);
                }

                // 如果是同城配送订单，生成同城配送号码
                if ("local".equals(order.getDeliveryMethod())) {
                    String localDeliveryCode = localDeliveryCodeService.generateLocalDeliveryCode();
                    order.setLocalDeliveryCode(localDeliveryCode);
                    log.info("同城配送订单支付成功，生成同城配送号码 - 订单ID: {}, 订单号: {}, 同城配送号码: {}",
                            order.getId(), order.getOrderSn(), localDeliveryCode);
                }

                orderMapper.updateById(order);

                OrderOperateHistory optHistory = new OrderOperateHistory();
                optHistory.setOrderId(order.getId());
                optHistory.setOrderSn(order.getOrderSn());
                optHistory.setOperateMan("" + order.getMemberId());
                optHistory.setOrderStatus(OrderStatus.NOT_DELIVERED.getType());
                optHistory.setCreateTime(optDate);
                optHistory.setCreateBy(order.getMemberId());
                optHistory.setUpdateBy(order.getMemberId());
                optHistory.setUpdateTime(optDate);
                orderOperateHistoryMapper.insert(optHistory);

                // 设置MDC上下文
                MDC.put("traceId", messageDTO.getOutTradeNo().toString());
                MDC.put("orderId", String.valueOf(order.getId()));
                MDC.put("orderSn", order.getOrderSn());

                try {
                    // 异步处理所有支付后操作
                    paymentPostProcessService.handleAllPostPaymentOperationsAsync(order);

                    log.info("支付回调核心处理完成，异步操作已启动 - 订单ID: {}, 订单号: {}",
                            order.getId(), order.getOrderSn());

                    // 发送订单通知给经销商
                    try {
                        // 获取订单商品信息
                        List<OrderItem> orderItems = orderItemMapper.selectList(
                                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));

                        // 构建商品名称列表
                        String productNames = orderItems.stream()
                                .map(OrderItem::getProductName)
                                .limit(3)
                                .collect(Collectors.joining("、"));

                        if (orderItems.size() > 3) {
                            productNames += "等";
                        }

                        // 获取会员信息
                        Member orderMember = memberMapper.selectById(order.getMemberId());
                        String memberName = orderMember != null ? orderMember.getNickname() : "未知用户";

                        // 发送通知给经销商
                        int notifyCount = wechatSubscribeMessageService.sendNewOrderNotifyToDealer(
                                order.getId(),
                                order.getOrderSn(),
                                memberName,
                                order.getPayAmount().toString(),
                                productNames);

                        log.info("订单支付成功，已发送通知给{}位经销商 - 订单ID: {}, 订单号: {}",
                                notifyCount, order.getId(), order.getOrderSn());
                    } catch (Exception e) {
                        log.error("发送经销商通知失败，但不影响订单处理 - 订单ID: {}, 订单号: {}",
                                order.getId(), order.getOrderSn(), e);
                    }

                } finally {
                    MDC.clear();
                }
            });
            UpdateWrapper<WechatPaymentHistory> paymentHistoryUpdateWrapper = new UpdateWrapper<>();
            paymentHistoryUpdateWrapper.eq("out_trade_no", messageDTO.getOutTradeNo().toString())
                    .set("payment_id", messageDTO.getTradeNo())
                    .set("payment_status", Constants.PaymentStatus.COMPLETE).set("update_time", optDate);
            wechatPaymentHistoryMapper.update(null, paymentHistoryUpdateWrapper);
        } catch (Exception e) {
            log.error("订单支付回调异常", e);
            throw new RuntimeException("订单支付回调异常");
        } finally {
            try {
                redisService.unLock(redisKey, redisValue);
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return ResponseEntity.ok("订单支付回调成功");
    }

    /**
     * 申请售后
     *
     * @param applyRefundForm
     * @return
     */
    @Transactional
    public Order applyRefund(ApplyRefundForm applyRefundForm) {
        Order order = orderMapper.selectById(applyRefundForm.getOrderId());
        // 是否符合售后条件
        this.checkIfCanApplyRefund(order);
        LocalDateTime optDate = LocalDateTime.now();
        Long memberId = order.getMemberId();
        // 创建售后单aftersale
        Aftersale addAftersale = new Aftersale();
        addAftersale.setId(IDGenerator.generateId());
        addAftersale.setMemberId(order.getMemberId());
        addAftersale.setOrderId(order.getId());
        addAftersale.setReturnAmount(order.getPayAmount());
        addAftersale.setType(applyRefundForm.getApplyRefundType());
        addAftersale.setStatus(AftersaleStatus.APPLY.getType());
        addAftersale.setReason(applyRefundForm.getReason());
        addAftersale.setQuantity(applyRefundForm.getQuantity());
        addAftersale.setReason(applyRefundForm.getReason());
        addAftersale.setDescription(applyRefundForm.getDescription());
        addAftersale.setProofPics(applyRefundForm.getProofPics());
        addAftersale.setCreateTime(optDate);
        addAftersale.setCreateBy(memberId);
        addAftersale.setUpdateTime(optDate);
        addAftersale.setUpdateBy(memberId);
        int rows = aftersaleMapper.insert(addAftersale);
        if (rows != 1) {
            throw new RuntimeException("插入订单售后失败");
        }
        // 创建aftersale item
        QueryWrapper<OrderItem> orderItemQw = new QueryWrapper<>();
        orderItemQw.eq("order_id", order.getId());
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQw);
        List<AftersaleItem> addAftersaleItemList = new ArrayList<>();
        orderItemList.forEach(orderItem -> {
            AftersaleItem aftersaleItem = new AftersaleItem();
            aftersaleItem.setMemberId(memberId);
            aftersaleItem.setAftersaleId(addAftersale.getId());
            aftersaleItem.setOrderId(orderItem.getOrderId());
            aftersaleItem.setOrderItemId(orderItem.getId());
            aftersaleItem
                    .setReturnAmount(orderItem.getSalePrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
            aftersaleItem.setQuantity(orderItem.getQuantity());
            aftersaleItem.setCreateTime(optDate);
            aftersaleItem.setCreateBy(memberId);
            aftersaleItem.setUpdateTime(optDate);
            aftersaleItem.setUpdateBy(memberId);
            addAftersaleItemList.add(aftersaleItem);
        });
        rows = aftersaleItemMapper.insertBatch(addAftersaleItemList);
        if (rows < 1) {
            throw new RuntimeException("创建售后订单item失败");
        }
        // 更新订单
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", order.getId()).set("aftersale_status", OrderRefundStatus.APPLY.getType())
                .set("update_time", optDate)
                .set("update_by", memberId);
        rows = orderMapper.update(null, updateWrapper);
        if (rows < 1) {
            throw new RuntimeException("修改订单状态失败");
        }
        // 创建订单操作记录
        OrderOperateHistory optHistory = new OrderOperateHistory();
        optHistory.setOrderId(order.getId());
        optHistory.setOrderSn(order.getOrderSn());
        optHistory.setOperateMan("" + memberId);
        optHistory.setOrderStatus(11);
        optHistory.setCreateTime(optDate);
        optHistory.setCreateBy(memberId);
        optHistory.setUpdateBy(memberId);
        optHistory.setUpdateTime(optDate);
        rows = orderOperateHistoryMapper.insert(optHistory);
        if (rows < 1) {
            throw new RuntimeException("创建订单操作记录失败");
        }
        return order;
    }

    /**
     * check是否能售后 可售后的状态为：待发货、待收货、已完成
     *
     * @param order 订单
     */
    private void checkIfCanApplyRefund(Order order) {
        if (order == null) {
            throw new RuntimeException("为查询到订单信息");
        }
        Integer status = order.getStatus();
        boolean flag = OrderStatus.NOT_DELIVERED.getType().equals(status)
                || OrderStatus.DELIVERED.getType().equals(status)
                || OrderStatus.COMPLETE.getType().equals(status);
        if (!flag) {
            throw new RuntimeException("该订单无法申请售后");
        }
        if (OrderStatus.COMPLETE.getType().equals(order.getStatus()) &&
                DateUtils.betweenDay(LocalDateTime.now(), order.getReceiveTime()) > 7) {
            throw new RuntimeException("订单确认收货时间已超过7天，无法申请售后");
        }
        if (OrderRefundStatus.APPLY.getType().equals(order.getAftersaleStatus())
                || OrderRefundStatus.WAIT.getType().equals(order.getAftersaleStatus())) {
            throw new RuntimeException("售后正在处理中");
        }
    }

    /**
     * 取消售后
     *
     * @param orderId 订单id
     * @return
     */
    @Transactional
    public String cancelRefund(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("未查询到该订单");
        }
        // 查询是否有（待处理和退货中）售后单
        QueryWrapper<Aftersale> aftersaleQw = new QueryWrapper<>();
        aftersaleQw.eq("order_id", orderId);
        aftersaleQw.in("status", Arrays.asList(AftersaleStatus.APPLY.getType(), AftersaleStatus.WAIT.getType()));
        Aftersale aftersale = aftersaleMapper.selectOne(aftersaleQw);
        if (aftersale == null) {
            throw new RuntimeException("无售后单");
        }
        if (OrderRefundStatus.SUCCESS.getType().equals(order.getAftersaleStatus())) {
            throw new RuntimeException("已退款成功");
        }
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
        LocalDateTime optDate = LocalDateTime.now();
        // 更新售后单状态
        UpdateWrapper<Aftersale> aftersaleUpdateWrapper = new UpdateWrapper<>();
        aftersaleUpdateWrapper.eq("id", aftersale.getId());
        aftersaleUpdateWrapper.set("status", AftersaleStatus.CANCEL.getType());
        aftersaleUpdateWrapper.set("update_time", optDate);
        aftersaleUpdateWrapper.set("update_by", member.getId());
        int rows = aftersaleMapper.update(null, aftersaleUpdateWrapper);
        if (rows < 1) {
            throw new RuntimeException("更新售后单失败");
        }
        // 更新订单售后状态
        // 更新订单
        UpdateWrapper<Order> updateOrderWrapper = new UpdateWrapper<>();
        updateOrderWrapper.eq("id", orderId)
                .set("aftersale_status", OrderRefundStatus.NO_REFUND.getType()).set("update_time", optDate)
                .set("update_by", member.getId());
        rows = orderMapper.update(null, updateOrderWrapper);
        if (rows != 1) {
            throw new RuntimeException("更新订单状态失败");
        }
        return "售后取消成功";
    }

    /**
     * 售后订单详情
     *
     * @param orderId 订单id
     * @return
     */
    public AftersaleRefundInfoVO refundOrderDetail(Long orderId) {
        QueryWrapper<Aftersale> aftersaleQw = new QueryWrapper<>();
        aftersaleQw.eq("order_id", orderId);
        aftersaleQw.orderByDesc("create_time");
        aftersaleQw.last("limit 1");
        Aftersale aftersale = aftersaleMapper.selectOne(aftersaleQw);
        if (aftersale == null) {
            throw new RuntimeException("未查询到售后订单");
        }
        // 查一下售后订单item
        QueryWrapper<AftersaleItem> aftersaleItemQw = new QueryWrapper<>();
        aftersaleItemQw.eq("aftersale_id", aftersale.getId());
        List<AftersaleItem> aftersaleItemList = aftersaleItemMapper.selectList(aftersaleItemQw);
        List<Long> orderItemIdList = aftersaleItemList.stream().map(AftersaleItem::getOrderItemId)
                .collect(Collectors.toList());
        // 再去查orderItem
        QueryWrapper<OrderItem> orderItemQw = new QueryWrapper<>();
        orderItemQw.in("id", orderItemIdList);
        List<OrderItem> orderItemList = orderItemMapper.selectList(orderItemQw);
        AftersaleRefundInfoVO vo = new AftersaleRefundInfoVO();
        BeanUtils.copyProperties(aftersale, vo);
        vo.setAftersaleItemList(aftersaleItemConvert.dos2vos(aftersaleItemList));
        vo.setOrderItemList(orderItemConvert.dos2vos(orderItemList));
        return vo;
    }

    public Order selectById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    /**
     * 根据用户ID和订单状态统计订单数量
     * 
     * @param memberId 用户ID
     * @param status   订单状态
     * @return 订单数量
     */
    public int countOrderByMemberAndStatus(Long memberId, Integer status) {
        try {
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("member_id", memberId);
            queryWrapper.eq("status", status);
            // 未删除的订单
            queryWrapper.eq("del_flag", 0);

            // 对于状态0、1、2的订单，要求aftersale_status=1，与订单列表查询保持一致
            // 注意：状态3（已完成）在订单列表查询中没有aftersale_status限制，所以这里也不加限制
            if (status >= 0 && status <= 2) {
                queryWrapper.eq("aftersale_status", 1);
            }

            Integer count = orderMapper.selectCount(queryWrapper);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计用户{}状态{}的订单数量失败", memberId, status, e);
            return 0;
        }
    }

    /**
     * 获取用户所有状态的订单数量统计
     * 
     * @param memberId 用户ID
     * @return 包含各状态订单数量的Map
     */
    public Map<String, Integer> getOrderCountByMember(Long memberId) {
        Map<String, Integer> result = new HashMap<>();
        try {
            // 统计各状态订单数量
            result.put("waitPayCount", countOrderByMemberAndStatus(memberId, 0));
            result.put("waitDeliveryCount", countOrderByMemberAndStatus(memberId, 1));
            result.put("waitReceiveCount", countOrderByMemberAndStatus(memberId, 2));
            result.put("completeCount", countOrderByMemberAndStatus(memberId, 3));
            result.put("cancelledCount", countOrderByMemberAndStatus(memberId, 4));

            // 计算总数
            int totalCount = result.values().stream().mapToInt(Integer::intValue).sum();
            result.put("totalCount", totalCount);

        } catch (Exception e) {
            log.error("获取用户{}订单数量统计失败", memberId, e);
            // 返回默认值
            result.put("waitPayCount", 0);
            result.put("waitDeliveryCount", 0);
            result.put("waitReceiveCount", 0);
            result.put("completeCount", 0);
            result.put("cancelledCount", 0);
            result.put("totalCount", 0);
        }
        return result;
    }

    /**
     * 根据用户ID和订单状态统计积分商城订单数量
     * 
     * @param memberId 用户ID
     * @param status   订单状态
     * @return 订单数量
     */
    public int countPointsOrderByMemberAndStatus(Long memberId, Integer status) {
        try {
            QueryWrapper<com.overdue.manager.oms.domain.entity.PointsOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("member_id", memberId);
            queryWrapper.eq("status", status);
            // 未删除的订单
            queryWrapper.eq("delete_status", 0);

            Integer count = pointsOrderService.getPointsOrderMapper().selectCount(queryWrapper);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("统计用户{}状态{}的积分商城订单数量失败", memberId, status, e);
            return 0;
        }
    }

    /**
     * 获取商家地址（用于到店取货）
     * 使用用户信息作为收货人，使用商家地址作为收货地址
     * 
     * @return 商家地址信息
     */
    private MemberAddress getStoreAddress() {
        // 获取当前用户信息
        Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);

        MemberAddress storeAddress = new MemberAddress();
        // 使用用户信息作为收货人
        storeAddress.setName(member.getNickname() != null ? member.getNickname() : "用户");
        storeAddress.setPhoneHidden(
                member.getPhone() != null ? member.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") : "");
        storeAddress.setPhoneEncrypted(member.getPhone() != null ? member.getPhone() : "");

        // 使用商家地址信息
        storeAddress.setProvince("广西壮族自治区");
        storeAddress.setCity("百色市");
        storeAddress.setDistrict("靖西市");
        storeAddress.setDetailAddress("新靖镇凤凰路1254号绿城美丽园6号楼地下一层6-1、6-2号商铺");
        storeAddress.setPostCode("533800");
        // 设置地区ID（可以根据实际需要设置）
        // 广西壮族自治区
        storeAddress.setProvinceId(450000L);
        // 百色市
        storeAddress.setCityId(451000L);
        // 靖西市
        storeAddress.setDistrictId(451081L);
        return storeAddress;
    }
}
