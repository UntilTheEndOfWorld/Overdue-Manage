package com.overdue.h5.config;

import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.common.constant.Constants;
import com.overdue.framework.config.LocalDataUtil;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtil {

  public static Member getLocalMember() {
    Member member = (Member) LocalDataUtil.getVar(Constants.MEMBER_INFO);
    return member;
  }

}
