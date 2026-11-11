package com.example.yiliaoxitong.dto;

/**
 * 患者分页查询条件。
 */
public class PatientQuery extends PageQuery {

    /** 姓名或患者编号，模糊匹配 */
    private String keyword;

    private String phone;

    private String gender;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
