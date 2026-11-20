package com.example.yiliaoxitong.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 管理员为其他管理员开户请求 */
public class AdminAccountRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 40, message = "用户名长度应为 3-40 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度应为 6-40 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 30, message = "姓名长度不能超过 30")
    private String realName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
}
