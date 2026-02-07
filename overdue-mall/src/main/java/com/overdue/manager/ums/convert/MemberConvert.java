package com.overdue.manager.ums.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.ums.domain.entity.Member;
import com.overdue.manager.ums.domain.vo.MemberVO;
import java.util.List;
/**
 * 会员信息  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface MemberConvert  {

    List<MemberVO> dos2vos(List<Member> list);
}
