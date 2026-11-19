package com.example.yiliaoxitong.dto;

/**
 * 患者分页查询条件。
 */
public class PatientQuery extends PageQuery {

    /** 姓名或患者编号，模糊匹配 */
    private String keyword;

    private String phone;

    /** 管理员可按身份证号查询（完整或尾号） */
    private String idCard;

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

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
