package com.overdue.manager.ums.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.ums.domain.entity.MemberLogininfor;
import com.overdue.manager.ums.domain.vo.MemberLogininforVO;
import java.util.List;
/**
 * 会员登录记录  DO <=> DTO <=> VO / BO / Query
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface MemberLogininforConvert  {

    List<MemberLogininforVO> dos2vos(List<MemberLogininfor> list);
}
