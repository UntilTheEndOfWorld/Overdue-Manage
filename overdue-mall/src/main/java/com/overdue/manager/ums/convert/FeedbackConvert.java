package com.overdue.manager.ums.convert;

import org.mapstruct.Mapper;
import com.overdue.manager.ums.domain.entity.Feedback;
import com.overdue.manager.ums.domain.vo.FeedbackVO;
import java.util.List;
/**
 * 意见反馈  DO <=> VO / BO
 *
 * @author zcc
 */
@Mapper(componentModel = "spring")
public interface FeedbackConvert  {

    List<FeedbackVO> dos2vos(List<Feedback> list);
}
