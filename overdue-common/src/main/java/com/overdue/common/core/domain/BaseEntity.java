package com.overdue.common.core.domain;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity基类
 *
 * @author ruoyi
 */
public abstract class BaseEntity extends BaseAudit implements Serializable, IQuery {
    private static final long serialVersionUID = 1L;

    /**
     * 搜索值（仅查询条件，非表字段）
     */
    @TableField(exist = false)
    private String searchValue;

    /**
     * 备注
     */
    private String remark;

    /**
     * 请求参数（如前端 JSON 中的 params，非表字段；勿参与 MyBatis 插入/更新）
     */
    @TableField(exist = false)
    private Map<String, Object> params;

    @Override
    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
