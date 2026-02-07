package com.overdue.h5.controller;

import com.overdue.h5.service.H5MemberAddressService;
import com.overdue.manager.ums.domain.form.MemberAddressForm;
import com.overdue.manager.ums.domain.vo.MemberAddressVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/h5/member/address")
public class H5MemberAddressController {

  @Autowired
  private H5MemberAddressService h5MemberAddressService;

  // 开发模式标志 - 已注释，确保使用正常业务逻辑
  // private static final boolean DEV_MODE = false;

  @GetMapping("/list")
  public ResponseEntity<List<MemberAddressVO>> getList() {
    return ResponseEntity.ok(h5MemberAddressService.selectList());
  }

  @GetMapping("/default")
  public ResponseEntity<MemberAddressVO> getDefault() {
    return ResponseEntity.ok(h5MemberAddressService.getDefault());
  }

  @PostMapping("/create")
  public ResponseEntity<Integer> create(@RequestBody MemberAddressForm memberAddressForm) {
    System.out.println("创建地址 - " + memberAddressForm.getName() + " " + memberAddressForm.getPhone());
    return ResponseEntity.ok(h5MemberAddressService.insert(memberAddressForm));
  }

  @PutMapping("/update")
  public ResponseEntity<Integer> update(@RequestBody MemberAddressForm memberAddressForm) {
    System.out.println("更新地址 - ID: " + memberAddressForm.getId());
    return ResponseEntity.ok(h5MemberAddressService.update(memberAddressForm));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MemberAddressVO> getInfo(@PathVariable Long id) {
    return ResponseEntity.ok(h5MemberAddressService.selectById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Integer> remove(@PathVariable Long id) {
    System.out.println("删除地址 - ID: " + id);
    return ResponseEntity.ok(h5MemberAddressService.deleteById(id));
  }

  @PostMapping("/setDefault/{id}")
  public ResponseEntity<Integer> setDefault(@PathVariable Long id) {
    System.out.println("设置默认地址 - ID: " + id);
    return ResponseEntity.ok(h5MemberAddressService.setDefault(id));
  }
}
