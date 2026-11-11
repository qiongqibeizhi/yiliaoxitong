package com.example.yiliaoxitong.dto;

/**
 * 科室分页查询条件。
 */
public class DepartmentQuery extends PageQuery {

    /** 科室名称，模糊匹配 */
    private String name;

    /** 状态：1 启用 0 停用 */
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
