package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.SharedSpace;
import com.overdue.manager.item.service.SharedSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 共享空间管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-共享空间管理")
@RestController
@RequestMapping("/item/space")
public class SharedSpaceController extends BaseController {
    @Autowired
    private SharedSpaceService sharedSpaceService;

    /**
     * 查询共享空间列表
     */
    @ApiOperation("查询共享空间列表")
    @PreAuthorize("@ss.hasPermi('item:space:list')")
    @GetMapping("/list")
    public TableDataInfo list(SharedSpace sharedSpace) {
        startPage();
        List<SharedSpace> list = sharedSpaceService.selectSharedSpaceList(sharedSpace);
        return getDataTable(list);
    }

    /**
     * 获取共享空间详细信息
     */
    @ApiOperation("获取共享空间详细信息")
    @PreAuthorize("@ss.hasPermi('item:space:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(sharedSpaceService.selectById(id));
    }

    /**
     * 新增共享空间
     */
    @ApiOperation("新增共享空间")
    @PreAuthorize("@ss.hasPermi('item:space:add')")
    @Log(title = "共享空间管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SharedSpace sharedSpace) {
        return toAjax(sharedSpaceService.insertSharedSpace(sharedSpace));
    }

    /**
     * 修改共享空间
     */
    @ApiOperation("修改共享空间")
    @PreAuthorize("@ss.hasPermi('item:space:edit')")
    @Log(title = "共享空间管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SharedSpace sharedSpace) {
        return toAjax(sharedSpaceService.updateSharedSpace(sharedSpace));
    }

    /**
     * 删除共享空间
     */
    @ApiOperation("删除共享空间")
    @PreAuthorize("@ss.hasPermi('item:space:remove')")
    @Log(title = "共享空间管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(sharedSpaceService.deleteSharedSpaceByIds(ids));
    }
}
