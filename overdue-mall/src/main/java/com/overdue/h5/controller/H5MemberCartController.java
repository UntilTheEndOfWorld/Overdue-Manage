package com.overdue.h5.controller;

import com.overdue.h5.service.H5MemberCartService;
import com.overdue.manager.ums.convert.MemberCartConvert;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.entity.MemberCart;
import com.overdue.manager.ums.domain.query.MemberCartQuery;
import com.overdue.manager.ums.domain.vo.MemberCartVO;
import com.overdue.manager.ums.mapper.MemberCartMapper;
import com.overdue.common.constant.Constants;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.framework.config.LocalDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/h5/cart")
public class H5MemberCartController {
    @Autowired
    private H5MemberCartService memberCartService;

    // 开发模式标志 - 已注释，确保使用正常业务逻辑
    // private static final boolean DEV_MODE = false;

    /**
     * 当前用户的购物车商品数量
     *
     * @return
     */
    @GetMapping("goodscount")
    public ResponseEntity<Integer> goodscount() {
        return ResponseEntity.ok(memberCartService.mineCartNum());
    }

    /**
     * 当前用户的购物车商品id列表
     *
     * @return
     */
    @GetMapping("cart-ids")
    public ResponseEntity<List<Long>> cartIds() {
        return ResponseEntity.ok(memberCartService.mineCartIds());
    }

    /**
     * 添加购物车
     *
     * @return 购物车商品
     */
    @PostMapping("add")
    public ResponseEntity<Integer> add(@RequestBody MemberCart memberCart) {
        System.out.println("添加购物车商品 - " + memberCart.getProductName() + ", 数量: " + memberCart.getQuantity());
        return ResponseEntity.ok(memberCartService.insert(memberCart));
    }

    /**
     * 修改购物车
     *
     * @return 是否修改
     */
    @PostMapping("modify")
    public ResponseEntity<Integer> modify(@Valid @RequestBody MemberCart memberCart) {
        System.out.println("修改购物车商品数量 - ID: " + memberCart.getId() + ", 数量: " + memberCart.getQuantity());
        return ResponseEntity.ok(memberCartService.update(memberCart));
    }

    /**
     * 删除购物车商品
     *
     * @return 是否删除成功
     */
    @DeleteMapping("remove")
    public ResponseEntity<Integer> remove(@RequestBody String ids) {
        System.out.println("删除购物车商品 - IDs: " + ids);
        return ResponseEntity.ok(memberCartService.deleteByIds(ids));
    }

    /**
     * 删除单个购物车商品
     *
     * @param id 购物车商品ID
     * @return 是否删除成功
     */
    @DeleteMapping("remove/{id}")
    public ResponseEntity<Integer> removeById(@PathVariable Long id) {
        System.out.println("删除购物车商品 - ID: " + id);
        return ResponseEntity.ok(memberCartService.deleteById(id));
    }

    /**
     * 清空购物车
     *
     * @return 删除的记录数
     */
    @DeleteMapping("clear")
    public ResponseEntity<Integer> clearCart() {
        System.out.println("清空购物车");
        return ResponseEntity.ok(memberCartService.clearCart());
    }

    /**
     * 购物车列表
     *
     * @return 购物车列表
     */
    @GetMapping("list")
    public AjaxResult getCartList() {
        try {
            Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
            if (member == null) {
                return AjaxResult.error("用户信息不存在");
            }

            MemberCartQuery query = new MemberCartQuery();
            query.setMemberId(member.getId());
            List<MemberCartVO> cartList = memberCartService.selectList(query, null);

            System.out.println("查询用户" + member.getId() + "的购物车，共" + (cartList != null ? cartList.size() : 0) + "件商品");

            return AjaxResult.successData(cartList != null ? cartList : new ArrayList<>());
        } catch (Exception e) {
            System.err.println("获取购物车列表失败: " + e.getMessage());
            e.printStackTrace();
            return AjaxResult.error("获取购物车列表失败");
        }
    }
}
