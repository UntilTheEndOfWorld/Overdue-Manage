package com.overdue.web.controller.item;

import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.manager.item.domain.entity.OperationLog;
import com.overdue.manager.item.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 操作日志管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-操作日志管理")
@RestController
@RequestMapping("/item/log")
public class OperationLogController extends BaseController {
    @Autowired
    private OperationLogService operationLogService;

    /**
     * 查询操作日志列表
     */
    @ApiOperation("查询操作日志列表")
    @PreAuthorize("@ss.hasPermi('item:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(OperationLog operationLog) {
        startPage();
        List<OperationLog> list = operationLogService.selectOperationLogList(operationLog);
        return getDataTable(list);
    }

    /**
     * 根据空间ID查询操作日志
     */
    @ApiOperation("根据空间ID查询操作日志")
    @PreAuthorize("@ss.hasPermi('item:log:list')")
    @GetMapping("/space/{spaceId}")
    public AjaxResult getBySpaceId(@PathVariable("spaceId") Long spaceId) {
        return AjaxResult.success(operationLogService.selectBySpaceId(spaceId));
    }

    /**
     * 根据物品ID查询操作日志
     */
    @ApiOperation("根据物品ID查询操作日志")
    @PreAuthorize("@ss.hasPermi('item:log:list')")
    @GetMapping("/item/{itemId}")
    public AjaxResult getByItemId(@PathVariable("itemId") Long itemId, 
                                   @RequestParam("itemType") String itemType) {
        return AjaxResult.success(operationLogService.selectByItemId(itemId, itemType));
    }
}
