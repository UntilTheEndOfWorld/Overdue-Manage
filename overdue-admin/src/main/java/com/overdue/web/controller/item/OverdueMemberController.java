package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.OverdueMember;
import com.overdue.manager.item.service.OverdueMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 会员管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-会员管理")
@RestController
@RequestMapping("/item/member")
public class OverdueMemberController extends BaseController {
    @Autowired
    private OverdueMemberService overdueMemberService;

    /**
     * 查询会员信息列表
     */
    @ApiOperation("查询会员信息列表")
    @PreAuthorize("@ss.hasPermi('item:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(OverdueMember overdueMember) {
        startPage();
        List<OverdueMember> list = overdueMemberService.selectOverdueMemberList(overdueMember);
        return getDataTable(list);
    }

    /**
     * 获取会员详细信息
     */
    @ApiOperation("获取会员详细信息")
    @PreAuthorize("@ss.hasPermi('item:member:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(overdueMemberService.selectById(id));
    }

    /**
     * 新增会员信息
     */
    @ApiOperation("新增会员信息")
    @PreAuthorize("@ss.hasPermi('item:member:add')")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OverdueMember overdueMember) {
        return toAjax(overdueMemberService.insertOverdueMember(overdueMember));
    }

    /**
     * 修改会员信息
     */
    @ApiOperation("修改会员信息")
    @PreAuthorize("@ss.hasPermi('item:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OverdueMember overdueMember) {
        return toAjax(overdueMemberService.updateOverdueMember(overdueMember));
    }
}
