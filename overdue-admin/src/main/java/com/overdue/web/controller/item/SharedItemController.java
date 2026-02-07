package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.SharedItem;
import com.overdue.manager.item.service.SharedItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 共享物品管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-共享物品管理")
@RestController
@RequestMapping("/item/sharedItem")
public class SharedItemController extends BaseController {

    @Autowired
    private SharedItemService sharedItemService;

    /**
     * 查询共享物品列表
     */
    @ApiOperation("查询共享物品列表")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(SharedItem sharedItem) {
        startPage();
        List<SharedItem> list = sharedItemService.selectSharedItemList(sharedItem);
        return getDataTable(list);
    }

    /**
     * 获取共享物品详细信息
     */
    @ApiOperation("获取共享物品详细信息")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(sharedItemService.selectById(id));
    }

    /**
     * 新增共享物品
     */
    @ApiOperation("新增共享物品")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:add')")
    @Log(title = "共享物品管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SharedItem sharedItem) {
        return toAjax(sharedItemService.insertSharedItem(sharedItem));
    }

    /**
     * 修改共享物品
     */
    @ApiOperation("修改共享物品")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:edit')")
    @Log(title = "共享物品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SharedItem sharedItem) {
        return toAjax(sharedItemService.updateSharedItem(sharedItem));
    }

    /**
     * 删除共享物品
     */
    @ApiOperation("删除共享物品")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:remove')")
    @Log(title = "共享物品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += sharedItemService.deleteSharedItemById(id);
        }
        return toAjax(count);
    }

    /**
     * 按空间ID查询物品列表
     */
    @ApiOperation("按空间ID查询物品列表")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:list')")
    @GetMapping("/space/{spaceId}")
    public AjaxResult listBySpace(@PathVariable("spaceId") Long spaceId) {
        return AjaxResult.success(sharedItemService.selectBySpaceId(spaceId));
    }

    /**
     * 查询即将过期的共享物品
     */
    @ApiOperation("查询即将过期的共享物品")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:list')")
    @GetMapping("/expiring/{spaceId}")
    public AjaxResult getExpiringItems(@PathVariable("spaceId") Long spaceId) {
        return AjaxResult.success(sharedItemService.selectExpiringItems(spaceId));
    }

    /**
     * 查询已过期的共享物品
     */
    @ApiOperation("查询已过期的共享物品")
    @PreAuthorize("@ss.hasPermi('item:sharedItem:list')")
    @GetMapping("/expired/{spaceId}")
    public AjaxResult getExpiredItems(@PathVariable("spaceId") Long spaceId) {
        return AjaxResult.success(sharedItemService.selectExpiredItems(spaceId));
    }
}
