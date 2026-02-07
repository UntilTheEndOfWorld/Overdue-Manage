package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.PersonalItem;
import com.overdue.manager.item.service.PersonalItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 个人物品管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-个人物品管理")
@RestController
@RequestMapping("/item/personal")
public class PersonalItemController extends BaseController {
    @Autowired
    private PersonalItemService personalItemService;

    /**
     * 查询个人物品列表
     */
    @ApiOperation("查询个人物品列表")
    @PreAuthorize("@ss.hasPermi('item:personal:list')")
    @GetMapping("/list")
    public TableDataInfo list(PersonalItem personalItem) {
        startPage();
        List<PersonalItem> list = personalItemService.selectPersonalItemList(personalItem);
        return getDataTable(list);
    }

    /**
     * 获取个人物品详细信息
     */
    @ApiOperation("获取个人物品详细信息")
    @PreAuthorize("@ss.hasPermi('item:personal:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(personalItemService.selectById(id));
    }

    /**
     * 新增个人物品
     */
    @ApiOperation("新增个人物品")
    @PreAuthorize("@ss.hasPermi('item:personal:add')")
    @Log(title = "个人物品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PersonalItem personalItem) {
        return toAjax(personalItemService.insertPersonalItem(personalItem));
    }

    /**
     * 修改个人物品
     */
    @ApiOperation("修改个人物品")
    @PreAuthorize("@ss.hasPermi('item:personal:edit')")
    @Log(title = "个人物品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PersonalItem personalItem) {
        return toAjax(personalItemService.updatePersonalItem(personalItem));
    }

    /**
     * 删除个人物品
     */
    @ApiOperation("删除个人物品")
    @PreAuthorize("@ss.hasPermi('item:personal:remove')")
    @Log(title = "个人物品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(personalItemService.deletePersonalItemByIds(ids));
    }

    /**
     * 查询即将过期的物品
     */
    @ApiOperation("查询即将过期的物品")
    @PreAuthorize("@ss.hasPermi('item:personal:list')")
    @GetMapping("/expiring/{userId}")
    public AjaxResult getExpiringItems(@PathVariable("userId") Long userId) {
        return AjaxResult.success(personalItemService.selectExpiringItems(userId));
    }

    /**
     * 查询已过期的物品
     */
    @ApiOperation("查询已过期的物品")
    @PreAuthorize("@ss.hasPermi('item:personal:list')")
    @GetMapping("/expired/{userId}")
    public AjaxResult getExpiredItems(@PathVariable("userId") Long userId) {
        return AjaxResult.success(personalItemService.selectExpiredItems(userId));
    }
}
