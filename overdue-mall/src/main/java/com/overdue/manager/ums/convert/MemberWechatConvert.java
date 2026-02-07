package com.overdue.manager.ums.convert;

import com.overdue.external.resp.AccessTokenResp;
import org.mapstruct.Mapper;
import com.overdue.manager.ums.domain.entity.MemberWechat;
import com.overdue.manager.ums.domain.vo.MemberWechatVO;
import java.util.List;
/**
 * 用户微信信息  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface MemberWechatConvert  {

    List<MemberWechatVO> dos2vos(List<MemberWechat> list);

    MemberWechat info2do(AccessTokenResp info);
}
