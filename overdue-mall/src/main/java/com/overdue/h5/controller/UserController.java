package com.overdue.h5.controller;

import com.overdue.manager.ums.domain.entity.MemberAddress;
import com.overdue.manager.ums.domain.query.MemberAddressQuery;
import com.overdue.manager.ums.service.MemberAddressService;
import com.overdue.manager.ums.service.MemberService;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.common.utils.MemberSecurityUtils;
import com.overdue.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("h5/ucenter")
@RestController
public class UserController {
    @Autowired
    private MemberAddressService memberAddressService;

    @Autowired
    private MemberService memberService;

    @GetMapping("user-address")
    public ResponseEntity<List<MemberAddress>> queryPageOfAddress() {
        MemberAddressQuery q = new MemberAddressQuery();
        q.setMemberId(MemberSecurityUtils.getMemberId());
        return ResponseEntity.ok(memberAddressService.selectList(q, null));
    }

    @PostMapping("add-update-user-address")
    public ResponseEntity<MemberAddress> addOrUpdateAddress(@RequestBody MemberAddress address) {
        if (address.getId() != null) {
            address.setMemberId(null);
            memberAddressService.updateSelective(address);
        } else {
            address.setMemberId(MemberSecurityUtils.getMemberId());
            memberAddressService.insert(address);
        }
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("delete-user-address")
    public ResponseEntity<Integer> deleteUserAddress(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(memberAddressService.deleteUserIds(ids));
    }

    @GetMapping("detail-user-address")
    public ResponseEntity<MemberAddress> detailUserAddress(@RequestParam("id") Long id) {
        return ResponseEntity.ok(memberAddressService.selectByUserAndId(id));
    }

    /**
     * 获取用户详细信息（包含头像、昵称等）
     */
    @GetMapping("user-profile")
    public AjaxResult getUserProfile() {
        try {
            Long memberId = MemberSecurityUtils.getMemberId();
            Member member = memberService.selectById(memberId);

            if (member != null) {
                return AjaxResult.success(member);
            } else {
                return AjaxResult.error("用户信息不存在");
            }
        } catch (Exception e) {
            return AjaxResult.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息（头像、昵称等）
     */
    @PutMapping("update-profile")
    public AjaxResult updateUserProfile(@RequestBody Member member) {
        try {
            Long memberId = MemberSecurityUtils.getMemberId();
            member.setId(memberId);

            // 只允许更新特定字段
            Member updateMember = new Member();
            updateMember.setId(memberId);
            updateMember.setNickname(member.getNickname());
            updateMember.setAvatar(member.getAvatar());
            updateMember.setGender(member.getGender());
            updateMember.setCity(member.getCity());
            updateMember.setProvince(member.getProvince());
            updateMember.setCountry(member.getCountry());

            int result = memberService.update(updateMember);
            if (result > 0) {
                return AjaxResult.success("更新成功");
            } else {
                return AjaxResult.error("更新失败");
            }
        } catch (Exception e) {
            return AjaxResult.error("更新用户信息失败：" + e.getMessage());
        }
    }
}
