package com.example.yiliaoxitong.dto;

/**
 * 医生分页查询条件，支持姓名/工号模糊、科室、职称、状态组合查询。
 */
public class DoctorQuery extends PageQuery {

    private String keyword;

    private Long departmentId;

    private String title;

    private Integer status;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
