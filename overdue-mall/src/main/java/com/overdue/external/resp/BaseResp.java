package com.overdue.external.resp;

import lombok.Data;

@Data
public class BaseResp {
    private Integer errcode;
    private String errmsg;
}
