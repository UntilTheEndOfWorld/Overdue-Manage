package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.OverdueUser;
import com.overdue.manager.item.service.OverdueUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 用户管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-用户管理")
@RestController
@RequestMapping("/item/user")
public class OverdueUserController extends BaseController {
    @Autowired
    private OverdueUserService overdueUserService;

    /**
     * 查询用户列表
     */
    @ApiOperation("查询用户列表")
    @PreAuthorize("@ss.hasPermi('item:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(OverdueUser overdueUser) {
        startPage();
        List<OverdueUser> list = overdueUserService.selectOverdueUserList(overdueUser);
        return getDataTable(list);
    }

    /**
     * 获取用户详细信息
     */
    @ApiOperation("获取用户详细信息")
    @PreAuthorize("@ss.hasPermi('item:user:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(overdueUserService.selectById(id));
    }

    /**
     * 新增用户
     */
    @ApiOperation("新增用户")
    @PreAuthorize("@ss.hasPermi('item:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OverdueUser overdueUser) {
        return toAjax(overdueUserService.insertOverdueUser(overdueUser));
    }

    /**
     * 修改用户
     */
    @ApiOperation("修改用户")
    @PreAuthorize("@ss.hasPermi('item:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OverdueUser overdueUser) {
        return toAjax(overdueUserService.updateOverdueUser(overdueUser));
    }

    /**
     * 删除用户
     */
    @ApiOperation("删除用户")
    @PreAuthorize("@ss.hasPermi('item:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(overdueUserService.deleteOverdueUserByIds(ids));
    }
}
