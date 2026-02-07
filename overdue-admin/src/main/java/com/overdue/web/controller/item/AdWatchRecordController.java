package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.AdWatchRecord;
import com.overdue.manager.item.service.AdWatchRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告观看记录管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-广告观看记录管理")
@RestController
@RequestMapping("/item/adWatch")
public class AdWatchRecordController extends BaseController {

    @Autowired
    private AdWatchRecordService adWatchRecordService;

    /**
     * 查询广告观看记录列表
     */
    @ApiOperation("查询广告观看记录列表")
    @PreAuthorize("@ss.hasPermi('item:adWatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(AdWatchRecord adWatchRecord) {
        startPage();
        List<AdWatchRecord> list = adWatchRecordService.selectAdWatchRecordList(adWatchRecord);
        return getDataTable(list);
    }

    /**
     * 获取广告观看记录详细信息
     */
    @ApiOperation("获取广告观看记录详细信息")
    @PreAuthorize("@ss.hasPermi('item:adWatch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(adWatchRecordService.selectById(id));
    }

    /**
     * 按用户ID查询广告观看记录
     */
    @ApiOperation("按用户ID查询广告观看记录")
    @PreAuthorize("@ss.hasPermi('item:adWatch:query')")
    @GetMapping("/user/{userId}")
    public AjaxResult getByUserId(@PathVariable("userId") Long userId) {
        AdWatchRecord record = adWatchRecordService.selectByUserId(userId);
        if (record == null) {
            return AjaxResult.success("该用户暂无广告观看记录", null);
        }
        return AjaxResult.success(record);
    }

    /**
     * 新增广告观看记录
     */
    @ApiOperation("新增广告观看记录")
    @PreAuthorize("@ss.hasPermi('item:adWatch:add')")
    @Log(title = "广告观看记录管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AdWatchRecord adWatchRecord) {
        return toAjax(adWatchRecordService.insertAdWatchRecord(adWatchRecord));
    }

    /**
     * 修改广告观看记录
     */
    @ApiOperation("修改广告观看记录")
    @PreAuthorize("@ss.hasPermi('item:adWatch:edit')")
    @Log(title = "广告观看记录管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AdWatchRecord adWatchRecord) {
        return toAjax(adWatchRecordService.updateAdWatchRecord(adWatchRecord));
    }

    /**
     * 删除广告观看记录
     */
    @ApiOperation("删除广告观看记录")
    @PreAuthorize("@ss.hasPermi('item:adWatch:remove')")
    @Log(title = "广告观看记录管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += adWatchRecordService.deleteAdWatchRecordById(id);
        }
        return toAjax(count);
    }

    /**
     * 获取广告观看统计信息
     */
    @ApiOperation("获取广告观看统计信息")
    @PreAuthorize("@ss.hasPermi('item:adWatch:list')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        return AjaxResult.success(adWatchRecordService.getStatistics());
    }

    /**
     * 按日期统计广告观看记录
     */
    @ApiOperation("按日期统计广告观看记录")
    @PreAuthorize("@ss.hasPermi('item:adWatch:list')")
    @GetMapping("/stats/date")
    public AjaxResult getStatsByDate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return AjaxResult.success(adWatchRecordService.getStatsByDate(startDate, endDate));
    }

    /**
     * 查询用户剩余额度
     */
    @ApiOperation("查询用户剩余额度")
    @PreAuthorize("@ss.hasPermi('item:adWatch:query')")
    @GetMapping("/quota/{userId}")
    public AjaxResult getRemainingQuota(@PathVariable("userId") Long userId) {
        int remaining = adWatchRecordService.getRemainingQuota(userId);
        return AjaxResult.success("剩余额度", remaining);
    }
}
