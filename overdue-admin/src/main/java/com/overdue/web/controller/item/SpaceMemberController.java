package com.overdue.web.controller.item;

import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.manager.item.domain.entity.SpaceMember;
import com.overdue.manager.item.service.SpaceMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 空间成员管理Controller
 * 
 * @author overdue
 */
@Api(tags = "过期了吗-空间成员管理")
@RestController
@RequestMapping("/item/spaceMember")
public class SpaceMemberController extends BaseController {

    @Autowired
    private SpaceMemberService spaceMemberService;

    /**
     * 查询空间成员列表
     */
    @ApiOperation("查询空间成员列表")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:list')")
    @GetMapping("/list")
    public TableDataInfo list(SpaceMember spaceMember) {
        startPage();
        List<SpaceMember> list = spaceMemberService.selectSpaceMemberList(spaceMember);
        return getDataTable(list);
    }

    /**
     * 获取空间成员详细信息
     */
    @ApiOperation("获取空间成员详细信息")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(spaceMemberService.selectById(id));
    }

    /**
     * 按空间ID查询成员列表
     */
    @ApiOperation("按空间ID查询成员列表")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:list')")
    @GetMapping("/space/{spaceId}")
    public AjaxResult listBySpace(@PathVariable("spaceId") Long spaceId) {
        return AjaxResult.success(spaceMemberService.selectBySpaceId(spaceId));
    }

    /**
     * 新增空间成员
     */
    @ApiOperation("新增空间成员")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:add')")
    @Log(title = "空间成员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SpaceMember spaceMember) {
        if (spaceMember.getSpaceId() == null || spaceMember.getUserId() == null) {
            return AjaxResult.error("空间ID和用户ID不能为空");
        }
        String role = spaceMember.getRole();
        if (role == null || role.isEmpty()) {
            role = "member";
        }
        return toAjax(spaceMemberService.addMember(spaceMember.getSpaceId(), spaceMember.getUserId(), role));
    }

    /**
     * 修改空间成员（如修改角色）
     */
    @ApiOperation("修改空间成员")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:edit')")
    @Log(title = "空间成员管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SpaceMember spaceMember) {
        return toAjax(spaceMemberService.updateSpaceMember(spaceMember));
    }

    /**
     * 移除空间成员（软删除-标记退出）
     */
    @ApiOperation("移除空间成员")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:remove')")
    @Log(title = "空间成员管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/leave/{spaceId}/{userId}")
    public AjaxResult leave(@PathVariable("spaceId") Long spaceId, @PathVariable("userId") Long userId) {
        return toAjax(spaceMemberService.leaveSpace(spaceId, userId));
    }

    /**
     * 删除空间成员（物理删除）
     */
    @ApiOperation("删除空间成员")
    @PreAuthorize("@ss.hasPermi('item:spaceMember:remove')")
    @Log(title = "空间成员管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            count += spaceMemberService.deleteById(id);
        }
        return toAjax(count);
    }
}
