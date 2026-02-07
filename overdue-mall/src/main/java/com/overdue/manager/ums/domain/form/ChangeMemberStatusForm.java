package com.overdue.manager.ums.domain.form;

import lombok.Data;

@Data
public class ChangeMemberStatusForm {
    private Long memberId;
    private Integer status;
}
