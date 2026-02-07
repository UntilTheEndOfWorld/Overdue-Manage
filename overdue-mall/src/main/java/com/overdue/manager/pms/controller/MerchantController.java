package com.overdue.manager.pms.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.overdue.manager.pms.domain.vo.ProductVO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.overdue.common.annotation.Log;
import com.overdue.common.core.controller.BaseController;
import com.overdue.common.core.domain.AjaxResult;
import com.overdue.common.core.page.TableDataInfo;
import com.overdue.common.enums.BusinessType;
import com.overdue.common.utils.poi.ExcelUtil;
import com.overdue.manager.pms.domain.entity.Merchant;
import com.overdue.manager.pms.domain.query.MerchantQuery;
import com.overdue.manager.pms.domain.vo.MerchantVo;
import com.overdue.manager.pms.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 商户信息Controller
 * 
 * @author zcc
 */
@Api(tags = "商户管理")
@RestController
@RequestMapping("/pms/merchant")
public class MerchantController extends BaseController {
  @Autowired
  private MerchantService merchantService;

  /**
   * 查询商户信息列表
   */
  @ApiOperation("查询商户信息列表")
  @PreAuthorize("@ss.hasPermi('pms:merchant:list')")
  @GetMapping("/list")
  public TableDataInfo list(MerchantQuery query) {
    startPage();
    List<MerchantVo> list = merchantService.selectMerchantList(query);
    return getDataTable(list);
  }

  /**
   * 导出商户信息列表
   */
  @ApiOperation("导出商户信息列表")
  @PreAuthorize("@ss.hasPermi('pms:merchant:export')")
  @Log(title = "商户信息", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(HttpServletResponse response, MerchantQuery query) throws IOException {
    List<MerchantVo> list = merchantService.selectMerchantList(query);
    ExcelUtil<MerchantVo> util = new ExcelUtil<MerchantVo>(MerchantVo.class);
    util.exportExcel(response, list, "商户信息数据");
  }

  /**
   * 获取商户信息详细信息
   */
  @ApiOperation("获取商户信息详细信息")
  @PreAuthorize("@ss.hasPermi('pms:merchant:query')")
  @GetMapping(value = "/{id}")
  public ResponseEntity<MerchantVo> getInfo(@PathVariable("id") Long id) {
    return ResponseEntity. ok(merchantService.selectMerchantById(id));
  }

  /**
   * 新增商户信息
   */
  @ApiOperation("新增商户信息")
  @PreAuthorize("@ss.hasPermi('pms:merchant:add')")
  @Log(title = "商户信息", businessType = BusinessType.INSERT)
  @PostMapping
  public AjaxResult add(@RequestBody Merchant merchant) {
    if (!merchantService.checkMerchantCodeUnique(merchant)) {
      return error("新增商户'" + merchant.getMerchantName() + "'失败，商户编码已存在");
    }
    return toAjax(merchantService.insertMerchant(merchant));
  }

  /**
   * 修改商户信息
   */
  @ApiOperation("修改商户信息")
  @PreAuthorize("@ss.hasPermi('pms:merchant:edit')")
  @Log(title = "商户信息", businessType = BusinessType.UPDATE)
  @PutMapping
  public AjaxResult edit(@RequestBody Merchant merchant) {
    if (!merchantService.checkMerchantCodeUnique(merchant)) {
      return error("修改商户'" + merchant.getMerchantName() + "'失败，商户编码已存在");
    }
    return toAjax(merchantService.updateMerchant(merchant));
  }

  /**
   * 删除商户信息
   */
  @ApiOperation("删除商户信息")
  @PreAuthorize("@ss.hasPermi('pms:merchant:remove')")
  @Log(title = "商户信息", businessType = BusinessType.DELETE)
  @DeleteMapping("/{ids}")
  public AjaxResult remove(@PathVariable Long[] ids) {
    return toAjax(merchantService.deleteMerchantByIds(ids));
  }

  /**
   * 更新商户状态
   */
  @ApiOperation("更新商户状态")
  @PreAuthorize("@ss.hasPermi('pms:merchant:edit')")
  @Log(title = "商户信息", businessType = BusinessType.UPDATE)
  @PutMapping("/changeStatus")
  public AjaxResult changeStatus(@RequestBody Merchant merchant) {
    return toAjax(merchantService.updateMerchantStatus(merchant));
  }
}
