package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 管理员为医生开户请求：同时创建医生档案与登录账号。
 */
public class DoctorAccountRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 40, message = "用户名长度应为 3-40 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度应为 6-40 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名长度不能超过 30")
    private String realName;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "^[男女]$", message = "性别只能为男或女")
    private String gender;

    @NotNull(message = "请选择所属科室")
    private Long departmentId;

    @Size(max = 30, message = "职称长度不能超过 30")
    private String title;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 200, message = "擅长领域长度不能超过 200")
    private String specialty;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
}
