package com.example.yiliaoxitong.service;

import com.example.yiliaoxitong.dto.AdminAccountRequest;
import com.example.yiliaoxitong.dto.DoctorAccountRequest;
import com.example.yiliaoxitong.entity.SysUser;

import java.util.List;

/** 管理员账号管理：为医生/管理员开户、查询账号列表 */
public interface AccountService {

    List<SysUser> listAccounts(String role);

    SysUser createDoctorAccount(DoctorAccountRequest request);

    SysUser createAdminAccount(AdminAccountRequest request);
}
